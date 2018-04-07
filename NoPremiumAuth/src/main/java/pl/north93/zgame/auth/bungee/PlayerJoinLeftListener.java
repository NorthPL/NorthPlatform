package pl.north93.zgame.auth.bungee;

import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.north93.zgame.api.bungee.proxy.event.HandlePlayerProxyQuitEvent;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.IOnlinePlayer;

public class PlayerJoinLeftListener implements Listener
{
    @Inject
    private Logger             logger;
    @Inject
    private INetworkManager    networkManager;
    @Inject
    private AuthProxyComponent authProxy;

    @EventHandler
    public void onPlayerJoin(final PostLoginEvent event)
    {
        final IOnlinePlayer iOnlinePlayer = this.networkManager.getPlayers().unsafe().getOnline(event.getPlayer().getName()).get();
        if (iOnlinePlayer == null)
        {
            this.logger.warning("iOnlinePlayer == null in onPlayerJoin (NoPremiumAuth)");
            return;
        }
        if (iOnlinePlayer.isPremium())
        {
            this.authProxy.getAuthManager().setLoggedInStatus(iOnlinePlayer.getUuid(), true);
        }
    }

    @EventHandler
    public void onPlayerQuit(final HandlePlayerProxyQuitEvent event)
    {
        final UUID playerId = event.getProxiedPlayer().getUniqueId();
        this.authProxy.getAuthManager().deleteStatus(playerId);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
