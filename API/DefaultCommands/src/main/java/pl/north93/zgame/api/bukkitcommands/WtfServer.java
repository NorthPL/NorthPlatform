package pl.north93.zgame.api.bukkitcommands;

import java.util.Optional;

import pl.north93.zgame.api.bukkit.BukkitApiCore;
import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.deployment.ServersGroup;
import pl.north93.zgame.api.global.network.server.Server;

public class WtfServer extends NorthCommand
{
    private BukkitApiCore api;

    public WtfServer()
    {
        super("wtfserver");
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        final Server server = this.api.getServer();
        sender.sendMessage("ID serwera: " + this.api.getId());
        sender.sendMessage("Nazwa w proxy: " + server.getProxyName());
        sender.sendMessage("Typ serwera: " + server.getType());
        sender.sendMessage("Czy uruchomiony przez demona: " + (server.isLaunchedViaDaemon() ? "tak" : "nie"));
        sender.sendMessage("Stan serwera: " + server.getServerState());
        final Optional<ServersGroup> serversGroup = server.getServersGroup();
        sender.sendMessage("Grupa serwerów: " + (serversGroup.isPresent() ? serversGroup.get().getName() : "brak"));
    }
}
