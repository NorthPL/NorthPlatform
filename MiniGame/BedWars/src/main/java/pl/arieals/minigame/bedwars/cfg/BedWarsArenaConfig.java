package pl.arieals.minigame.bedwars.cfg;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.bukkit.utils.xml.XmlCuboid;

@XmlRootElement(name = "arena")
@XmlAccessorType(XmlAccessType.FIELD)
public class BedWarsArenaConfig
{
    @XmlElement(required = true, name = "teams")
    private List<BedWarsTeamConfig>    teams;
    @XmlElementWrapper(name = "generatorTypes")
    @XmlElement(name = "generatorType")
    private List<BedWarsGeneratorType> generatorTypes;
    @XmlElementWrapper(name = "generators")
    @XmlElement(name = "generator")
    private List<BedWarsGenerator>     generators;
    @XmlElementWrapper(name = "secureRegions")
    @XmlElement(name = "secureRegion")
    private List<XmlCuboid>            secureRegions;

    public BedWarsArenaConfig()
    {
    }

    public BedWarsArenaConfig(final List<BedWarsTeamConfig> teams, final List<BedWarsGeneratorType> generatorTypes, final List<BedWarsGenerator> generators, final List<XmlCuboid> secureRegions)
    {
        this.teams = teams;
        this.generatorTypes = generatorTypes;
        this.generators = generators;
        this.secureRegions = secureRegions;
    }

    public List<BedWarsTeamConfig> getTeams()
    {
        return this.teams;
    }

    public List<BedWarsGeneratorType> getGeneratorTypes()
    {
        return this.generatorTypes;
    }

    public List<BedWarsGenerator> getGenerators()
    {
        return this.generators;
    }

    public List<XmlCuboid> getSecureRegions()
    {
        return this.secureRegions;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("teams", this.teams).append("generatorTypes", this.generatorTypes).append("generators", this.generators).append("secureRegions", this.secureRegions).toString();
    }
}
