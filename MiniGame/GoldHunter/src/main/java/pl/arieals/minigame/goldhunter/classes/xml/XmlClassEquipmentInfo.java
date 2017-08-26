package pl.arieals.minigame.goldhunter.classes.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import pl.arieals.minigame.goldhunter.GoldHunterPlayer;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlClassEquipmentInfo
{
    private XmlClassEquipmentSlot chestplate = new XmlClassEquipmentSlot();
    
    private XmlClassEquipmentSlot leggins = new XmlClassEquipmentSlot();
    
    private XmlClassEquipmentSlot boots = new XmlClassEquipmentSlot();
    
    @XmlElement(name = "inv")
    private List<XmlClassEquipmentInvSlot> inventory = new ArrayList<>();
    
    public XmlClassEquipmentSlot getChestplate()
    {
        return chestplate;
    }
    
    public XmlClassEquipmentSlot getLeggins()
    {
        return leggins;
    }
    
    public XmlClassEquipmentSlot getBoots()
    {
        return boots;
    }
    
    public List<XmlClassEquipmentInvSlot> getInventory()
    {
        return inventory;
    }
    
    public void applyToPlayer(GoldHunterPlayer player)
    {
        PlayerInventory inv = player.getPlayer().getInventory();
        
        inv.setChestplate(chestplate.getItemStack(player));
        inv.setLeggings(leggins.getItemStack(player));
        inv.setBoots(boots.getItemStack(player));
        
        inv.setContents(getInventoryContents(player));
    }
    
    private ItemStack[] getInventoryContents(GoldHunterPlayer player)
    {
        ItemStack[] result = new ItemStack[36];
        
        for ( XmlClassEquipmentInvSlot slot : getInventory() )
        {
            result[slot.getSlot()] = slot.getItemStack(player);
        }
        
        return result;
    }
}