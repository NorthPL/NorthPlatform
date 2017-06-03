package pl.north93.zgame.skyblock.server.cmd;

import java.util.UUID;

import pl.north93.zgame.api.global.ApiCore;
import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.InjectComponent;
import pl.north93.zgame.api.global.component.annotations.InjectMessages;
import pl.north93.zgame.api.global.messages.MessagesBox;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.IOfflinePlayer;
import pl.north93.zgame.api.global.network.players.IOnlinePlayer;
import pl.north93.zgame.api.global.redis.observable.Value;
import pl.north93.zgame.skyblock.server.SkyBlockServer;
import pl.north93.zgame.skyblock.shared.api.player.SkyPlayer;

public class AcceptCmd extends NorthCommand
{
    private ApiCore         apiCore;
    @Inject
    private INetworkManager networkManager;
    @Inject
    private SkyBlockServer  server;
    @Inject @Messages("SkyBlock")
    private MessagesBox     messages;

    public AcceptCmd()
    {
        super("accept", "akceptuj", "zaakceptuj");
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        if (args.length() != 1)
        {
            sender.sendMessage(this.messages, "cmd.accept.args");
            return;
        }

        this.apiCore.getPlatformConnector().runTaskAsynchronously(() ->
        {
            final UUID islandId = this.islandIdFromOwnerName(args.asString(0));
            if (islandId != null)
            {
                this.server.getSkyBlockManager().invitationAccepted(islandId, sender.getName());
            }
            else
            {
                sender.sendMessage(this.messages, "cmd.accept.no_invite");
            }
        });
    }

    private UUID islandIdFromOwnerName(final String owner)
    {
        final Value<IOnlinePlayer> onlinePlayer = this.networkManager.getOnlinePlayer(owner);
        if (onlinePlayer.isAvailable())
        {
            return SkyPlayer.get(onlinePlayer).getIslandId();
        }

        final IOfflinePlayer offlinePlayer = this.networkManager.getOfflinePlayer(owner);
        if (offlinePlayer != null)
        {
            return SkyPlayer.get(offlinePlayer).getIslandId();
        }

        return null;
    }
}
