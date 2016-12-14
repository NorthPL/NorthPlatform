package pl.north93.zgame.api.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import pl.north93.zgame.api.global.API;
import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.ICommandsManager;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;

public class BungeeCommandsManager implements ICommandsManager
{
    @Override
    public void registerCommand(final NorthCommand northCommand)
    {
        ProxyServer.getInstance().getPluginManager().registerCommand(((BungeeApiCore) API.getApiCore()).getBungeePlugin(), new WrappedCommand(northCommand));
    }

    private class WrappedCommand extends Command
    {
        private final NorthCommand northCommand;

        public WrappedCommand(final NorthCommand northCommand)
        {
            super(northCommand.getName(), northCommand.getPermission(), northCommand.getAliases().toArray(new String[]{}));
            this.northCommand = northCommand;
        }

        @Override
        public void execute(final CommandSender commandSender, final String[] strings)
        {
            this.northCommand.execute(new WrappedSender(commandSender), new Arguments(strings), ""); // TODO
        }
    }

    private class WrappedSender implements NorthCommandSender
    {
        private final CommandSender sender;

        public WrappedSender(final CommandSender sender)
        {
            this.sender = sender;
        }

        @Override
        public String getName()
        {
            return this.sender.getName();
        }

        @Override
        public void sendMessage(final String message)
        {
            this.sender.sendMessage(TextComponent.fromLegacyText(message));
        }

        @Override
        public boolean isPlayer()
        {
            return this.sender instanceof ProxiedPlayer;
        }

        @Override
        public boolean isConsole()
        {
            return ! this.isPlayer();
        }

        @Override
        public Object unwrapped()
        {
            return this.sender;
        }
    }
}
