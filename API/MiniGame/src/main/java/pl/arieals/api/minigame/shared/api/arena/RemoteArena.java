package pl.arieals.api.minigame.shared.api.arena;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.arieals.api.minigame.shared.api.GamePhase;
import pl.north93.zgame.api.global.redis.messaging.annotations.MsgPackCustomTemplate;
import pl.north93.zgame.api.global.redis.messaging.templates.ArrayListTemplate;

/**
 * Obiekt przedstawia arenę znajdującą się *gdzieś* w sieci.
 * Używany jest do gadania przez RPC.
 */
public class RemoteArena implements IArena
{
    private UUID       arenaId;
    private UUID       serverId;
    private GamePhase  gamePhase;
    @MsgPackCustomTemplate(ArrayListTemplate.class)
    private List<UUID> players;

    public RemoteArena()
    {
    }

    public RemoteArena(final UUID arenaId, final UUID serverId, final GamePhase gamePhase, final List<UUID> players)
    {
        this.arenaId = arenaId;
        this.serverId = serverId;
        this.gamePhase = gamePhase;
        this.players = players;
    }

    @Override
    public UUID getId()
    {
        return this.arenaId;
    }

    @Override
    public UUID getServerId()
    {
        return this.serverId;
    }

    @Override
    public GamePhase getGamePhase()
    {
        return this.gamePhase;
    }

    public void setGamePhase(final GamePhase gamePhase)
    {
        this.gamePhase = gamePhase;
    }

    @Override
    public List<UUID> getPlayers()
    {
        return this.players;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("arenaId", this.arenaId).append("serverId", this.serverId).append("gamePhase", this.gamePhase).append("players", this.players).toString();
    }
}