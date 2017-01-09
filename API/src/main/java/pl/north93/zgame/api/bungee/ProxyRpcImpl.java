package pl.north93.zgame.api.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import pl.north93.zgame.api.global.network.ProxyRpc;
import pl.north93.zgame.api.global.network.server.ServerProxyData;
import pl.north93.zgame.api.global.network.server.joinaction.JoinActionsContainer;

public class ProxyRpcImpl implements ProxyRpc
{
    private final BungeeApiCore apiCore;
    private final ProxyServer proxy = ProxyServer.getInstance();

    public ProxyRpcImpl(final BungeeApiCore apiCore)
    {
        this.apiCore = apiCore;
    }

    @Override
    public boolean isOnline(final String nick)
    {
        return this.proxy.getPlayer(nick) != null;
    }

    @Override
    public void sendMessage(final String nick, final String message)
    {
        this.proxy.getPlayer(nick).sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }

    @Override
    public void kick(final String nick, final String kickMessage)
    {
        this.proxy.getPlayer(nick).disconnect(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', kickMessage)));
    }

    @Override
    public void connectPlayer(final String nick, final String serverName, final JoinActionsContainer actions)
    {
        this.apiCore.getConnectionManager().connectPlayerToServer(this.proxy.getPlayer(nick), serverName, actions);
    }

    @Override
    public void connectPlayerToServersGroup(final String nick, final String serversGroup, final JoinActionsContainer actions)
    {
        this.apiCore.getConnectionManager().connectPlayerToServersGroup(this.proxy.getPlayer(nick), serversGroup, actions);
    }

    @Override
    public void addServer(final ServerProxyData proxyData)
    {
        this.apiCore.getServersManager().addServer(proxyData);
    }

    @Override
    public void removeServer(final String serverName)
    {
        this.apiCore.getServersManager().removeServer(serverName);
    }

    @Override
    public void removeAllServers()
    {
        this.apiCore.getServersManager().removeAllServers();
    }
}
