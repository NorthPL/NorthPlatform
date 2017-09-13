package pl.north93.zgame.api.bungee.proxy.impl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import net.md_5.bungee.api.ProxyServer;
import pl.north93.zgame.api.bungee.BungeeApiCore;
import pl.north93.zgame.api.bungee.proxy.IProxyServerList;
import pl.north93.zgame.api.bungee.proxy.IProxyServerManager;
import pl.north93.zgame.api.global.component.Component;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.proxy.IProxyRpc;
import pl.north93.zgame.api.global.network.proxy.ProxyDto;
import pl.north93.zgame.api.global.redis.observable.Hash;
import pl.north93.zgame.api.global.redis.rpc.IRpcManager;

class ProxyServerManagerImpl extends Component implements IProxyServerManager
{
    private static final int UPDATE_PROXY_DATA_EVERY = 20;
    @Inject
    private BungeeApiCore       apiCore;
    @Inject
    private IRpcManager         rpcManager;
    @Inject
    private INetworkManager     networkManager;
    private ProxyServerListImpl proxyServerList;

    @Override
    protected void enableComponent()
    {
        this.proxyServerList = new ProxyServerListImpl();
        this.proxyServerList.synchronizeServers();

        this.rpcManager.addRpcImplementation(IProxyRpc.class, new ProxyRpcImpl());

        // rejestrujemy nasze listenery
        this.apiCore.registerListeners(new PingListener(), new PlayerListener(), new PermissionsListener());

        this.uploadInfo();
        this.getApiCore().getPlatformConnector().runTaskAsynchronously(this::uploadInfo, UPDATE_PROXY_DATA_EVERY);
    }

    @Override
    protected void disableComponent()
    {
        final Hash<ProxyDto> hash = this.networkManager.getProxies().unsafe().getHash();
        hash.delete(this.getApiCore().getId());
    }

    @Override
    public IProxyServerList getServerList()
    {
        return this.proxyServerList;
    }

    private void uploadInfo()
    {
        final Hash<ProxyDto> hash = this.networkManager.getProxies().unsafe().getHash();
        hash.put(this.getApiCore().getId(), this.generateInfo());
    }

    private ProxyDto generateInfo()
    {
        final ProxyDto proxyInstanceInfo = new ProxyDto();

        final BungeeApiCore apiCore = (BungeeApiCore) this.getApiCore();

        proxyInstanceInfo.setId(apiCore.getProxyConfig().getUniqueName());
        proxyInstanceInfo.setHostname(apiCore.getHostName());
        proxyInstanceInfo.setOnlinePlayers(ProxyServer.getInstance().getOnlineCount());

        return proxyInstanceInfo;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
