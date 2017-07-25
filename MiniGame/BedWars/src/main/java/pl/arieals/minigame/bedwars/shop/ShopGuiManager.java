package pl.arieals.minigame.bedwars.shop;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.arieals.minigame.bedwars.cfg.BwShopEntry;
import pl.arieals.minigame.bedwars.shop.gui.ShopArmor;
import pl.arieals.minigame.bedwars.shop.gui.ShopBase;
import pl.arieals.minigame.bedwars.shop.gui.ShopBows;
import pl.arieals.minigame.bedwars.shop.gui.ShopExtras;
import pl.arieals.minigame.bedwars.shop.gui.ShopLapis;
import pl.arieals.minigame.bedwars.shop.gui.ShopMain;
import pl.arieals.minigame.bedwars.shop.gui.ShopMaterials;
import pl.arieals.minigame.bedwars.shop.gui.ShopSwords;
import pl.arieals.minigame.bedwars.shop.gui.ShopTools;
import pl.north93.zgame.api.bukkit.utils.ChatUtils;
import pl.north93.zgame.api.global.component.annotations.bean.Bean;
import pl.north93.zgame.api.global.messages.Messages;
import pl.north93.zgame.api.global.messages.MessagesBox;
import pl.north93.zgame.api.global.messages.PluralForm;
import pl.north93.zgame.api.global.uri.UriHandler;

public final class ShopGuiManager
{
    private final ShopManager shopManager;
    private final MessagesBox shopMessages;

    @Bean
    private ShopGuiManager(final ShopManager shopManager, final @Messages("BedWarsShop") MessagesBox shopMessages)
    {
        this.shopManager = shopManager;
        this.shopMessages = shopMessages;
    }

    @UriHandler("/minigame/bedwars/shopCategory/:name/:playerId")
    public boolean openCategory(final String calledUri, final Map<String, String> parameters)
    {
        final Player player = Bukkit.getPlayer(UUID.fromString(parameters.get("playerId")));

        final String name = parameters.get("name");
        final ShopBase gui;
        switch (name)
        {
            case "main":
                gui = new ShopMain(player);
                break;
            case "materials":
                gui = new ShopMaterials(player);
                break;
            case "tools":
                gui = new ShopTools(player);
                break;
            case "swords":
                gui = new ShopSwords(player);
                break;
            case "bows":
                gui = new ShopBows(player);
                break;
            case "armor":
                gui = new ShopArmor(player);
                break;
            case "extras":
                gui = new ShopExtras(player);
                break;
            case "lapis":
                gui = new ShopLapis(player);
                break;
            default:
                return false;
        }

        gui.open(player);

        return true;
    }

    @UriHandler("/minigame/bedwars/shop/nameColor/:name/:playerId")
    public String getNameColor(final String calledUri, final Map<String, String> parameters)
    {
        final Player player = Bukkit.getPlayer(UUID.fromString(parameters.get("playerId")));
        final String name = parameters.get("name");

        final ItemStack price = this.shopManager.getShopEntry(name).getPrice().createItemStack();
        if (player.getInventory().containsAtLeast(price, price.getAmount()))
        {
            return "a";
        }
        return "c";
    }

    @UriHandler("/minigame/bedwars/shop/lore/:name/:playerId")
    public String getLore(final String calledUri, final Map<String, String> parameters)
    {
        final Player player = Bukkit.getPlayer(UUID.fromString(parameters.get("playerId")));
        final String name = parameters.get("name");
        final String locale = player.spigot().getLocale();

        final BwShopEntry shopEntry = this.shopManager.getShopEntry(name);
        final ItemStack priceItem = shopEntry.getPrice().createItemStack();

        final String currencyKey = "currency." + priceItem.getType().name().toLowerCase(Locale.ENGLISH);
        final String priceMsgKey = PluralForm.transformKey(currencyKey, priceItem.getAmount());
        final String price = this.shopMessages.getMessage(locale, priceMsgKey, priceItem.getAmount());

        final String description = this.shopMessages.getMessage(locale, "item." + name + ".lore");

        if (player.getInventory().containsAtLeast(priceItem, priceItem.getAmount()))
        {
            return this.shopMessages.getMessage(locale,
                    "gui.shop.item_lore.available",
                    description,
                    price);
        }
        else
        {
            final String currencyName = ChatUtils.stripColor(this.shopMessages.getMessage(locale, currencyKey + ".many", ""));
            return this.shopMessages.getMessage(locale,
                    "gui.shop.item_lore.no_money",
                    description,
                    price,
                    currencyName);
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}