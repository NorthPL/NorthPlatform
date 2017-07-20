package pl.north93.zgame.api.bukkit.gui;

import org.bukkit.entity.Player;

public interface IGuiManager
{
    void openGui(Player player, Gui gui);
    
    default void closeGui(Player player)
    {
        player.closeInventory();
    }
    
    <T extends Gui> T getCurrentGui(Player player);
    
    default boolean hasOpenedGui(Player player)
    {
        return getCurrentGui(player) != null;
    }
}