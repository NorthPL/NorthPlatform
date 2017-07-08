package pl.arieals.minigame.goldhunter.scoreboard;

import pl.arieals.minigame.goldhunter.GoldHunterPlayer;
import pl.north93.zgame.api.bukkit.scoreboard.IScoreboardContext;
import pl.north93.zgame.api.bukkit.scoreboard.IScoreboardLayout;

public abstract class GoldHunterScoreboardLayout implements IScoreboardLayout
{
    protected final GoldHunterPlayer player;
    
    public GoldHunterScoreboardLayout(GoldHunterPlayer player)
    {
        this.player = player;
    }
    
    @Override
    public String getTitle(IScoreboardContext context)
    {
        return player.getMessage("scoreboard_title");
    }
}