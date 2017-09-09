package pl.north93.zgame.controller.servers.groups;

import javax.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.component.annotations.bean.Aggregator;
import pl.north93.zgame.api.global.component.annotations.bean.Bean;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.config.IConfig;
import pl.north93.zgame.api.global.config.NetConfig;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.server.group.ServersGroupDto;
import pl.north93.zgame.api.global.redis.observable.Hash;
import pl.north93.zgame.api.global.network.daemon.config.AutoScalingConfig;
import pl.north93.zgame.api.global.network.daemon.config.ServersGroupConfig;
import pl.north93.zgame.controller.servers.scaler.value.IScalingValue;

public class LocalGroupsManager
{
    @Inject
    private Logger                          logger;
    @Inject
    private INetworkManager                 networkManager;
    @Inject @NetConfig(type = AutoScalingConfig.class, id = "autoscaler")
    private IConfig<AutoScalingConfig>      config;
    private Map<String, IScalingValue>      scalingValues = new HashMap<>();
    private Map<String, ILocalServersGroup> localGroups = new HashMap<>();

    @Bean
    private LocalGroupsManager()
    {
    }

    @Aggregator(IScalingValue.class)
    private void aggregateScalingValues(final IScalingValue value)
    {
        this.scalingValues.put(value.getId(), value);
    }

    public @Nullable IScalingValue getScalingValue(final String id)
    {
        return this.scalingValues.get(id);
    }

    public @Nullable ILocalServersGroup getGroup(final String id)
    {
        return this.localGroups.get(id);
    }

    public Collection<ILocalServersGroup> getLocalGroups()
    {
        return this.localGroups.values();
    }

    public void loadGroups()
    {
        final AutoScalingConfig config = this.config.get();

        for (final ServersGroupConfig groupConfig : config.getServersGroups())
        {
            if (this.getGroup(groupConfig.getName()) != null)
            {
                // nie dodajemy kolejny raz
                continue;
            }

            this.createGroupFromConfig(groupConfig);
        }
    }

    private void createGroupFromConfig(final ServersGroupConfig config)
    {
        final Hash<ServersGroupDto> serversGroups = this.networkManager.getServers().unsafe().getServersGroups();

        final ILocalServersGroup localGroup = LocalServersGroupFactory.INSTANCE.createLocalGroup(config);
        this.localGroups.put(localGroup.getName(), localGroup);
        serversGroups.put(localGroup.getName(), localGroup.getAsDto());

        localGroup.init();

        this.logger.log(Level.INFO, "Created {0} group with name {1}", new Object[]{config.getType(), config.getName()});
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("config", this.config).append("localGroups", this.localGroups).toString();
    }
}
