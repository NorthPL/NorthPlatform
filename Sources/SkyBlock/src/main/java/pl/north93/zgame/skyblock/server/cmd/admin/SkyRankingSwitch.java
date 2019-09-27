package pl.north93.zgame.skyblock.server.cmd.admin;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.IPlayerTransaction;
import pl.north93.zgame.skyblock.server.SkyBlockServer;
import pl.north93.zgame.skyblock.shared.api.player.SkyPlayer;

public class SkyRankingSwitch extends NorthCommand
{
    @Inject
    private     INetworkManager networkManager;
    @Inject
    private     SkyBlockServer  server;

    public SkyRankingSwitch()
    {
        super("skyrankingswitch");
        this.setPermission("skyblock.admin");
        this.setAsync(true);
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        if (args.length() != 1)
        {
            sender.sendRawMessage("&c/skyrankingswitch <nick> - wlacza/wylacza ranking dla wyspy danego usera");
            return;
        }

        final String targetNick = args.asString(0);
        try (final IPlayerTransaction t = this.networkManager.getPlayers().transaction(targetNick))
        {
            final SkyPlayer skyPlayer = SkyPlayer.get(t.getPlayer());

            if (! skyPlayer.hasIsland())
            {
                sender.sendRawMessage("&cUzytkownik " + targetNick + " nie ma wyspy!");
                return;
            }

            final Boolean newRanking = ! this.server.getIslandDao().getIsland(skyPlayer.getIslandId()).getShowInRanking();
            this.server.getSkyBlockManager().setShowInRanking(skyPlayer.getIslandId(), newRanking);
            if (newRanking)
            {
                sender.sendRawMessage("&cWyspa uzytkownika " + targetNick + " zostala dodana do rankingu.");
            }
            else
            {
                sender.sendRawMessage("&cWyspa uzytkownika " + targetNick + " zostala usunieta z rankingu.");
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("networkManager", this.networkManager).append("server", this.server).toString();
    }
}