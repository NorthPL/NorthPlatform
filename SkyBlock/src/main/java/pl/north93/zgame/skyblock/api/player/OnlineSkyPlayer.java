package pl.north93.zgame.skyblock.api.player;

import static pl.north93.zgame.skyblock.api.IslandRole.OWNER;


import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.metadata.MetaStore;
import pl.north93.zgame.api.global.network.players.IOnlinePlayer;
import pl.north93.zgame.api.global.redis.observable.Value;
import pl.north93.zgame.skyblock.api.IslandRole;

class OnlineSkyPlayer extends SkyPlayer
{
    private final Value<IOnlinePlayer> networkPlayer;

    public OnlineSkyPlayer(final Value<IOnlinePlayer> networkPlayer)
    {
        this.networkPlayer = networkPlayer;
    }

    @Override
    public boolean hasIsland()
    {
        final MetaStore metaStore = this.networkPlayer.get().getMetaStore();
        return metaStore.contains(PLAYER_HAS_ISLAND) && metaStore.getBoolean(PLAYER_HAS_ISLAND);
    }

    @Override
    public UUID getIslandId()
    {
        final MetaStore metaStore = this.networkPlayer.get().getMetaStore();
        if (! metaStore.contains(PLAYER_ISLAND_ID))
        {
            return null;
        }
        return metaStore.getUuid(PLAYER_ISLAND_ID);
    }

    @Override
    public IslandRole getIslandRole()
    {
        final MetaStore metaStore = this.networkPlayer.get().getMetaStore();
        if (! metaStore.contains(PLAYER_ROLE))
        {
            return null;
        }
        return IslandRole.values()[metaStore.getInteger(PLAYER_ROLE)];
    }

    @Override
    public long getIslandCooldown()
    {
        final MetaStore metaStore = this.networkPlayer.get().getMetaStore();
        if (! metaStore.contains(PLAYER_ISLAND_COL))
        {
            return 0;
        }
        return metaStore.getLong(PLAYER_ISLAND_COL);
    }

    @Override
    public void setIslandCooldown(final long cooldown)
    {
        try
        {
            this.networkPlayer.lock();
            final IOnlinePlayer iOnlinePlayer = this.networkPlayer.get();
            iOnlinePlayer.getMetaStore().setLong(PLAYER_ISLAND_COL, cooldown);
            this.networkPlayer.set(iOnlinePlayer);
        }
        finally
        {
            this.networkPlayer.unlock();
        }
    }

    @Override
    public void setIsland(final UUID islandId, final IslandRole islandRole)
    {
        try
        {
            this.networkPlayer.lock();
            final IOnlinePlayer iOnlinePlayer = this.networkPlayer.get();
            final MetaStore metaStore = iOnlinePlayer.getMetaStore();
            if (islandId == null)
            {
                metaStore.setBoolean(PLAYER_HAS_ISLAND, false);
                metaStore.remove(PLAYER_ISLAND_ID);
                metaStore.remove(PLAYER_ROLE);
            }
            else
            {
                metaStore.setBoolean(PLAYER_HAS_ISLAND, true);
                metaStore.setUuid(PLAYER_ISLAND_ID, islandId);
                metaStore.setInteger(PLAYER_ROLE, islandRole.ordinal());
                if (islandRole == OWNER)
                {
                    metaStore.setLong(PLAYER_ISLAND_COL, System.currentTimeMillis()); // set island creation time
                }
            }
            this.networkPlayer.set(iOnlinePlayer);
        }
        finally
        {
            this.networkPlayer.unlock();
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("networkPlayer", this.networkPlayer).toString();
    }
}