package pl.north93.zgame.api.bukkit.player.impl;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.bukkit.BukkitApiCore;
import pl.north93.zgame.api.bukkit.player.IBukkitPlayers;
import pl.north93.zgame.api.bukkit.player.INorthPlayer;
import pl.north93.zgame.api.global.component.Component;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.IOnlinePlayer;
import pl.north93.zgame.api.global.network.players.IPlayersManager;
import pl.north93.zgame.api.global.redis.observable.Value;

public class BukkitPlayerManagerImpl extends Component implements IBukkitPlayers
{
    @Inject
    private BukkitApiCore   bukkitApiCore;
    @Inject
    private INetworkManager networkManager;

    @Override
    protected void enableComponent()
    {
        this.bukkitApiCore.registerEvents(new JoinLeftListener(), new ChatListener(), new LanguageKeeper());
    }

    @Override
    protected void disableComponent()
    {
    }

    @Override
    public OfflinePlayer getBukkitOfflinePlayer(final UUID uuid)
    {
        final IPlayersManager.Unsafe unsafe = this.networkManager.getPlayers().unsafe();
        return unsafe.getOffline(uuid).map(NorthOfflinePlayer::new).orElse(null);
    }

    @Override
    public OfflinePlayer getBukkitOfflinePlayer(final String nick)
    {
        final IPlayersManager.Unsafe unsafe = this.networkManager.getPlayers().unsafe();
        return unsafe.getOffline(nick).map(NorthOfflinePlayer::new).orElse(null);
    }

    @Override
    public INorthPlayer getPlayer(final Player player)
    {
        final Value<IOnlinePlayer> onlinePlayerData = this.networkManager.getPlayers().unsafe().getOnline(player.getName());
        return this.wrapNorthPlayer(player, onlinePlayerData);
    }

    @Override
    public INorthPlayer getPlayer(final UUID uuid)
    {
        return Optional.ofNullable(Bukkit.getPlayer(uuid)).map(this::getPlayer).orElse(null);
    }

    @Override
    public INorthPlayer getPlayer(final String nick)
    {
        return Optional.ofNullable(Bukkit.getPlayer(nick)).map(this::getPlayer).orElse(null);
    }

    @Override
    public CraftPlayer getCraftPlayer(final Player player)
    {
        if (player instanceof CraftPlayer)
        {
            return (CraftPlayer) player;
        }
        return ((INorthPlayer) player).getCraftPlayer();
    }

    private INorthPlayer wrapNorthPlayer(final Player player, final Value<IOnlinePlayer> playerData)
    {
        if (player instanceof NorthPlayer)
        {
            return (INorthPlayer) player;
        }
        return new NorthPlayer(this.networkManager, player, playerData);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
