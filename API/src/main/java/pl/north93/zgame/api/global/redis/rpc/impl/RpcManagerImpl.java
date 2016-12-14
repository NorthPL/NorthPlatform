package pl.north93.zgame.api.global.redis.rpc.impl;

import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import pl.north93.zgame.api.global.component.Component;
import pl.north93.zgame.api.global.component.annotations.InjectComponent;
import pl.north93.zgame.api.global.data.StorageConnector;
import pl.north93.zgame.api.global.redis.messaging.TemplateManager;
import pl.north93.zgame.api.global.redis.rpc.RpcManager;
import pl.north93.zgame.api.global.redis.rpc.RpcTarget;
import pl.north93.zgame.api.global.redis.rpc.exceptions.RpcUnimplementedException;
import pl.north93.zgame.api.global.redis.rpc.impl.messaging.RpcExceptionInfo;
import pl.north93.zgame.api.global.redis.rpc.impl.messaging.RpcInvokeMessage;
import pl.north93.zgame.api.global.redis.rpc.impl.messaging.RpcResponseMessage;
import pl.north93.zgame.api.global.redis.subscriber.RedisSubscriber;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RpcManagerImpl extends Component implements RpcManager
{
    @InjectComponent("API.Database.StorageConnector")
    private StorageConnector                        storageConnector;
    @InjectComponent("API.Database.Redis.Subscriber")
    private RedisSubscriber                         redisSubscriber;
    @InjectComponent("API.Database.Redis.MessagePackSerializer")
    private TemplateManager                         msgPack;
    private final RpcProxyCache                     rpcProxyCache      = new RpcProxyCache(this);
    private final Int2ObjectMap<RpcResponseHandler> responseHandlerMap = new Int2ObjectArrayMap<>();
    private final Int2ObjectMap<RpcResponseLock>    locks              = new Int2ObjectArrayMap<>();
    private final Map<Class<?>, RpcObjectDescription> descriptionCache = new HashMap<>();

    @Override
    protected void enableComponent()
    {
    }

    @Override
    protected void disableComponent()
    {
    }

    @Override
    public void addListeningContext(final String id)
    {
        this.getApiCore().debug("addListeningContext(" + id + ")");
        if (this.getStatus().isDisabled())
        {
            this.getApiCore().getLogger().warning("Tried to register listeningContext while RpcManager is disabled");
            return;
        }
        this.redisSubscriber.subscribe("rpc:" + id + ":invoke", this::handleMethodInvocation);
        this.redisSubscriber.subscribe("rpc:" + id + ":response", this::handleResponse);
    }

    @Override
    public void addRpcImplementation(final Class<?> classInterface, final Object implementation)
    {
        this.getApiCore().debug("addRpcImplementation(" + classInterface + ", " + implementation.getClass().getName() + ")");
        this.responseHandlerMap.put(classInterface.getName().hashCode(), new RpcResponseHandler(this, classInterface, implementation));
    }

    @Override
    public <T> T createRpcProxy(final Class<T> classInterface, final RpcTarget target)
    {
        //noinspection unchecked
        return (T) this.rpcProxyCache.get(classInterface, target);
    }

    @Override
    public RpcObjectDescription getObjectDescription(final Class<?> classInterface)
    {
        final RpcObjectDescription rpcObjectDescription = this.descriptionCache.get(classInterface);
        if (rpcObjectDescription == null)
        {
            final RpcObjectDescription newObjDesc = new RpcObjectDescription(classInterface);
            this.descriptionCache.put(classInterface, newObjDesc);
            return newObjDesc;
        }
        return rpcObjectDescription;
    }

    /*default*/ RpcResponseLock createFor(final int requestId)
    {
        final RpcResponseLock lock = new RpcResponseLock();
        this.locks.put(requestId, lock);
        return lock;
    }

    /*default*/ void removeLock(final int requestId)
    {
        this.locks.remove(requestId);
    }

    /*default*/ JedisPool getJedisPool()
    {
        return this.storageConnector.getJedisPool();
    }

    /*default*/ void sendResponse(final String target, final Integer requestId, final Object response)
    {
        try (final Jedis jedis = this.storageConnector.getJedisPool().getResource())
        {
            final RpcResponseMessage responseMessage = new RpcResponseMessage(requestId, response);
            jedis.publish(("rpc:" + target + ":response").getBytes(), this.msgPack.serialize(RpcResponseMessage.class, responseMessage));
        }
    }

    private void handleMethodInvocation(final String channel, final byte[] bytes)
    {
        final RpcInvokeMessage invokeMessage = this.getApiCore().getMessagePackTemplates().deserialize(RpcInvokeMessage.class, bytes);
        final RpcResponseHandler handler = this.responseHandlerMap.get(invokeMessage.getClassId());
        if (handler != null)
        {
            handler.handleInvoke(invokeMessage);
            return;
        }
        this.sendResponse(invokeMessage.getSender(), invokeMessage.getRequestId(), new RpcExceptionInfo(new RpcUnimplementedException()));
    }

    private void handleResponse(final String channel, final byte[] bytes)
    {
        final RpcResponseMessage responseMessage = this.getApiCore().getMessagePackTemplates().deserialize(RpcResponseMessage.class, bytes);
        final RpcResponseLock lock = this.locks.get(responseMessage.getRequestId());
        if (lock == null)
        {
            this.getApiCore().getLogger().warning("Received RPC response but lock was null. Response:" + responseMessage);
            return; // Moze się wydarzyć, gdy nastąpi timeout i lock zostanie usunięty. W takim wypadku ignorujemy odpowiedź.
        }
        lock.provideResponse(responseMessage.getResponse());
        this.removeLock(responseMessage.getRequestId());
    }
}
