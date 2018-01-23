package pl.north93.zgame.api.bukkit.map.impl;

import javax.annotation.Nullable;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;

import pl.north93.zgame.api.bukkit.utils.region.Cuboid;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;

final class BoardFactory
{
    @Inject
    private static MapController mapController;

    // uniemozliwiamy tworzenie instancji
    private BoardFactory()
    {
    }

    public static BoardImpl createBoard(final Location loc1, final Location loc2)
    {
        final Cuboid cuboid = new Cuboid(loc1, loc2);

        if (cuboid.getSizeX() > 1 && cuboid.getSizeZ() > 1)
        {
            throw new IllegalArgumentException("Illegal board cuboid");
        }

        final int width = Math.max(cuboid.getSizeX(), cuboid.getSizeZ()); // szerokosc
        final int height = cuboid.getSizeY(); // wysokosc

        final MapImpl[][] maps = new MapImpl[width][height];
        final BoardImpl board = new BoardImpl(mapController, width, height, maps);

        walkWall(loc1, loc2, location ->
        {
            final int distanceX = Math.abs(loc1.getBlockX() - location.getBlockX());
            final int distanceZ = Math.abs(loc1.getBlockZ() - location.getBlockZ());
            int itemFrameX = Math.max(distanceX, distanceZ);

            final int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
            int itemFrameY = Math.abs(maxY - location.getBlockY());

            maps[itemFrameX][itemFrameY] = createMap(board, location);
        });

        return board;
    }

    private static MapImpl createMap(final BoardImpl board, final Location location)
    {
        final CraftWorld world = (CraftWorld) location.getWorld();
        world.getBlockAt(location).setType(Material.AIR);

        ItemFrame itemFrame = getFrameAt(location);
        if (itemFrame == null)
        {
            itemFrame = (ItemFrame) world.spawnEntity(location, EntityType.ITEM_FRAME);
            itemFrame.setInvulnerable(true);
        }

        return new MapImpl(mapController, board, itemFrame);
    }

    private static @Nullable ItemFrame getFrameAt(final Location loc)
    {
        final Location frameLocation = new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY() + 0.5, loc.getZ() + 0.5);
        for (final Entity entity : frameLocation.getWorld().getNearbyEntities(frameLocation, 0.5, 0.5, 0.5))
        {
            if (entity instanceof ItemFrame)
            {
                return (ItemFrame) entity;
            }
        }
        return null;
    }

    private static void walkWall(final Location loc1, final Location loc2, final Consumer<Location> locationConsumer)
    {
        final Location tempLocation = new Location(loc1.getWorld(), 0, 0, 0);

        final int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        final int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());

        if (loc1.getBlockX() > loc2.getBlockX()) // x sie roznia, z takie samo
        {
            tempLocation.setZ(loc1.getBlockZ());
            for (int x = loc1.getBlockX(); x >= loc2.getBlockX(); x--)
            {
                tempLocation.setX(x);
                for (int y = maxY; y >= minY; y--)
                {
                    tempLocation.setY(y);
                    locationConsumer.accept(tempLocation);
                }
            }
        }
        else if (loc1.getBlockX() < loc2.getBlockX()) // x sie roznia, z takie samo
        {
            tempLocation.setZ(loc1.getBlockZ());
            for (int x = loc1.getBlockX(); x <= loc2.getBlockX(); x++)
            {
                tempLocation.setX(x);
                for (int y = maxY; y >= minY; y--)
                {
                    tempLocation.setY(y);
                    locationConsumer.accept(tempLocation);
                }
            }
        }
        //
        else if (loc1.getBlockZ() > loc2.getBlockZ()) // z sie roznia, x takie samo
        {
            tempLocation.setX(loc1.getBlockX());
            for (int z = loc1.getBlockZ(); z >= loc2.getBlockZ(); z--)
            {
                tempLocation.setZ(z);
                for (int y = maxY; y >= minY; y--)
                {
                    tempLocation.setY(y);
                    locationConsumer.accept(tempLocation);
                }
            }
        }
        else if (loc1.getBlockZ() < loc2.getBlockZ()) // z sie roznia, x takie samo
        {
            tempLocation.setX(loc1.getBlockX());
            for (int z = loc1.getBlockZ(); z <= loc2.getBlockZ(); z++)
            {
                tempLocation.setZ(z);
                for (int y = maxY; y >= minY; y--)
                {
                    tempLocation.setY(y);
                    locationConsumer.accept(tempLocation);
                }
            }
        }
    }
}
