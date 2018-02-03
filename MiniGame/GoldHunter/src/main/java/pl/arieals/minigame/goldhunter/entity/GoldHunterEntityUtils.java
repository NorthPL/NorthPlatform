package pl.arieals.minigame.goldhunter.entity;

import java.util.Set;

import org.diorite.utils.reflections.DioriteReflectionUtils;
import org.diorite.utils.reflections.FieldAccessor;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.RegistryMaterials;

public class GoldHunterEntityUtils
{
    public static void registerGoldHunterEntities()
    {
        registerCustomEntity(PoisonArrow.class, 10);
    }
    
    private static void registerCustomEntity(Class<? extends Entity> entityClass, int typeId)
    {
        FieldAccessor<RegistryMaterials<MinecraftKey, Class<? extends Entity>>> b = DioriteReflectionUtils.getField(EntityTypes.class, "b");
        FieldAccessor<Set<MinecraftKey>> d = DioriteReflectionUtils.getField(EntityTypes.class, "d");
        
        MinecraftKey key = new MinecraftKey("goldhunter", entityClass.getSimpleName());
        b.get(null).a(typeId, key, entityClass);
        d.get(null).add(key);
    }
}
