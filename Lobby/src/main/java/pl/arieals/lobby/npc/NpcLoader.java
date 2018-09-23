package pl.arieals.lobby.npc;

import javax.xml.bind.JAXB;

import java.io.File;

import org.bukkit.World;

import lombok.extern.slf4j.Slf4j;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import pl.arieals.lobby.npc.xml.WorldNpcs;
import pl.arieals.lobby.npc.xml.XmlNpc;
import pl.north93.zgame.api.bukkit.server.IWorldInitializer;

@Slf4j
public class NpcLoader implements IWorldInitializer
{
    private final NPCRegistry registry = CitizensAPI.createNamedNPCRegistry("hub", new MemoryNPCDataStore());

    @Override
    public void initialiseWorld(final World world, final File directory)
    {
        this.loadNpcsInWorld(world, directory);
    }

    private void loadNpcsInWorld(final World world, final File directory)
    {
        final File xmlFile = new File(directory, "Hub.NPCs.xml");
        if (! xmlFile.exists())
        {
            return;
        }

        log.info("Loading NPCs from {}", xmlFile);

        final WorldNpcs npcs = JAXB.unmarshal(xmlFile, WorldNpcs.class);
        for (final XmlNpc xmlNpc : npcs.getNpcs())
        {
            xmlNpc.createNpc(this.registry, world);
        }
    }
}
