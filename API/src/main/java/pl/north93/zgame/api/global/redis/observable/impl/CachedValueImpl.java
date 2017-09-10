package pl.north93.zgame.api.global.redis.observable.impl;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.lambdaworks.redis.api.sync.RedisCommands;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.data.StorageConnector;
import pl.north93.zgame.api.global.redis.observable.Lock;
import pl.north93.zgame.api.global.redis.observable.ObjectKey;
import pl.north93.zgame.api.global.redis.observable.Value;

class CachedValueImpl<T> extends CachedValue<T>
{
    private final Class<T>  clazz;
    private final ObjectKey objectKey;
    private final Lock      myLock;
    private       T         cache;

    public CachedValueImpl(final ObservationManagerImpl observationManager, final Class<T> clazz, final ObjectKey objectKey)
    {
        super(observationManager);
        this.clazz = clazz;
        this.objectKey = objectKey;

        this.myLock = observationManager.getLock("caval_lock:" + this.getInternalName());
        observationManager.getValueSubHandler().addListener(this);
    }

    @Override
    /*default*/ String getInternalName()
    {
        return "key:" + this.objectKey.getKey();
    }

    @Override
    /*default*/ void handleNewValue(final byte[] newValue)
    {
        if (newValue.length == 0)
        {
            this.cache = null;
        }
        else
        {
            this.cache = this.observationManager.getMsgPack().deserialize(this.clazz, newValue);
        }
    }

    @Override
    public T get()
    {
        if (this.cache != null)
        {
            return this.cache;
        }
        return this.getFromRedis();
    }

    @Override
    public T getWithoutCache()
    {
        return this.getFromRedis();
    }

    @Override
    public T getOr(final Supplier<T> defaultValue)
    {
        if (this.isCached())
        {
            return this.cache;
        }
        else if (this.isAvailable())
        {
            return this.getFromRedis();
        }
        else
        {
            this.set(defaultValue.get());
            return this.cache;
        }
    }

    @Override
    public synchronized T getAndDelete()
    {
        final String key = this.objectKey.getKey();

        final StorageConnector storageConnector = this.observationManager.getStorageConnector();
        final byte[] getResult = storageConnector.redisAtomically(redis ->
        {
            redis.multi();
            redis.get(key);
            redis.del(key);

            return (byte[]) redis.exec().get(0);
        });

        if (getResult == null)
        {
            return null;
        }

        // wysylamy aktualizacje mowiaca o usunieciu wartosci.
        this.observationManager.getValueSubHandler().update(this, new byte[0]);

        return this.observationManager.getMsgPack().deserialize(this.clazz, getResult);
    }

    @Override
    public boolean update(final Function<T, T> update)
    {
        try
        {
            this.lock();
            final T t = this.get();
            if (t != null)
            {
                this.set(update.apply(t));
                return true;
            }

            return false;
        }
        finally
        {
            this.unlock();
        }
    }

    @Override
    public void get(final Consumer<T> callback)
    {
        if (this.isCached())
        {
            callback.accept(this.cache);
        }
        else
        {
            this.observationManager.getPlatformConnector().runTaskAsynchronously(() -> callback.accept(this.getFromRedis()));
        }
    }

    @Override
    public void ifPresent(final Consumer<T> action)
    {
        final T value = this.get();
        if (value != null)
        {
            action.accept(value);
        }
    }

    private synchronized T getFromRedis()
    {
        final RedisCommands<String, byte[]> redis = this.observationManager.getRedis();
        final byte[] bytes = redis.get(this.objectKey.getKey());
        if (bytes == null)
        {
            this.cache = null;
            return null;
        }
        final T fromRedis = this.observationManager.getMsgPack().deserialize(this.clazz, bytes);
        this.cache = fromRedis;
        return fromRedis;
    }

    @Override
    public void set(final T newValue)
    {
        if (newValue == null)
        {
            this.delete();
        }
        else
        {
            this.cache = newValue;
            this.upload(-1);
        }
    }

    @Override
    public void setExpire(final T newValue, final long time, final TimeUnit timeUnit)
    {
        if (newValue == null)
        {
            this.delete();
        }
        else
        {
            this.cache = newValue;
            this.upload(timeUnit.toMillis(time));
        }
    }

    private void upload(final long time)
    {
        final byte[] serialized = this.observationManager.getMsgPack().serialize(this.clazz, this.cache);

        final RedisCommands<String, byte[]> redis = this.observationManager.getRedis();
        if (time == -1)
        {
            redis.set(this.objectKey.getKey(), serialized);
        }
        else
        {
            redis.psetex(this.objectKey.getKey(), time, serialized);
        }
        this.observationManager.getValueSubHandler().update(this, serialized);
    }

    @Override
    public Value<T> setIfUnavailable(final Supplier<T> defaultValue)
    {
        if (! this.isAvailable())
        {
            this.set(defaultValue.get());
        }
        return this;
    }

    @Override
    public boolean delete()
    {
        this.cache = null;

        final RedisCommands<String, byte[]> redis = this.observationManager.getRedis();
        final boolean success = redis.del(this.objectKey.getKey()) != 0L;
        this.observationManager.getValueSubHandler().update(this, new byte[0]);
        return success;
    }

    @Override
    public boolean isAvailable()
    {
        return this.observationManager.getRedis().exists(this.objectKey.getKey());
    }

    @Override
    public boolean isCached()
    {
        return this.cache != null;
    }

    @Override
    public void expire(final int seconds)
    {
        final RedisCommands<String, byte[]> redis = this.observationManager.getRedis();
        if (seconds == -1)
        {
            redis.persist(this.objectKey.getKey());
        }
        else
        {
            redis.expire(this.objectKey.getKey(), seconds);
        }
    }

    @Override
    public long getTimeToLive()
    {
        return this.observationManager.getRedis().ttl(this.objectKey.getKey());
    }

    @Override
    public final Lock getLock()
    {
        return this.myLock;
    }

    @Override
    protected void finalize() throws Throwable
    {
        this.observationManager.getValueSubHandler().removeListener(this);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("clazz", this.clazz).append("objectKey", this.objectKey).append("cache", this.cache).toString();
    }
}
