package pl.north93.zgame.auth.server.cmd;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.extern.slf4j.Slf4j;
import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.messages.Messages;
import pl.north93.zgame.api.global.messages.MessagesBox;
import pl.north93.zgame.api.global.network.players.Identity;
import pl.north93.zgame.auth.api.IAuthManager;
import pl.north93.zgame.auth.api.IAuthPlayer;
import pl.north93.zgame.auth.server.event.PlayerSuccessfullyAuthEvent;

@Slf4j
public class RegisterCommand extends NorthCommand
{
    @Inject @Messages("NoPremiumAuth")
    private MessagesBox  messages;
    @Inject
    private IAuthManager authManager;

    public RegisterCommand()
    {
        super("register", "zarejestruj");
        this.setAsync(true);
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        final Player player = (Player) sender.unwrapped();
        if (this.authManager.isLoggedIn(player.getName()))
        {
            sender.sendMessage(this.messages, "error.already_logged_in");
            return;
        }

        final IAuthPlayer authPlayer = this.authManager.getPlayer(Identity.of(player));
        if (authPlayer.isRegistered())
        {
            sender.sendMessage(this.messages, "error.already_registered");
            return;
        }

        if (args.length() != 1)
        {
            sender.sendMessage(this.messages, "cmd.login.args", label);
            return;
        }

        authPlayer.setPassword(args.asString(0));
        this.authManager.setLoggedInStatus(Identity.of(player), true);
        log.info("User {} successfully registered! (no-premium password)", player.getName());
        sender.sendMessage(this.messages, "info.successfully_registered");
        Bukkit.getPluginManager().callEvent(new PlayerSuccessfullyAuthEvent(player)); // todo zrobić fasadę na Bukkitową część API
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
