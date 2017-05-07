package pl.arieals.api.minigame.server.gamehost.event.arena;

import org.bukkit.event.HandlerList;

import pl.arieals.api.minigame.server.gamehost.arena.LocalArena;
import pl.arieals.api.minigame.shared.api.GameMap;

public class MapSwitchedEvent extends ArenaEvent
{
    private static final HandlerList handlers = new HandlerList();

    public MapSwitchedEvent(final LocalArena arena)
    {
        super(arena);
    }

    public GameMap getGameMap()
    {
        return this.arena.getWorld().getActiveMap();
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}