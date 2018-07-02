package pl.arieals.minigame.bedwars.event;

import org.bukkit.event.HandlerList;

import pl.arieals.api.minigame.server.gamehost.arena.LocalArena;
import pl.arieals.minigame.bedwars.arena.BedWarsPlayer;

/**
 * Event wywolywany gdy gracz zostaje wyelininowany, tzn przegrywa gre.
 * Moze zostac wywolany takze gdy gracz jest offline (timeout gdy ma lozko)
 */
public class PlayerEliminatedEvent extends BedWarsPlayerArenaEvent
{
    private static final HandlerList handlers = new HandlerList();

    public PlayerEliminatedEvent(final LocalArena arena, final BedWarsPlayer bedWarsPlayer)
    {
        super(arena, bedWarsPlayer);
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
