package pl.north93.zgame.api.bukkit.hologui.hologram;

import java.util.List;

public interface IHologramMessage
{
    List<String> render(HologramRenderContext renderContext);

    int hashCode();

    boolean equals(Object obj);
}
