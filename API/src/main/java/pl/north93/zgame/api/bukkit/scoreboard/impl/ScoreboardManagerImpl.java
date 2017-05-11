package pl.north93.zgame.api.bukkit.scoreboard.impl;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.bukkit.BukkitApiCore;
import pl.north93.zgame.api.bukkit.scoreboard.IScoreboardContext;
import pl.north93.zgame.api.bukkit.scoreboard.IScoreboardLayout;
import pl.north93.zgame.api.bukkit.scoreboard.IScoreboardManager;
import pl.north93.zgame.api.global.component.Component;

public class ScoreboardManagerImpl extends Component implements IScoreboardManager
{
    private BukkitApiCore apiCore;

    @Override
    public IScoreboardContext setLayout(final Player player, final IScoreboardLayout layout)
    {
        final ScoreboardContextImpl context = new ScoreboardContextImpl(player, layout);
        this.setContext(player, context);
        return context;
    }

    @Override
    public ScoreboardContextImpl getContext(final Player player)
    {
        final List<MetadataValue> metadata = player.getMetadata("scoreboard_context");
        if (metadata.isEmpty())
        {
            return null;
        }
        return (ScoreboardContextImpl) metadata.get(0).value();
    }

    private void setContext(final Player player, final ScoreboardContextImpl scoreboardContext)
    {
        final ScoreboardContextImpl old = this.getContext(player);
        if (old != null)
        {
            old.cleanup();
        }
        player.setMetadata("scoreboard_context", new FixedMetadataValue(this.apiCore.getPluginMain(), scoreboardContext));
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        scoreboardContext.update();
    }

    @Override
    protected void enableComponent()
    {
    }

    @Override
    protected void disableComponent()
    {
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}