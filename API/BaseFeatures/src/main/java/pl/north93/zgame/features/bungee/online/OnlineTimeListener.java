package pl.north93.zgame.features.bungee.online;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.north93.zgame.api.bungee.BungeeApiCore;
import pl.north93.zgame.api.global.component.annotations.bean.Bean;
import pl.north93.zgame.api.global.network.players.Identity;
import pl.north93.zgame.features.global.online.impl.OnlineTimeServiceImpl;

public class OnlineTimeListener implements Listener
{
    private final OnlineTimeServiceImpl onlineTimeService;

    @Bean
    private OnlineTimeListener(final BungeeApiCore apiCore, final OnlineTimeServiceImpl onlineTimeService)
    {
        this.onlineTimeService = onlineTimeService;
        apiCore.registerListeners(this);
    }

    @EventHandler
    public void startTracingOnlineTime(final PostLoginEvent event)
    {
        final Identity identity = Identity.of(event.getPlayer());
        this.onlineTimeService.startTrackingOnlineTime(identity);
    }

    @EventHandler
    public void endTracingOnlineTime(final PlayerDisconnectEvent event)
    {
        final Identity identity = Identity.of(event.getPlayer());
        this.onlineTimeService.endTrackingOnlineTime(identity);
    }
}
