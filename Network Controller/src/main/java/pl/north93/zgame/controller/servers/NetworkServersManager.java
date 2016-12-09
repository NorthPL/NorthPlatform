package pl.north93.zgame.controller.servers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import pl.north93.zgame.api.global.API;
import pl.north93.zgame.api.global.component.Component;
import pl.north93.zgame.api.global.component.annotations.InjectComponent;
import pl.north93.zgame.api.global.deployment.RemoteDaemon;
import pl.north93.zgame.api.global.deployment.ServersGroup;
import pl.north93.zgame.api.global.network.server.ServerImpl;
import pl.north93.zgame.controller.ConfigBroadcaster;
import pl.north93.zgame.controller.servers.allocators.AllocationProcessor;

public class NetworkServersManager extends Component implements INetworkServersManager, IServerCountManager
{
    @InjectComponent("NetworkController.ConfigBroadcaster")
    private ConfigBroadcaster           configBroadcaster;
    private final AllocationProcessor   allocationProcessor;

    public NetworkServersManager()
    {
        this.allocationProcessor = new AllocationProcessor(this);
    }

    @Override
    protected void enableComponent()
    {
        new Thread(this::thread, "Servers Manager").start();
    }

    @Override
    protected void disableComponent()
    {
    }

    private void thread()
    {
        while (true) // TODO safe stop
        {
            final List<ServersGroup> serversGroups = this.configBroadcaster.getServersGroups().getGroups();
            final Set<RemoteDaemon> daemons = API.getNetworkManager().getDaemons();

            this.allocationProcessor.processTasks(daemons, serversGroups);
            try
            {
                synchronized (this)
                {
                    this.wait(1_000);
                }
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }
        }

    }

    @Override
    public IServerCountManager getServerCountManager()
    {
        return this;
    }

    @Override
    public long getServersCount(final ServersGroup serversGroup)
    {
        return API.getNetworkManager().getServers().stream()
                  .filter(server -> {
                      final Optional<ServersGroup> sGroup = server.getServersGroup();
                      return sGroup.isPresent() && sGroup.get().equals(serversGroup);
                  }).count();
    }

    @Override
    public void addServers(final ServersGroup serversGroup, final long servers)
    {
        API.getLogger().info("Adding " + servers + " servers to group " + serversGroup.getName());
        for (int i = 0; i < servers; i++)
        {
            final ServerImpl server = ServerFactory.INSTANCE.createNewServer(serversGroup);
            server.sendUpdate(); // send server data to redis.
            this.allocationProcessor.queueServerDeployment(server); // queue server for deployment.
        }
    }

    @Override
    public void removeServers(final ServersGroup serversGroup, final long servers)
    {
        API.getLogger().info("Removing " + servers + " servers from group " + serversGroup.getName());
        // TODO
    }
}