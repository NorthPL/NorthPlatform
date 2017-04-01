package pl.north93.zgame.controller.playerfixer;

import java.text.MessageFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.component.Component;
import pl.north93.zgame.api.global.component.annotations.InjectComponent;
import pl.north93.zgame.api.global.data.StorageConnector;
import pl.north93.zgame.api.global.data.players.IPlayersData;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.IOnlinePlayer;
import pl.north93.zgame.api.global.redis.observable.Value;
import redis.clients.jedis.Jedis;

/**
 * Klasa ma za zadanie usuwać z Redisa dane graczy których już nie ma na bungee
 * (czyli naprawiać zbugowanych)
 * Przydatne gdy bungee się zcrashuje, utraci kontankt z bazą itp.
 */
public class PlayersDataFixer extends Component implements Runnable
{
    private Logger           logger;
    @InjectComponent("API.Database.StorageConnector")
    private StorageConnector storage;
    @InjectComponent("API.MinecraftNetwork.NetworkManager")
    private INetworkManager  networkManager;
    @InjectComponent("API.MinecraftNetwork.PlayersStorage")
    private IPlayersData     playersData;

    @Override
    protected void enableComponent()
    {
        // wykonywanie zadania co 10 minut
        this.getApiCore().getPlatformConnector().runTaskAsynchronously(this, 20 * 60 * 10);
    }

    @Override
    protected void disableComponent()
    {
    }

    @Override
    public void run()
    {
        final Set<String> keys;
        try (final Jedis jedis = this.storage.getJedisPool().getResource())
        {
            keys = jedis.keys("players:*");
        }

        keys.stream().map(s -> StringUtils.replace(s, "players:", "")).forEach(this::checkPlayer);
    }

    private void checkPlayer(final String nick)
    {
        final Value<IOnlinePlayer> player = this.networkManager.getPlayers().unsafe().getOnline(nick);
        final IOnlinePlayer cache = player.get();
        if (cache == null)
        {
            return;
        }

        try
        {
            if (cache.isOnline()) // isOnline wysyła zapytanie bezpośrednio do bungee
            {
                return;
            }
        }
        catch (final Exception exception) // RpcException
        {
            final String msg = "[PlayersDataFixer] Exception occurred while checking player {0}";
            this.logger.log(Level.WARNING, MessageFormat.format(msg, nick), exception);
            return;
        }

        this.playersData.savePlayer(cache);
        player.delete();

        final String msg = "[PlayersDataFixer] Flushed data of player {0} because he isn't online in bungee";
        this.logger.info(MessageFormat.format(msg, nick));
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}