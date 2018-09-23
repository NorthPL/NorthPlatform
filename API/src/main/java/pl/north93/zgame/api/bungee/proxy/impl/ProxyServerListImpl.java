package pl.north93.zgame.api.bungee.proxy.impl;

import static java.util.Collections.synchronizedMap;


import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.north93.zgame.api.bungee.proxy.IProxyServerList;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.server.Server;
import pl.north93.zgame.api.global.network.server.ServerProxyData;

@Slf4j
class ProxyServerListImpl implements IProxyServerList
{
    private final ProxyServer             proxyServer;
    private final Map<String, ServerInfo> servers;
    @Inject
    private INetworkManager networkManager;

    public ProxyServerListImpl()
    {
        this.proxyServer = ProxyServer.getInstance();
        this.servers = synchronizedMap(this.proxyServer.getConfig().getServers());
    }

    @Override
    public void synchronizeServers()
    {
        log.info("Adding all servers actually existing in network...");
        this.removeAllServers();
        for (final Server server : this.networkManager.getServers().all())
        {
            this.addServer(server);
        }
    }

    @Override
    public void addServer(final ServerProxyData proxyData)
    {
        final String name = proxyData.getProxyName();
        final InetSocketAddress address = new InetSocketAddress(proxyData.getConnectHost(), proxyData.getConnectPort());

        final ServerInfo serverInfo = this.proxyServer.constructServerInfo(name, address, name, false);

        this.servers.put(name, serverInfo);
    }

    @Override
    public void removeServer(final ServerProxyData proxyData)
    {
        final String proxyName = proxyData.getProxyName();

        final ServerInfo serverInfo = this.servers.get(proxyName);
        if (serverInfo == null)
        {
            // z jakiegos powodu serwer juz nie istnieje, zabezpieczenie przed ewentualnym NPE
            log.warn("Tried to removeServer({}), but server doesnt exist", proxyData.getProxyName());
            return;
        }

        serverInfo.getPlayers().forEach(ProxiedPlayer::disconnect);
        this.servers.remove(proxyName);
    }

    @Override
    public void removeAllServers()
    {
        final Iterator<ServerInfo> iterator = this.servers.values().iterator();
        while (iterator.hasNext())
        {
            final ServerInfo server = iterator.next();
            server.getPlayers().forEach(ProxiedPlayer::disconnect);
            iterator.remove();
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
