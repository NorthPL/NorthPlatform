package pl.arieals.api.minigame.server.gamehost.listener;

import static org.bukkit.event.EventPriority.MONITOR;


import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.arieals.api.minigame.server.gamehost.arena.LocalArena;
import pl.arieals.api.minigame.server.gamehost.event.arena.gamephase.GameEndEvent;
import pl.arieals.api.minigame.server.gamehost.event.player.PlayerQuitArenaEvent;
import pl.arieals.api.minigame.server.gamehost.event.player.SpectatorQuitEvent;
import pl.arieals.api.minigame.shared.api.GamePhase;
import pl.arieals.api.minigame.shared.api.match.IMatchAccess;
import pl.arieals.api.minigame.shared.api.match.StandardMatchStatistics;
import pl.arieals.api.minigame.shared.api.statistics.unit.DurationUnit;
import pl.north93.zgame.api.bukkit.server.IBukkitExecutor;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;

public class ArenaEndListener implements Listener
{
    @Inject
    private Logger          logger;
    @Inject
    private IBukkitExecutor bukkitExecutor;

    @EventHandler(priority = MONITOR)
    public void stopEmptyArenaWhenPlayerQuit(final PlayerQuitArenaEvent event)
    {
        this.checkArenaShouldEnd(event.getArena());
    }

    @EventHandler(priority = MONITOR)
    public void stopEmptyArenaWhenSpectatorQuit(final SpectatorQuitEvent event)
    {
        this.checkArenaShouldEnd(event.getArena());
    }

    private void checkArenaShouldEnd(final LocalArena arena)
    {
        final List<Player> allPlayers = arena.getPlayersManager().getAllPlayers();
        if (! allPlayers.isEmpty())
        {
            return;
        }

        final GamePhase gamePhase = arena.getGamePhase();
        if (gamePhase == GamePhase.INITIALISING || gamePhase == GamePhase.LOBBY)
        {
            // W initialising nic nie robimy zeby nie zbugowac
            // W lobby nie trzeba nic robic
            return;
        }

        // jak arena byla w trakcie gry lub po grze to przelaczamy do ponownej inicjalizacji
        this.logger.log(Level.INFO, "Arena {0} is empty, switching to INITIALISING...", arena.getId());
        this.bukkitExecutor.sync(() ->
        {
            if (gamePhase == GamePhase.STARTED)
            {
                arena.endGame();
            }
            else if (gamePhase == GamePhase.POST_GAME)
            {
                // nie wywolujemy prepareNewCycle() bo ono odpowiada tylko za rozlaczenie graczy
                arena.setGamePhase(GamePhase.INITIALISING);
            }
        });
    }

    @EventHandler
    public void markMatchAsEnded(final GameEndEvent event)
    {
        final IMatchAccess match = event.getArena().getMatch();
        if (match == null)
        {
            this.logger.log(Level.WARNING, "Match is null on arena {0} when switched to post_game", event.getArena().getId());
            return;
        }

        // operacje na bazie danych mongo
        this.bukkitExecutor.async(() ->
        {
            // oznaczamy mecz jako zakonczony
            match.endMatch();

            final Duration matchDuration = Duration.between(match.getStartedAt(), match.getEndedAt());
            match.getStatistics().record(StandardMatchStatistics.MATCH_DURATION, new DurationUnit(matchDuration));

            System.out.println("Match duration: " + matchDuration);
        });
    }

    @EventHandler
    public void switchToInitialisingWhenEndEmpty(final GameEndEvent event)
    {
        final LocalArena arena = event.getArena();
        if (! arena.getPlayersManager().getPlayers().isEmpty())
        {
            return;
        }

        this.logger.log(Level.INFO, "Arena {0} has been switched to POST_GAME without players, switching to INITIALISING", arena.getId());
        this.bukkitExecutor.sync(() ->
        {
            // kiedy cos przelaczylo pusta arene do POST_GAME to natychmiast przerzucamy do INITIALISING
            // dopiero przy nastepnym ticku zeby nie zepsuc innej logiki w minigrach
            arena.setGamePhase(GamePhase.INITIALISING);
        });
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
