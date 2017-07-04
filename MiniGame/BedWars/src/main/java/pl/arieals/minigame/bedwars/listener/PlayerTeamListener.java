package pl.arieals.minigame.bedwars.listener;

import static pl.arieals.api.minigame.server.gamehost.MiniGameApi.getArena;
import static pl.arieals.api.minigame.server.gamehost.MiniGameApi.getPlayerData;
import static pl.arieals.api.minigame.server.gamehost.MiniGameApi.setPlayerData;


import java.util.Comparator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.arieals.api.minigame.server.gamehost.arena.LocalArena;
import pl.arieals.api.minigame.server.gamehost.event.arena.gamephase.GameStartEvent;
import pl.arieals.api.minigame.server.gamehost.event.player.PlayerJoinArenaEvent;
import pl.arieals.api.minigame.server.gamehost.event.player.PlayerQuitArenaEvent;
import pl.arieals.minigame.bedwars.arena.BedWarsArena;
import pl.arieals.minigame.bedwars.arena.BedWarsPlayer;
import pl.arieals.minigame.bedwars.arena.Team;
import pl.arieals.minigame.bedwars.scoreboard.GameScoreboard;
import pl.arieals.minigame.bedwars.scoreboard.LobbyScoreboard;
import pl.north93.zgame.api.bukkit.scoreboard.IScoreboardManager;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.messages.Messages;
import pl.north93.zgame.api.global.messages.MessagesBox;

public class PlayerTeamListener implements Listener
{
    @Inject
    private IScoreboardManager scoreboardManager;
    @Inject @Messages("BedWars")
    private MessagesBox        messages;

    @EventHandler
    public void playerJoin(final PlayerJoinArenaEvent event)
    {
        final Player player = event.getPlayer();

        final BedWarsArena arenaData = event.getArena().getArenaData();
        final BedWarsPlayer playerData = new BedWarsPlayer(player);
        setPlayerData(player, playerData);

        player.teleport(arenaData.getConfig().getLobby().toBukkit(event.getArena().getWorld().getCurrentWorld()));

        this.scoreboardManager.setLayout(player, new LobbyScoreboard());
    }

    @EventHandler
    public void gameStart(final GameStartEvent event)
    {
        final LocalArena arena = event.getArena();
        final BedWarsArena arenaData = arena.getArenaData();

        for (final Player player : arena.getPlayersManager().getPlayers())
        {
            final Team smallestTeam = arenaData.getTeams().stream().sorted(Comparator.comparing(team -> team.getPlayers().size())).findFirst().orElse(null);
            getPlayerData(player, BedWarsPlayer.class).switchTeam(smallestTeam);

            this.scoreboardManager.setLayout(player, new GameScoreboard());
        }
    }

    @EventHandler
    public void playerLeave(final PlayerQuitArenaEvent event)
    {
        final BedWarsPlayer playerData = getPlayerData(event.getPlayer(), BedWarsPlayer.class);
        if (playerData == null)
        {
            return;
        }

        final Team team = playerData.getTeam();
        if (team != null)
        {
            team.getPlayers().remove(event.getPlayer());
        }
    }

    @EventHandler
    public void chestOpen(final PlayerInteractEvent event)
    {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();

        if (block == null || block.getType() != Material.CHEST)
        {
            return;
        }

        final BedWarsPlayer playerData = getPlayerData(player, BedWarsPlayer.class);
        if (playerData == null || playerData.getTeam() == null)
        {
            event.setCancelled(true);
            return;
        }

        final BedWarsArena arenaData = getArena(player).getArenaData();
        final Team teamAt = arenaData.getTeamAt(block);
        if (teamAt == playerData.getTeam())
        {
            return;
        }

        if (teamAt.isBedAlive() || !teamAt.getAlivePlayers().isEmpty())
        {
            // jesli team ma lozko lub zywych graczy to anulujemy otwarcie skrzynki
            event.setCancelled(true);
            this.messages.sendMessage(player, "chest_blocked", teamAt.getColorChar(), "nazwa druzyny");
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
