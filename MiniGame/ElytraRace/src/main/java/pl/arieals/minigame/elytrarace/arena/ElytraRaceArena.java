package pl.arieals.minigame.elytrarace.arena;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import pl.arieals.api.minigame.server.gamehost.arena.IArenaData;
import pl.arieals.minigame.elytrarace.ElytraRaceMode;
import pl.arieals.minigame.elytrarace.cfg.ArenaConfig;

public class ElytraRaceArena implements IArenaData
{
    private final ArenaConfig        arenaConfig;
    private final ElytraRaceMode     gameMode;
    private       boolean            isStarted; // czy odliczanie do startu dobieglo konca
    private       int                place; // uzywane w RACE_MODE do okreslania miejsca gracza
    private       Map<UUID, Integer> points; // uzywane w SCORE_MODE do przyznawania nagrod nawet gdy gracz wyjdzie

    public ElytraRaceArena(final ArenaConfig arenaConfig, final ElytraRaceMode gameMode)
    {
        this.arenaConfig = arenaConfig;
        this.gameMode = gameMode;
        if (gameMode == ElytraRaceMode.SCORE_MODE)
        {
            this.points = new HashMap<>();
        }
    }

    public ArenaConfig getArenaConfig()
    {
        return this.arenaConfig;
    }

    public ElytraRaceMode getGameMode()
    {
        return this.gameMode;
    }

    public boolean isStarted()
    {
        return this.isStarted;
    }

    public void setStarted(final boolean started)
    {
        this.isStarted = started;
    }

    public int getPlace()
    {
        return this.place;
    }

    public void setPlace(final int place)
    {
        this.place = place;
    }

    public Map<UUID, Integer> getPoints()
    {
        return this.points;
    }
}