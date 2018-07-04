package pl.arieals.lobby.game;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Optional;

import org.bukkit.entity.Player;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.arieals.api.minigame.shared.api.booster.IBoosterManager;
import pl.arieals.api.minigame.shared.api.statistics.IRecord;
import pl.arieals.api.minigame.shared.api.statistics.unit.NumberUnit;
import pl.north93.zgame.api.bukkit.player.INorthPlayer;
import pl.north93.zgame.api.bukkit.scoreboard.IScoreboardLayout;
import pl.north93.zgame.api.economy.IAccountAccessor;
import pl.north93.zgame.api.economy.ICurrency;
import pl.north93.zgame.api.economy.IEconomyManager;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.IOnlinePlayer;
import pl.north93.zgame.api.global.network.players.Identity;
import pl.north93.zgame.api.global.redis.observable.Value;

/**
 * Klasa pomocnicza do tworzenia scoreboardow na hubach.
 */
public abstract class HubScoreboardLayout implements IScoreboardLayout
{
    private static final NumberFormat CURRENCY_FORMATTER = new DecimalFormat("#");
    @Inject
    protected INetworkManager networkManager;
    @Inject
    protected IEconomyManager economyManager;
    @Inject
    protected IBoosterManager boosterManager;

    /**
     * Formatuje wynik odpytywania baze o statystyke.
     *
     * @param optional Wynik z metody pobierajacej statystyke.
     * @return Sformatowana liczba.
     */
    protected final String parseNumber(final Optional<IRecord<NumberUnit>> optional)
    {
        return optional.map(numberUnitIRecord -> numberUnitIRecord.getValue().getValue().toString()).orElse("0");
    }

    protected final String getPlayerCurrency(final Player player)
    {
        final Identity identity = Identity.of(player);
        final ICurrency currency = this.economyManager.getCurrency("minigame");

        final IAccountAccessor unsafeAccessor = this.economyManager.getUnsafeAccessor(currency, identity);
        return CURRENCY_FORMATTER.format(unsafeAccessor.getAmount());
    }

    protected final String getPlayerBooster(final INorthPlayer player)
    {
        final Value<IOnlinePlayer> value = this.networkManager.getPlayers().unsafe().getOnlineValue(player.getName());
        if (! value.isPreset())
        {
            return "?";
        }

        final double multiplier = this.boosterManager.calculateFinalMultiplier(value.get());
        return "x" + multiplier;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
