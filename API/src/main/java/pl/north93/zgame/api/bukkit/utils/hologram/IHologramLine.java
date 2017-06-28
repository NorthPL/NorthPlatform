package pl.north93.zgame.api.bukkit.utils.hologram;

import org.bukkit.entity.Player;

public interface IHologramLine
{
    String render(IHologram hologram, Player player);

    int hashCode();

    boolean equals(Object obj);
}
