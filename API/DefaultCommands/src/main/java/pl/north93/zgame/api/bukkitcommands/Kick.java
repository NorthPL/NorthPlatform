package pl.north93.zgame.api.bukkitcommands;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import pl.north93.zgame.api.global.API;
import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.InjectResource;
import pl.north93.zgame.api.global.network.NetworkPlayer;

public class Kick extends NorthCommand
{
    @InjectResource(bundleName = "Commands")
    private ResourceBundle messages;

    public Kick()
    {
        super("kick");
        this.setPermission("api.command.kick");
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        if (args.length() < 2)
        {
            sender.sendMessage(this.messages, "command.usage", label, "<nick gracza> <wiadomosc wyrzucenia>");
            return;
        }

        API.getPlatformConnector().runTaskAsynchronously(() ->
        {
            final NetworkPlayer networkPlayer = API.getApiCore().getNetworkManager().getNetworkPlayer(args.asString(0));
            if (networkPlayer == null)
            {
                sender.sendMessage(this.messages, "command.no_player");
                return;
            }

            final String reason = args.asText(1);
            final String kickMessage;
            if (StringUtils.isEmpty(reason))
            {
                kickMessage = this.messages.getString("kick.by_command.without_reason");
            }
            else
            {
                kickMessage = MessageFormat.format(this.messages.getString("kick.by_command.with_reason"), reason);
            }

            networkPlayer.kick(kickMessage);
        });
    }
}
