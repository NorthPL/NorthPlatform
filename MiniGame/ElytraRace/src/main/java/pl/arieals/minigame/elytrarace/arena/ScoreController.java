package pl.arieals.minigame.elytrarace.arena;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_10_R1.EntityFallingBlock;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.arieals.api.minigame.server.gamehost.arena.LocalArena;
import pl.arieals.minigame.elytrarace.cfg.Score;
import pl.north93.zgame.api.bukkit.entityhider.IEntityHider;
import pl.north93.zgame.api.bukkit.utils.region.Cuboid;
import pl.north93.zgame.api.global.component.annotations.InjectComponent;

public class ScoreController
{
    @InjectComponent("API.EntityHider")
    private IEntityHider entityHider;
    private final LocalArena   arena;
    private final Score        score; // score point associated with this controller
    private final List<Entity> normalBlocks;
    private final List<Entity> grayedBlocks;

    public ScoreController(final LocalArena arena, final Score score)
    {
        this.arena = arena;
        this.score = score;
        this.normalBlocks = new ArrayList<>();
        this.grayedBlocks = new ArrayList<>();
    }

    public void setup()
    {
        final Cuboid cuboid = this.score.getArea().toCuboid(this.arena.getWorld().getCurrentWorld());

        final CraftWorld world = (CraftWorld) cuboid.getWorld();
        for (final Block block : cuboid)
        {
            if (block.isEmpty())
            {
                continue; // air
            }

            {
                final EntityFallingBlock fallingBlock = NorthFallingBlock.create(block.getLocation(), block.getType(), block.getData());
                world.addEntity(fallingBlock, CreatureSpawnEvent.SpawnReason.CUSTOM);

                this.normalBlocks.add(fallingBlock.getBukkitEntity());
            }

            {
                final EntityFallingBlock fallingBlock = NorthFallingBlock.create(block.getLocation(), Material.WOOL, (byte) 7);
                world.addEntity(fallingBlock, CreatureSpawnEvent.SpawnReason.CUSTOM);

                this.grayedBlocks.add(fallingBlock.getBukkitEntity());
            }

            block.setType(Material.AIR, false);
        }
    }

    public void makeGray(final Player player)
    {
        this.entityHider.hideEntities(player, this.normalBlocks);
        this.entityHider.showEntities(player, this.grayedBlocks);
    }

    public void makeNormal(final Player player)
    {
        this.entityHider.hideEntities(player, this.grayedBlocks);
        this.entityHider.showEntities(player, this.normalBlocks);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("arena", this.arena).append("score", this.score).append("normalBlocks", this.normalBlocks).append("grayedBlocks", this.grayedBlocks).toString();
    }
}