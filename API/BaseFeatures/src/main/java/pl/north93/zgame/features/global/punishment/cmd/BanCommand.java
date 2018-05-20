package pl.north93.zgame.features.global.punishment.cmd;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.IPlayersManager;
import pl.north93.zgame.api.global.network.players.Identity;
import pl.north93.zgame.api.global.network.players.PlayerNotFoundException;
import pl.north93.zgame.features.global.punishment.AbstractBan;
import pl.north93.zgame.features.global.punishment.BanService;
import pl.north93.zgame.features.global.punishment.cfg.PredefinedBanCfg;

public class BanCommand extends NorthCommand
{
    @Inject
    private BanService      banService;
    @Inject
    private INetworkManager networkManager;

    public BanCommand()
    {
        super("ban");
        this.setPermission("api.command.ban");
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        final IPlayersManager players = this.networkManager.getPlayers();

        if (args.length() == 1)
        {
            final AbstractBan ban = this.banService.getBan(Identity.create(null, args.asString(0)));
            if (ban == null)
            {
                sender.sendMessage("&cGracz o nicku {0} nie ma bana", args.asString(0));
            }
            else
            {
                final String adminNick = Optional.ofNullable(ban.getAdminId()).flatMap(players::getNickFromUuid).orElse("<SERWER>");
                sender.sendMessage("&cZbanowany {0} przez {1}", ban.getGivenAt(), adminNick);
            }
        }
        else if (args.length() == 2)
        {
            final PredefinedBanCfg config = this.banService.getConfigByName(args.asString(1));
            if (config == null)
            {
                sender.sendMessage("Niepoprawna nazwa bana {0}", args.asString(1));
                return;
            }

            final UUID adminId = this.getAdminId(sender);
            try
            {
                this.banService.createBan(Identity.create(null, args.asString(0)), adminId, config);
                sender.sendMessage("&cUzytkownik zbanowany");

            }
            catch (final PlayerNotFoundException e)
            {
                sender.sendMessage("&cNie ma takiego gracza");
            }
        }
        else
        {
            sender.sendMessage("&c/ban nick powód");
        }
    }

    private UUID getAdminId(final NorthCommandSender sender)
    {
        if (! sender.isPlayer())
        {
            return null;
        }

        return this.networkManager.getPlayers().getUuidFromNick(sender.getName()).orElse(null);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("networkManager", this.networkManager).toString();
    }
}
