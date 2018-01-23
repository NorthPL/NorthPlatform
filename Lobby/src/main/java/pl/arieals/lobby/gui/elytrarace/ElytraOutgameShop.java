package pl.arieals.lobby.gui.elytrarace;

import org.bukkit.entity.Player;

import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;

public class ElytraOutgameShop extends NorthCommand
{
    public ElytraOutgameShop()
    {
        super("elytraoutgame");
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        final Player player = (Player) sender.unwrapped();

        final ElytraShopMain elytraShopMain = new ElytraShopMain(player);
        elytraShopMain.open(player);
    }
}