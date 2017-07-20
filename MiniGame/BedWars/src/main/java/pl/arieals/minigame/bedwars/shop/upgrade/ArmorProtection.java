package pl.arieals.minigame.bedwars.shop.upgrade;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import pl.arieals.api.minigame.server.gamehost.arena.LocalArena;
import pl.arieals.minigame.bedwars.arena.Team;
import pl.arieals.minigame.bedwars.event.ItemBuyEvent;
import pl.north93.zgame.api.bukkit.BukkitApiCore;

public class ArmorProtection implements IUpgrade, Listener
{
    // system agregacji wspiera SmartExecutora, wiec ten konstruktor zadziala
    private ArmorProtection(final BukkitApiCore apiCore)
    {
        apiCore.registerEvents(this);
    }

    @Override
    public void apply(final LocalArena arena, final Team team, final int level)
    {
        for (final Player player : team.getPlayers())
        {
            for (final ItemStack itemStack : player.getInventory().getArmorContents())
            {
                if (itemStack == null)
                {
                    continue;
                }

                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
            }
        }
    }

    @EventHandler
    public void newArmorHandler(final ItemBuyEvent event)
    {
        final String specialHandler = event.getShopEntry().getSpecialHandler();
        if (specialHandler == null || ! specialHandler.equals("ArmorEntry"))
        {
            return;
        }

        final ItemStack itemStack = event.getPlayer().getInventory().getArmorContents()[3]; // czapka
        if (itemStack == null)
        {
            return;
        }

        final int level = itemStack.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
        if (level == 0)
        {
            return;
        }

        for (final ItemStack stack : event.getItems())
        {
            stack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
        }
    }

    @Override
    public int maxLevel()
    {
        return 2;
    }
}