package pl.north93.zgame.api.global.redis.observable.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.PlatformConnector;
import pl.north93.zgame.api.global.component.Component;
import pl.north93.zgame.api.global.component.annotations.InjectComponent;
import pl.north93.zgame.api.global.data.StorageConnector;
import pl.north93.zgame.api.global.redis.messaging.TemplateManager;
import pl.north93.zgame.api.global.redis.observable.ICacheBuilder;
import pl.north93.zgame.api.global.redis.observable.IObservationManager;
import pl.north93.zgame.api.global.redis.observable.Lock;
import pl.north93.zgame.api.global.redis.observable.ObjectKey;
import pl.north93.zgame.api.global.redis.observable.ProvidingRedisKey;
import pl.north93.zgame.api.global.redis.observable.Value;
import pl.north93.zgame.api.global.redis.subscriber.RedisSubscriber;
import redis.clients.jedis.JedisPool;
import redis.clients.util.SafeEncoder;

public class ObservationManagerImpl extends Component implements IObservationManager
{
    private final Map<String, WeakReference<Value<?>>> cachedValues = new HashMap<>();
    private final List<LockImpl>                       waitingLocks = new ArrayList<>();
    @InjectComponent("API.Database.StorageConnector")
    private StorageConnector storageConnector;
    @InjectComponent("API.Database.Redis.MessagePackSerializer")
    private TemplateManager  msgPack;
    @InjectComponent("API.Database.Redis.Subscriber")
    private RedisSubscriber  redisSubscriber;

    @Override
    public <T> Value<T> get(final Class<T> clazz, final String objectKey)
    {
        return this.get(clazz, new ObjectKey(objectKey));
    }

    @Override
    public <T> Value<T> get(final Class<T> clazz, final ObjectKey objectKey)
    {
        final String key = objectKey.getKey();

        WeakReference<Value<?>> value;
        synchronized (this.cachedValues)
        {
            value = this.cachedValues.get(key);
            if (value == null || value.get() == null)
            {
                value = new WeakReference<>(new CachedValueImpl<>(this, clazz, objectKey));
                this.cachedValues.put(key, value);
            }
        }

        //noinspection unchecked
        return (Value<T>) value.get();
    }

    @Override
    public <T> Value<T> get(final Class<T> clazz, final ProvidingRedisKey keyProvider)
    {
        return this.get(clazz, keyProvider.getKey());
    }

    @Override
    public <T extends ProvidingRedisKey> Value<T> of(final T preCachedObject)
    {
        //noinspection unchecked
        final Value<T> value = this.get((Class<T>) preCachedObject.getClass(), preCachedObject);
        value.set(preCachedObject);
        return value;
    }

    @Override
    public <K, V> ICacheBuilder<K, V> cacheBuilder(final Class<K> keyClass, final Class<V> valueClass)
    {
        return new CacheBuilderImpl<>(this, keyClass, valueClass);
    }

    @Override
    public Lock getLock(final String name)
    {
        return new LockImpl(this, name);
    }

    @Override
    public Lock getMultiLock(final String... names)
    {
        final int namesLength = names.length;
        if (namesLength == 0)
        {
            throw new IllegalArgumentException("names must ne not empty");
        }
        else if (namesLength == 1)
        {
            return this.getLock(names[0]);
        }

        final Lock[] locks = new Lock[namesLength];
        for (int i = 0; i < namesLength; i++)
        {
            locks[i] = this.getLock(names[i]);
        }

        return new MultiLockImpl(locks);
    }

    /*default*/ void addWaitingLock(final LockImpl lock)
    {
        synchronized (this.waitingLocks)
        {
            this.waitingLocks.add(lock);
        }
    }

    private void unlockNotify(final String channel, final byte[] message)
    {
        final String lock = SafeEncoder.encode(message);
        synchronized (this.waitingLocks)
        {
            for (final LockImpl waitingLock : this.waitingLocks)
            {
                if (waitingLock.getName().equals(lock))
                {
                    waitingLock.remoteUnlock();
                }
            }
        }
    }

    @Override
    protected void enableComponent()
    {
        this.redisSubscriber.subscribe("unlock", this::unlockNotify);
    }

    @Override
    protected void disableComponent()
    {
        synchronized (this.cachedValues)
        {
            this.cachedValues.clear();
        }
    }

    /*default*/ PlatformConnector getPlatformConnector()
    {
        return this.getApiCore().getPlatformConnector();
    }

    /*default*/ JedisPool getJedis()
    {
        return this.storageConnector.getJedisPool();
    }

    /*default*/ TemplateManager getMsgPack()
    {
        return this.msgPack;
    }

    /*default*/ RedisSubscriber getRedisSubscriber()
    {
        return this.redisSubscriber;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("cachedValues", this.cachedValues).toString();
    }
}