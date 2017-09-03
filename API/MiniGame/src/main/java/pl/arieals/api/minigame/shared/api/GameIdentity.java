package pl.arieals.api.minigame.shared.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Obiekt identyfikujcy w sieci dany typ minigry wraz z jej wariantem.
 * Typ gry to np. elytrarace a wariant to np. score_mode.
 */
@XmlRootElement(name = "gameIdentity")
@XmlAccessorType(XmlAccessType.FIELD)
public final class GameIdentity
{
    @XmlElement(required = true)
    private String gameId;
    @XmlElement(required = true)
    private String variantId;

    /**
     * Zwraca identyfikator tej minigry, unikalny we wszystkich minigrach.
     *
     * @return identyfikator gry.
     */
    public String getGameId()
    {
        return this.gameId;
    }

    /**
     * Zwraca wariant minigry. Jedna minigra moze miec kilka wariantow.
     * Na podstawie tej wartosci plugin minigry moze zmieniac dzialanie
     * (ale nie musi).
     *
     * @return wariant gry.
     */
    public String getVariantId()
    {
        return this.variantId;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("gameId", this.gameId).append("variantId", this.variantId).toString();
    }
}