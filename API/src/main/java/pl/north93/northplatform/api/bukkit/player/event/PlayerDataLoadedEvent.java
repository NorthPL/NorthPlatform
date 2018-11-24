package pl.north93.northplatform.api.bukkit.player.event;

import java.util.Collection;

import org.bukkit.event.HandlerList;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.northplatform.api.global.network.server.joinaction.IServerJoinAction;
import pl.north93.northplatform.api.bukkit.player.INorthPlayer;

/**
 * Event wywolywany po wejsciu gracza gdy zostaly pobrane jego dane
 * i akcje do wykonania po wejsciu na serwer.
 * Jest on normalnie synchroniczny do watku serwera.
 */
public class PlayerDataLoadedEvent extends NorthPlayerEvent
{
    private static final HandlerList            handlers = new HandlerList();
    private final Collection<IServerJoinAction> joinActions;

    public PlayerDataLoadedEvent(final INorthPlayer northPlayer, final Collection<IServerJoinAction> joinActions)
    {
        super(northPlayer);
        this.joinActions = joinActions;
    }

    /**
     * Zwraca modyfikowalna liste akcji ktora zostanie wykonana po tym evencie.
     * Zostala pobrana z redisa i z niego skasowana.
     *
     * @return modyfikowalna lista akcji do wykonania.
     */
    public Collection<IServerJoinAction> getJoinActions()
    {
        return this.joinActions;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("joinActions", this.joinActions).toString();
    }
}