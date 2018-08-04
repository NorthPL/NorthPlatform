package pl.north93.zgame.api.global.network.impl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.component.Component;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.config.IConfig;
import pl.north93.zgame.api.global.config.NetConfig;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.NetworkControllerRpc;
import pl.north93.zgame.api.global.network.NetworkMeta;
import pl.north93.zgame.api.global.network.event.NetworkShutdownNetEvent;
import pl.north93.zgame.api.global.network.players.IPlayersManager;
import pl.north93.zgame.api.global.redis.event.NetEventSubscriber;
import pl.north93.zgame.api.global.redis.observable.IObservationManager;
import pl.north93.zgame.api.global.redis.rpc.IRpcManager;
import pl.north93.zgame.api.global.redis.rpc.Targets;

class NetworkManager extends Component implements INetworkManager
{
    @Inject
    private IObservationManager  observationManager;
    @Inject
    private IRpcManager          rpcManager;
    @Inject
    private IPlayersManager      playersManager;
    @Inject @NetConfig(type = NetworkMeta.class, id = "networkMeta")
    private IConfig<NetworkMeta> networkConfig;
    private ProxiesManagerImpl   proxiesManager;
    private DaemonsManagerImpl   daemonsManager;
    private ServersManagerImpl   serversManager;

    @Override
    protected void enableComponent()
    {
        this.proxiesManager = new ProxiesManagerImpl(this.rpcManager, this.observationManager);
        this.daemonsManager = new DaemonsManagerImpl(this.rpcManager, this.observationManager);
        this.serversManager = new ServersManagerImpl(this.rpcManager, this.observationManager);
    }

    @Override
    protected void disableComponent()
    {
    }

    @Override
    public IConfig<NetworkMeta> getNetworkConfig()
    {
        return this.networkConfig;
    }

    /**
     * Zwraca instancję zdalnego wywoływania procedur do kontrolera sieci.
     *
     * @return NetworkControllerRpc
     */
    @Override
    public NetworkControllerRpc getNetworkController()
    {
        return this.rpcManager.createRpcProxy(NetworkControllerRpc.class, Targets.networkController());
    }

    @Override
    public ProxiesManagerImpl getProxies()
    {
        return this.proxiesManager;
    }

    @Override
    public DaemonsManagerImpl getDaemons()
    {
        return this.daemonsManager;
    }

    @Override
    public IPlayersManager getPlayers()
    {
        return this.playersManager;
    }

    @Override
    public ServersManagerImpl getServers()
    {
        return this.serversManager;
    }

    @NetEventSubscriber(NetworkShutdownNetEvent.class)
    public void onNetShutdownEvent(final NetworkShutdownNetEvent event) // nasluchuje na event wylaczenia sieci
    {
        this.getApiCore().getPlatformConnector().stop();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
