package pl.arieals.minigame.bedwars.listener;

import static pl.arieals.api.minigame.server.gamehost.MiniGameApi.getArena;
import static pl.arieals.api.minigame.server.gamehost.MiniGameApi.getPlayerData;
import static pl.arieals.api.minigame.server.gamehost.MiniGameApi.getPlayerStatus;
import static pl.arieals.api.minigame.server.gamehost.MiniGameApi.setPlayerStatus;
import static pl.north93.zgame.api.global.utils.JavaUtils.instanceOf;


import java.time.Duration;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.arieals.api.minigame.server.gamehost.arena.LocalArena;
import pl.arieals.api.minigame.server.gamehost.reward.CurrencyReward;
import pl.arieals.api.minigame.shared.api.PlayerStatus;
import pl.arieals.api.minigame.shared.api.arena.DeathMatchState;
import pl.arieals.minigame.bedwars.arena.BedWarsPlayer;
import pl.arieals.minigame.bedwars.arena.RevivePlayerCountdown;
import pl.arieals.minigame.bedwars.arena.Team;
import pl.arieals.minigame.bedwars.shop.elimination.IEliminationEffect;
import pl.arieals.minigame.bedwars.shop.stattrack.StatTrackManager;
import pl.arieals.minigame.bedwars.shop.stattrack.TrackedStatistic;
import pl.north93.zgame.api.bukkit.BukkitApiCore;
import pl.north93.zgame.api.bukkit.utils.dmgtracker.DamageContainer;
import pl.north93.zgame.api.bukkit.utils.dmgtracker.DamageEntry;
import pl.north93.zgame.api.bukkit.utils.dmgtracker.DamageTracker;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.messages.Messages;
import pl.north93.zgame.api.global.messages.MessagesBox;
import pl.north93.zgame.api.global.network.players.Identity;

public class DeathListener implements Listener
{
    @Inject
    private BukkitApiCore    apiCore;
    @Inject
    private StatTrackManager statTrackManager;
    @Inject @Messages("BedWars")
    private MessagesBox      messages;

    @EventHandler
    public void onVoidDamage(final EntityDamageEvent event)
    {
        final Player player = instanceOf(event.getEntity(), Player.class);
        if (player == null)
        {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.VOID)
        {
            return;
        }

        final PlayerStatus playerStatus = getPlayerStatus(player);
        if (playerStatus == PlayerStatus.PLAYING)
        {
            event.setDamage(Integer.MAX_VALUE);
        }
    }

    @EventHandler
    public void onPlayerHitPlayer(final EntityDamageByEntityEvent event)
    {
        final Player player = instanceOf(event.getEntity(), Player.class);
        final Player damager = new DamageEntry(event, null).getPlayerDamager(); // pozyczylismy sobie kod z damagetrackera
        if (player == null || damager == null)
        {
            return;
        }

        final BedWarsPlayer playerData = getPlayerData(player, BedWarsPlayer.class);
        final BedWarsPlayer damagerData = getPlayerData(damager, BedWarsPlayer.class);

        if (playerData == null || damagerData == null || playerData.getTeam() == damagerData.getTeam())
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event)
    {
        final Player player = event.getEntity();
        final LocalArena arena = getArena(player);
        final BedWarsPlayer playerData = getPlayerData(player, BedWarsPlayer.class);

        final Team team = playerData.getTeam();
        if (arena == null || team == null)
        {
            return;
        }

        this.apiCore.getLogger().log(Level.INFO, "Player {0} death on arena {1}", new Object[]{player.getName(), arena.getId()});

        player.setHealth(player.getMaxHealth());
        setPlayerStatus(player, PlayerStatus.PLAYING_SPECTATOR);

        // usuwamy wszystkie efekty potionek, upgradey zadbaja zeby je oddac
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        this.handleKiller(event, arena, playerData); // podbija licznik zabojstw, wysyla wiadomosc i daje nagrode zabojcy
        this.handleRespawn(player, arena, playerData); // zmniejsza licznik zycia, uruchamia task respawnujacy, wysyla title
        this.safePlaceTeleport(player, team, arena);

        team.checkEliminated();
    }

    private void handleRespawn(final Player player, final LocalArena arena, final BedWarsPlayer playerData)
    {
        final Team team = playerData.getTeam();
        if (team.isBedAlive())
        {
            new RevivePlayerCountdown(player, playerData).start(20);
            return;
        }
        else
        {
            final DeathMatchState deathMatchState = arena.getDeathMatch().getState();

            // gdy gracz ma zycie i deathmatch nie jest wlaczony to zabieramy zycie i normalnie respawnimy
            if (playerData.getLives() > 0 && deathMatchState == DeathMatchState.NOT_STARTED)
            {
                playerData.removeLife();
                new RevivePlayerCountdown(player, playerData).start(20);
                return;
            }
        }

        playerData.eliminate();
    }

    private void safePlaceTeleport(final Player player, final Team team, final LocalArena arena)
    {
        final EntityDamageEvent lastDamageCause = player.getLastDamageCause();
        if (lastDamageCause != null && lastDamageCause.getCause() == EntityDamageEvent.DamageCause.VOID)
        {
            // jesli gracz umarl w voidzie to teleportujemy go na spawn
            final DeathMatchState deathmatchState = arena.getDeathMatch().getState();
            if (deathmatchState.isStarted())
            {
                player.teleport(arena.getDeathMatch().getArenaSpawn());
            }
            else
            {
                player.teleport(team.getSpawn());
            }
        }
    }

    private void handleKiller(final PlayerDeathEvent event, final LocalArena arena, final BedWarsPlayer deathData)
    {
        event.setDeathMessage(null);

        final Player player = event.getEntity();
        final Team team = deathData.getTeam();

        final DamageContainer dmgContainer = DamageTracker.get().getContainer(player);
        final DamageEntry lastDmg = dmgContainer.getLastDamageByPlayer(Duration.ofSeconds(10));

        if (lastDmg == null)
        {
            return;
        }

        final Player damager = lastDmg.getPlayerDamager();
        assert damager != null; // damager nie moze byc tu nullem bo uzywamy getLastDamageByPlayer

        final BedWarsPlayer damagerData = getPlayerData(damager, BedWarsPlayer.class);
        if (damagerData == null || damagerData.isEliminated())
        {
            // jesli damager jest wyeliminowany to nie uznajemy go za zabojce
            return;
        }

        final IEliminationEffect eliminationEffect = damagerData.getEliminationEffect();
        if (eliminationEffect != null)
        {
            // odtwarzamy animacje zabicia gracza
            eliminationEffect.playerEliminated(player, damager);
        }

        // dodajemy zabojcy killa w systemia stattrak
        this.statTrackManager.bumpStatistic(damager, TrackedStatistic.KILLS, lastDmg.getTool());
        // dodajemy zabojcy killa w obiekcie BedWarsPlayer
        damagerData.incrementKills();
        // dajemy zabojcy nagrode(?) za eliminacje
        arena.getRewards().addReward(Identity.of(damager), new CurrencyReward("elimination", "minigame", 100));

        // jesli gracz ma 0 i mniej zycia, a lozko jest zniszczone to nastapila eliminacja
        final boolean elimination = deathData.getLives() <= 0 && ! team.isBedAlive();
        final String deathMessageKey = this.getDeathMessageKey(player, elimination);

        if (elimination)
        {
            arena.getPlayersManager().broadcast(this.messages, deathMessageKey,
                    team.getColorChar(),
                    player.getDisplayName(),
                    damagerData.getTeam().getColorChar(),
                    damager.getDisplayName());
        }
        else
        {
            arena.getPlayersManager().broadcast(this.messages, deathMessageKey,
                    team.getColorChar(),
                    player.getDisplayName(),
                    damagerData.getTeam().getColorChar(),
                    damager.getDisplayName());
        }
    }

    private String getDeathMessageKey(final Player deathPlayer, final boolean elimination)
    {
        final StringBuilder builder = new StringBuilder("die.broadcast.");

        if (deathPlayer.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID)
        {
            builder.append("fall.");
        }
        else
        {
            builder.append("kill.");
        }

        builder.append(elimination ? "eliminated_by" : "killed_by");

        return builder.toString();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
