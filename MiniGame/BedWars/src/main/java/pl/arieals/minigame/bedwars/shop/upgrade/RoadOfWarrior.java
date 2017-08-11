package pl.arieals.minigame.bedwars.shop.upgrade;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import org.diorite.utils.math.DioriteMathUtils;

import pl.arieals.api.minigame.server.gamehost.arena.LocalArena;
import pl.arieals.minigame.bedwars.arena.Team;
import pl.arieals.minigame.bedwars.cfg.BwConfig;
import pl.arieals.minigame.bedwars.event.ItemBuyEvent;
import pl.north93.zgame.api.bukkit.BukkitApiCore;
import pl.north93.zgame.api.global.messages.MessagesBox;

public class RoadOfWarrior implements IUpgrade, Listener
{
    private final BwConfig bwConfig;

    // system agregacji wspiera SmartExecutora
    private RoadOfWarrior(final BwConfig config, final BukkitApiCore apiCore)
    {
        this.bwConfig = config;
        apiCore.registerEvents(this);
    }

    @Override
    public void apply(final LocalArena arena, final Team team, final int level)
    {
        for (final Player player : team.getPlayers())
        {
            for (final ItemStack itemStack : player.getInventory().getContents())
            {
                if (itemStack == null || ! this.isSword(itemStack.getType()))
                {
                    continue;
                }
                this.apply(itemStack, level);
            }
        }
    }

    @Override
    public String getLoreDescription(final MessagesBox messagesBox, final Team team, final Player player)
    {
        final String sharpnessLevel = DioriteMathUtils.toRoman(Math.min(team.getUpgrades().getUpgradeLevel(this) + 1, this.maxLevel()));
        return messagesBox.getMessage(player.spigot().getLocale(), "upgrade_gui.RoadOfWarrior.lore", sharpnessLevel);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemBuy(final ItemBuyEvent event)
    {
        final int upgradeLevel = this.getUpgradeLevel(event.getPlayer());
        if (upgradeLevel == 0)
        {
            return;
        }

        for (final ItemStack itemStack : event.getItems())
        {
            final Material type = itemStack.getType();
            if (! this.isSword(type))
            {
                continue;
            }
            this.apply(itemStack, upgradeLevel);
        }
    }

    private boolean isSword(final Material type)
    {
        return type == Material.WOOD_SWORD || type == Material.STONE_SWORD || type == Material.IRON_SWORD || type == Material.GOLD_SWORD || type == Material.DIAMOND_SWORD;
    }

    public void apply(final ItemStack itemStack, final int level)
    {
        itemStack.addEnchantment(Enchantment.DAMAGE_ALL, level);
    }

    @Override
    public int maxLevel()
    {
        if (this.bwConfig.getTeamSize() == 4)
        {
            return 3;
        }
        return 1;
    }
}
