package pl.arieals.lobby.ui;

import pl.north93.zgame.api.bukkit.gui.ClickHandler;
import pl.north93.zgame.api.bukkit.gui.HotbarMenu;
import pl.north93.zgame.api.bukkit.gui.impl.HotbarClickEvent;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.messages.Messages;
import pl.north93.zgame.api.global.messages.MessagesBox;

public class HubHotbar extends HotbarMenu
{
    @Inject @Messages("UserInterface")
    private static MessagesBox messages;

    public HubHotbar()
    {
        super(messages, "hub");
    }

    @ClickHandler
    public void openGamePicker(final HotbarClickEvent event)
    {
        this.getViewers().forEach(viewer -> new HubPickGui().open(viewer));
    }
}