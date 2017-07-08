package pl.north93.zgame.api.bukkitcommands;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.exceptions.PlayerNotFoundException;
import pl.north93.zgame.api.global.messages.Messages;
import pl.north93.zgame.api.global.messages.MessagesBox;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.IOnlinePlayer;
import pl.north93.zgame.api.global.network.players.IPlayer;
import pl.north93.zgame.api.global.network.players.IPlayerTransaction;

public class NickCmd extends NorthCommand
{
    @Inject @Messages("Commands")
    private MessagesBox messages;
    @Inject
    private INetworkManager networkManager;

    public NickCmd()
    {
        super("nick");
        this.setPermission("api.command.nick");
        this.setAsync(true);
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        if (args.length() == 0)
        {
            this.printNickInfo(sender, sender.getName());
        }
        else if (args.length() == 1)
        {
            this.printNickInfo(sender, args.asString(0));
        }
        else
        {
            try (final IPlayerTransaction t = this.networkManager.getPlayers().transaction(args.asString(0)))
            {
                if (args.asString(1).equalsIgnoreCase("reset"))
                {
                    t.getPlayer().setDisplayName(null);
                    sender.sendRawMessage("&eZresetowano nazwe wyswietlana dla: " + t.getPlayer().getLatestNick());
                }
                else
                {
                    final String newName = args.asText(1);
                    t.getPlayer().setDisplayName(newName);
                    sender.sendRawMessage("&eNiestandardowa nazwa " + t.getPlayer().getLatestNick() + " zmieniona na: " + newName);
                }
            }
            catch (final PlayerNotFoundException e)
            {
                sender.sendMessage(this.messages, "command.no_player");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void printNickInfo(final NorthCommandSender sender, final String nick)
    {
        try (final IPlayerTransaction t = this.networkManager.getPlayers().transaction(nick))
        {
            final IPlayer player = t.getPlayer();
            if (t.isOnline())
            {
                final IOnlinePlayer onlinePlayer = (IOnlinePlayer) player;
                sender.sendRawMessage("&ePrawdziwy nick: " +  onlinePlayer.getNick() + " (" +  onlinePlayer.getLatestNick() + "&e)");
            }
            else
            {
                sender.sendRawMessage("&ePrawdziwy (ostatni znany) nick: " +  player.getLatestNick());
            }

            if (player.hasDisplayName())
            {
                sender.sendRawMessage("&eNiestandardowa nazwa: " + player.getDisplayName());
            }
            else
            {
                sender.sendRawMessage("&eBrak niestandardowej nazwy.");
            }
        }
        catch (final PlayerNotFoundException e)
        {
            sender.sendMessage(this.messages, "command.no_player");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}