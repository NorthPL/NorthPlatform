package pl.arieals.minigame.goldhunter.classes.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.bukkit.inventory.ItemStack;

import pl.arieals.minigame.goldhunter.GoldHunterPlayer;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlClassEquipmentSlot
{
    @XmlElement(name = "item")
    private List<XmlBuyConditionItemStack> items = new ArrayList<>();
    
    public List<XmlBuyConditionItemStack> getItems()
    {
        return items;
    }
    
    public ItemStack getItemStack(GoldHunterPlayer player)
    {
        return items.stream().filter(condition -> condition.check(player)).findFirst()
                .map(condition -> condition.createItemStack()).orElse(null);
    }
}