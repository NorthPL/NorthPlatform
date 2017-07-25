package pl.north93.zgame.api.bukkit.gui.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.reflections.Reflections;

import com.google.common.base.Preconditions;

import pl.north93.zgame.api.bukkit.gui.impl.xml.XmlGuiLayout;
import pl.north93.zgame.api.bukkit.gui.impl.xml.XmlHotbarLayout;
import pl.north93.zgame.api.global.API;

public class XmlLayoutRegistry
{
    private static final Map<String, XmlGuiLayout> loadedGuiLayouts = new HashMap<>();
    private static final Map<String, XmlHotbarLayout> loadedHotbarLayouts = new HashMap<>();
    
    public static XmlGuiLayout getGuiLayout(String name)
    {
        Preconditions.checkArgument(loadedGuiLayouts.containsKey(name), "Gui layout with name " + name + " doesn't exists!");
        return loadedGuiLayouts.get(name);
    }
    
    public static XmlHotbarLayout getHotbarLayout(String name)
    {
        Preconditions.checkArgument(loadedHotbarLayouts.containsKey(name), "Hotbar layout with name " + name + " doesn't exists!");
        return loadedHotbarLayouts.get(name);
    }
    
    public static void loadLayouts(ClassLoader cl)
    {
        final Reflections reflections = API.getApiCore().getComponentManager().accessReflections(cl);

        final Collection<String> values = reflections.getStore().get("ResourcesScanner").values();
        
        values.stream().filter(name -> name.startsWith("gui") && name.endsWith(".xml")).forEach(path -> loadGuiLayout(cl, path));
        values.stream().filter(name -> name.startsWith("hotbar") && name.endsWith(".xml")).forEach(path -> loadHotbarLayout(cl, path));
    }
    
    private static void loadGuiLayout(ClassLoader cl, String path)
    {
        String name = path.substring("gui/".length(), path.length() - ".xml".length());
        XmlGuiLayout layout = JAXB.unmarshal(cl.getResourceAsStream(path), XmlGuiLayout.class);
        loadedGuiLayouts.putIfAbsent(name, layout);
        System.out.println("Loaded gui layout with name " + name);
    }
    
    private static void loadHotbarLayout(ClassLoader cl, String path)
    {
        String name = path.substring("hotbar/".length(), path.length() - ".xml".length());
        XmlHotbarLayout layout = JAXB.unmarshal(cl.getResourceAsStream(path), XmlHotbarLayout.class);
        loadedHotbarLayouts.putIfAbsent(name, layout);
        System.out.println("Loaded hotbar layout with name " + name);
    }
}