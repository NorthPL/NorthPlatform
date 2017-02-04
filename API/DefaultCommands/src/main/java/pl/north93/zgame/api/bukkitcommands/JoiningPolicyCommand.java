package pl.north93.zgame.api.bukkitcommands;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.InjectComponent;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.JoiningPolicy;

public class JoiningPolicyCommand extends NorthCommand
{
    @InjectComponent("API.MinecraftNetwork.NetworkManager")
    private INetworkManager networkManager;

    public JoiningPolicyCommand()
    {
        super("joiningpolicy", "joinpolicy");
        this.setPermission("api.command.joiningpolicy");
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        if (args.length() == 0)
        {
            final String values = Arrays.stream(JoiningPolicy.values()).map(Enum::name).collect(Collectors.joining(", "));
            sender.sendMessage("&eDostępne wartości: " + values);
            sender.sendMessage("&eAktualnie wybrana opcja: " + this.networkManager.getJoiningPolicy());
        }
        else if (args.length() == 1)
        {
            final JoiningPolicy newJoinPolicy;
            try
            {
                newJoinPolicy = JoiningPolicy.valueOf(args.asString(0).toUpperCase());
            }
            catch (final IllegalArgumentException e)
            {
                sender.sendMessage("&cNiepoprawne argumenty");
                return;
            }

            this.networkManager.getNetworkMeta().update(meta ->
            {
                meta.joiningPolicy = newJoinPolicy;
            });
            sender.sendMessage("&aJoining policy zmienione na " + newJoinPolicy.name());
        }
        else
        {
            sender.sendMessage("&cNiepoprawne argumenty");
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}