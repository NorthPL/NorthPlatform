package pl.arieals.minigame.bedwars.hotbar;

import java.util.Collections;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.arieals.api.minigame.server.MiniGameServer;
import pl.arieals.api.minigame.server.gamehost.GameHostManager;
import pl.north93.zgame.api.bukkit.gui.ClickHandler;
import pl.north93.zgame.api.bukkit.gui.HotbarMenu;
import pl.north93.zgame.api.bukkit.gui.event.HotbarClickEvent;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.messages.Messages;
import pl.north93.zgame.api.global.messages.MessagesBox;

public class SpectatorHotbar extends HotbarMenu
{
    @Inject @Messages("BedWars")
    private static MessagesBox messages;
    @Inject
    private MiniGameServer     miniGameServer;

    public SpectatorHotbar()
    {
        super(messages, "spectator");
    }

    @ClickHandler
    public void kickPlayerToLobby(final HotbarClickEvent event)
    {
        final GameHostManager serverManager = this.miniGameServer.getServerManager();

        final String myHubId = serverManager.getMiniGameConfig().getHubId();
        serverManager.tpToHub(Collections.singleton(event.getWhoClicked()), myHubId);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}