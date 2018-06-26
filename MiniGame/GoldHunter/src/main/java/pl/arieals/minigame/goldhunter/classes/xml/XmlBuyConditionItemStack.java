package pl.arieals.minigame.goldhunter.classes.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import pl.north93.zgame.api.bukkit.utils.xml.itemstack.XmlItemStack;

@XmlAccessorType(XmlAccessType.NONE)
public class XmlBuyConditionItemStack extends XmlItemStack implements BuyConditionElement
{
    @XmlAttribute(name = "ifbuyed")
    private String condition;
    
    public String getCondition()
    {
        return condition;
    }
}
