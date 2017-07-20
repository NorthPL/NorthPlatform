package pl.arieals.minigame.bedwars.cfg;

import static pl.north93.zgame.api.global.utils.CollectionUtils.findInCollection;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Globalna konfiguracja rozgrywki BedWars.
 */
@XmlRootElement(name = "bedwars")
@XmlAccessorType(XmlAccessType.FIELD)
public class BwConfig
{
    @XmlElement
    private Integer               destroyBedsAt;
    @XmlElement
    private Integer               startDeathMatchAt;
    @XmlElement
    private Integer               teamSize;
    @XmlElementWrapper(name = "generatorTypes")
    @XmlElement(name = "generatorType")
    private List<BwGeneratorType> generatorTypes;
    @XmlElementWrapper(name = "shopEntries")
    @XmlElement(name = "shopEntry")
    private List<BwShopEntry>     shopEntries;
    @XmlElement
    private Map<String, Integer>  upgrades;

    public Integer getDestroyBedsAt()
    {
        return this.destroyBedsAt;
    }

    public Integer getStartDeathMatchAt()
    {
        return this.startDeathMatchAt;
    }

    public Integer getTeamSize()
    {
        return this.teamSize;
    }

    public List<BwGeneratorType> getGeneratorTypes()
    {
        return this.generatorTypes;
    }

    public BwGeneratorType getGeneratorType(final String name)
    {
        return findInCollection(this.generatorTypes, BwGeneratorType::getName, name);
    }

    public List<BwShopEntry> getShopEntries()
    {
        return this.shopEntries;
    }

    public Map<String, Integer> getUpgrades()
    {
        return this.upgrades;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("destroyBedsAt", this.destroyBedsAt).append("startDeathMatchAt", this.startDeathMatchAt).append("teamSize", this.teamSize).append("generatorTypes", this.generatorTypes).append("shopEntries", this.shopEntries).append("upgrades", this.upgrades).toString();
    }
}