package pl.north93.zgame.features.bukkit.tp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.ApiCore;
import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.IOnlinePlayer;
import pl.north93.zgame.api.global.network.server.Server;

public class TpHereCommand extends NorthCommand
{
    @Inject
    private ApiCore         apiCore;
    @Inject
    private INetworkManager networkManager;

    public TpHereCommand()
    {
        super("tphere", "s");
        this.setPermission("api.command.tp");
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        if (args.length() != 1)
        {
            sender.sendMessage("&c/tphere <nick>");
            return;
        }

        final String origin = args.asString(0);
        final Player originBukkitPlayer = Bukkit.getPlayer(origin);
        if (originBukkitPlayer != null)
        {
            originBukkitPlayer.teleport(((Player) sender.unwrapped()).getLocation(), TeleportCause.COMMAND);
            return;
        }

        this.apiCore.getPlatformConnector().runTaskAsynchronously(() ->
        {
            final IOnlinePlayer playerSender = this.networkManager.getPlayers().unsafe().getOnlineValue(sender.getName()).get();
            final IOnlinePlayer player = this.networkManager.getPlayers().unsafe().getOnlineValue(origin).get();
            if (player == null || playerSender == null)
            {
                sender.sendMessage("&cGracz jest offline");
                return;
            }

            final Server destinationServer = this.networkManager.getServers().withUuid(playerSender.getServerId());
            player.connectTo(destinationServer, new TeleportToPlayer(playerSender.getUuid(), true));
        });
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
