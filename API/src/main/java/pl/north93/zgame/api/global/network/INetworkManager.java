package pl.north93.zgame.api.global.network;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import pl.north93.zgame.api.global.deployment.RemoteDaemon;
import pl.north93.zgame.api.global.deployment.ServerPattern;
import pl.north93.zgame.api.global.deployment.ServersGroup;
import pl.north93.zgame.api.global.messages.NetworkMeta;
import pl.north93.zgame.api.global.messages.ProxyInstanceInfo;
import pl.north93.zgame.api.global.network.minigame.MiniGame;
import pl.north93.zgame.api.global.network.server.Server;
import pl.north93.zgame.api.global.utils.ObservableValue;

public interface INetworkManager
{
    ObservableValue<NetworkMeta> getNetworkMeta();

    JoiningPolicy getJoiningPolicy();

    Set<ProxyInstanceInfo> getProxyServers();

    Set<RemoteDaemon> getDaemons();

    Set<Server> getServers();

    List<ServersGroup> getServersGroups();

    ServersGroup getServersGroup(String name);

    List<MiniGame> getMiniGames();

    MiniGame getMiniGame(String name);

    List<ServerPattern> getServerPatterns();

    ServerPattern getServerPattern(String name);

    Server getServer(UUID uuid);

    int onlinePlayersCount();

    NetworkPlayer getNetworkPlayer(String nick);

    boolean isOnline(String nick);

    void broadcastNetworkAction(NetworkAction networkAction);

    NetworkControllerRpc getNetworkController();
}