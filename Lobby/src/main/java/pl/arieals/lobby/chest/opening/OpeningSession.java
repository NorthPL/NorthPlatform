package pl.arieals.lobby.chest.opening;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.arieals.api.minigame.server.lobby.hub.HubWorld;
import pl.arieals.lobby.chest.loot.LootResult;

class OpeningSession implements IOpeningSession
{
    private final Player           player;
    private final HubWorld         hub;
    private final HubOpeningConfig openingConfig;
    private CompletableFuture<LootResult> lastResults;

    public OpeningSession(final Player player, final HubWorld hub, final HubOpeningConfig openingConfig)
    {
        this.player = player;
        this.hub = hub;
        this.openingConfig = openingConfig;
    }

    @Override
    public Player getPlayer()
    {
        return this.player;
    }

    @Override
    public HubWorld getHub()
    {
        return this.hub;
    }

    @Override
    public Location getPlayerLocation()
    {
        return this.openingConfig.getPlayerLocation().toBukkit(this.hub.getBukkitWorld());
    }

    @Override
    public HubOpeningConfig getConfig()
    {
        return this.openingConfig;
    }

    public CompletableFuture<LootResult> getLastResults()
    {
        return this.lastResults;
    }

    public void setLastResults(final CompletableFuture<LootResult> lastResults)
    {
        this.lastResults = lastResults;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("player", this.player).append("hub", this.hub).append("openingConfig", this.openingConfig).append("lastResults", this.lastResults).toString();
    }
}
