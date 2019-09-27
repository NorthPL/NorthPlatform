package pl.north93.zgame.skyblock.server.cmd.admin;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.exceptions.PlayerNotFoundException;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.IPlayerTransaction;
import pl.north93.zgame.skyblock.server.SkyBlockServer;
import pl.north93.zgame.skyblock.shared.api.player.SkyPlayer;

public class SkyDelete extends NorthCommand
{
    @Inject
    private INetworkManager networkManager;
    @Inject
    private SkyBlockServer  server;

    public SkyDelete()
    {
        super("skydelete");
        this.setPermission("skyblock.admin");
        this.setAsync(true);
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        if (args.length() != 1)
        {
            sender.sendRawMessage("&cTa komenda USUNIE wyspe podanego gracza. Wpisz /skydelete nick.");
            return;
        }

        String target = args.asString(0);
        try (final IPlayerTransaction t = this.networkManager.getPlayers().transaction(target))
        {
            final SkyPlayer skyPlayer = SkyPlayer.get(t.getPlayer());
            if (!skyPlayer.hasIsland()) {
                sender.sendRawMessage("&Gracz " + target + " nie ma wyspy!");
                return;
            }


            this.server.getSkyBlockManager().deleteIsland(skyPlayer.getIslandId());
            sender.sendRawMessage("&aUsunieto wyspe gracza " + target);
        }
        catch (final PlayerNotFoundException e)
        {
            sender.sendRawMessage("&cGracz " + target + " nie istnieje!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}