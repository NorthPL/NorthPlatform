package pl.north93.zgame.api.bukkit.entityhider.impl;

import static pl.north93.zgame.api.bukkit.utils.nms.EntityTrackerHelper.getTrackerEntry;
import static pl.north93.zgame.api.bukkit.utils.nms.EntityTrackerHelper.toNmsEntity;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EntityTrackerEntry;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.bukkit.BukkitApiCore;
import pl.north93.zgame.api.bukkit.Main;
import pl.north93.zgame.api.bukkit.entityhider.EntityVisibility;
import pl.north93.zgame.api.bukkit.entityhider.IEntityHider;
import pl.north93.zgame.api.global.component.Component;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;

public class EntityHiderImpl extends Component implements IEntityHider
{
    @Inject
    private BukkitApiCore apiCore;
    private final GlobalVisibility globalVisibility = new GlobalVisibility();

    @Override
    public void setVisibility(final Player player, final EntityVisibility visibility, final Collection<Entity> entities)
    {
        final VisibilityController controller = this.getController(player);
        entities.forEach(entity -> controller.setVisibility(entity, visibility));
        this.refreshEntities(entities);
    }

    @Override
    public void setVisibility(final EntityVisibility visibility, final Collection<Entity> entities)
    {
        entities.forEach(entity -> this.globalVisibility.setGlobalEntityStatus(entity, visibility));
        this.refreshEntities(entities);
    }

    @Override
    public boolean isVisible(final Player player, final Entity entity)
    {
        return this.getController(player).isEntityVisible(entity);
    }

    @Override
    public void refreshEntities(final Collection<Entity> entities)
    {
        for (final Entity entity : entities)
        {
            final EntityTrackerEntry tracker = getTrackerEntry(toNmsEntity(entity));
            if (tracker == null)
            {
                continue;
            }

            for (final EntityPlayer trackedPlayer : new HashSet<>(tracker.trackedPlayers))
            {
                tracker.clear(trackedPlayer); // kasujemy gracza z listy sledzacych
                tracker.updatePlayer(trackedPlayer); // próbujemy zaktualizowac status sledzenia
            }
        }
    }

    private VisibilityController getController(final Player player)
    {
        final List<MetadataValue> metadata = player.getMetadata("API.EntityHider/controller");
        if (metadata.isEmpty())
        {
            final Main pluginMain = this.apiCore.getPluginMain();

            final VisibilityController controller = new VisibilityController(this.globalVisibility);
            player.setMetadata("API.EntityHider/controller", new FixedMetadataValue(pluginMain, controller));

            return controller;
        }
        return (VisibilityController) metadata.get(0).value();
    }

    @Override
    protected void enableComponent()
    {
    }

    @Override
    protected void disableComponent()
    {
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
