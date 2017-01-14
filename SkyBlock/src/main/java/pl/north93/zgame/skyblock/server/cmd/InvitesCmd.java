package pl.north93.zgame.skyblock.server.cmd;

import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.ApiCore;
import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.InjectComponent;
import pl.north93.zgame.api.global.component.annotations.InjectResource;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.IOnlinePlayer;
import pl.north93.zgame.api.global.redis.observable.Value;
import pl.north93.zgame.skyblock.api.IslandRole;
import pl.north93.zgame.skyblock.api.player.SkyPlayer;
import pl.north93.zgame.skyblock.server.SkyBlockServer;

public class InvitesCmd extends NorthCommand
{
    private ApiCore         apiCore;
    @InjectComponent("API.MinecraftNetwork.NetworkManager")
    private INetworkManager networkManager;
    @InjectComponent("SkyBlock.Server")
    private SkyBlockServer  server;
    @InjectResource(bundleName = "SkyBlock")
    private ResourceBundle  messages;

    public InvitesCmd()
    {
        super("invites", "zaproszenia");
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        this.apiCore.getPlatformConnector().runTaskAsynchronously(() ->
        {
            final Value<IOnlinePlayer> onlineSender = this.networkManager.getOnlinePlayer(sender.getName());
            final SkyPlayer skyPlayer = SkyPlayer.get(onlineSender);
            if (! skyPlayer.hasIsland())
            {
                sender.sendMessage(this.messages, "error.you_must_have_island");
                return;
            }

            if (args.length() == 0)
            {
                this.listInvites(sender, skyPlayer);
                if (skyPlayer.getIslandRole().equals(IslandRole.OWNER))
                {
                    sender.sendMessage(this.messages, "cmd.invites.help", label);
                }
            }
            else if (args.length() == 1)
            {
                final String args1 = args.asString(0);
                if (args1.equals("remove") || args1.equals("cofnij") || args1.equals("wyrzuc"))
                {
                    if (skyPlayer.getIslandRole().equals(IslandRole.MEMBER))
                    {
                        sender.sendMessage(this.messages, "error.you_must_be_owner");
                        return;
                    }
                    sender.sendMessage(this.messages, "cmd.invites.help", label);
                }
            }
            else if (args.length() == 2)
            {
                final String args1 = args.asString(0);
                if (args1.equals("remove") || args1.equals("cofnij") || args1.equals("wyrzuc"))
                {
                    if (skyPlayer.getIslandRole().equals(IslandRole.MEMBER))
                    {
                        sender.sendMessage(this.messages, "error.you_must_be_owner");
                        return;
                    }
                    this.server.getSkyBlockManager().leaveIsland(skyPlayer.getIslandId(), sender.getName(), args.asString(1), false);
                }
                else
                {
                    sender.sendMessage(this.messages, "cmd.invites.help", label);
                }
            }
        });
    }

    private void listInvites(final NorthCommandSender sender, final SkyPlayer skyPlayer)
    {
        this.server.getIslandDao().modifyIsland(skyPlayer.getIslandId(), islandData ->
        {
            final String members = islandData.getMembersUuid().stream()
                                             .map(this.networkManager::getNickFromUuid)
                                             .collect(Collectors.joining(", "));

            final String invites = islandData.getInvitations().stream()
                                             .map(this.networkManager::getNickFromUuid)
                                             .collect(Collectors.joining(", "));

            final String empty = this.messages.getString("cmd.invites.empty_list");
            sender.sendMessage(this.messages, "cmd.invites.members", StringUtils.isEmpty(members) ? empty : members);
            sender.sendMessage(this.messages, "cmd.invites.list", StringUtils.isEmpty(invites) ? empty : invites);
        });
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
