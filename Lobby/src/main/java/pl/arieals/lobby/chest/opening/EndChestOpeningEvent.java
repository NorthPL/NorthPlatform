package pl.arieals.lobby.chest.opening;

import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class EndChestOpeningEvent extends PlayerEvent
{
    private static final HandlerList handlers = new HandlerList();
    private final IOpeningSession session;

    public EndChestOpeningEvent(final IOpeningSession session)
    {
        super(session.getPlayer());
        this.session = session;
    }

    public IOpeningSession getSession()
    {
        return this.session;
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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("session", this.session).toString();
    }
}
