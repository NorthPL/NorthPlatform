package pl.north93.zgame.skyblock.server.cmd;

import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;

public class IslandCmd extends NorthCommand
{
    public IslandCmd()
    {
        super("island", "is", "wyspa");
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        sender.sendMessage("&f&l> &6/stworz &7tworzy wyspe.");
        sender.sendMessage("&f&l> &6/usun &7usuwa wyspe.");
        sender.sendMessage("&f&l> &6/dom &7przenosi na wyspe.");
        sender.sendMessage("&f&l> &6/ustawdom &7ustawia wyspe.");
        sender.sendMessage("&f&l> &6/zapros nick &7zaprasza gracza.");
        sender.sendMessage("&f&l> &6/zaproszenia &7lista graczy zaproszonych do wyspy.");
        sender.sendMessage("&f&l> &6/opusc &7opuszczasz wyspe jesli byles zaproszony.");
        sender.sendMessage("&f&l> &6/odwiedzanie &7wlacza/wylacza odwiedzanie wyspy.");
        sender.sendMessage("&f&l> &6/odwiedz nick &7odwiedza gracza.");
    }
}