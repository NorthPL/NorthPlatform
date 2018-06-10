package pl.mcpiraci.world.properties.impl;

import org.bukkit.Bukkit;

import pl.north93.zgame.api.global.component.Component;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;

public class WorldPropertiesComponent extends Component
{
    @Inject
    private PropertiesManagerImpl propertiesManager;
    
    private static boolean enabled;
    
    @Override
    protected void enableComponent()
    {   
        propertiesManager.reloadServerConfig();
        
        Bukkit.getWorlds().forEach(propertiesManager::addWorldProperties);
        enabled = true;
    }

    @Override
    protected void disableComponent()
    {
    }
    
    public static boolean isEnabled()
    {
        return enabled;
    }
}
