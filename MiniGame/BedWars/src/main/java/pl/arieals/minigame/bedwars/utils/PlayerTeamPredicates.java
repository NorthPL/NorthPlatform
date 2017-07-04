package pl.arieals.minigame.bedwars.utils;

import static pl.arieals.api.minigame.server.gamehost.MiniGameApi.getPlayerData;


import java.util.function.Predicate;

import org.bukkit.entity.Player;

import pl.arieals.minigame.bedwars.arena.BedWarsPlayer;
import pl.arieals.minigame.bedwars.arena.Team;

public final class PlayerTeamPredicates
{
    private PlayerTeamPredicates()
    {
    }

    public static Predicate<Player> isInTeam(final Team team)
    {
        return player -> getPlayerData(player, BedWarsPlayer.class).getTeam() == team;
    }

    public static Predicate<Player> notInTeam(final Team team)
    {
        return player -> getPlayerData(player, BedWarsPlayer.class).getTeam() != team;
    }
}
