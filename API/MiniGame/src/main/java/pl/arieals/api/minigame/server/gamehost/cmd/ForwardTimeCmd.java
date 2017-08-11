package pl.arieals.api.minigame.server.gamehost.cmd;

import static pl.arieals.api.minigame.server.gamehost.MiniGameApi.getArena;


import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.arieals.api.minigame.server.MiniGameServer;
import pl.arieals.api.minigame.server.gamehost.arena.LocalArena;
import pl.arieals.api.minigame.server.lobby.LobbyManager;
import pl.arieals.api.minigame.shared.api.GamePhase;
import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.messages.Messages;
import pl.north93.zgame.api.global.messages.MessagesBox;

public class ForwardTimeCmd extends NorthCommand
{
    @Inject @Messages("MiniGameApi")
    private MessagesBox    messages;
    @Inject
    private MiniGameServer server;

    public ForwardTimeCmd()
    {
        super("forwardtime");
        this.setPermission("dev");
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        if (this.server.getServerManager() instanceof LobbyManager)
        {
            sender.sendMessage(this.messages, "cmd.general.only_gamehost");
            return;
        }

        final Player player = (Player) sender.unwrapped();
        final LocalArena arena = getArena(player);

        if (arena.getGamePhase() != GamePhase.STARTED)
        {
            sender.sendRawMessage("&cTa komenda dziala tylko gdy arena jest w gamephase STARTED.");
            return;
        }

        if (args.length() == 1)
        {
            final Long seconds = args.asLong(0);

            sender.sendRawMessage("&aAktualny czas areny: &e{0}&a. Dodawanie &e{1} &asekund.", arena.getTimer(), seconds);
            arena.forwardTime(seconds, TimeUnit.SECONDS);
            sender.sendRawMessage("&aZakonczono symulacje. Nowy czas areny: &e{0}", arena.getTimer());

        }
        else
        {
            sender.sendRawMessage("&a/forwardtime <czas w sekundach>");
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
