package pl.north93.zgame.api.economy.impl.shared;

import static pl.north93.zgame.api.global.utils.CollectionUtils.findInCollection;


import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.economy.ICurrency;
import pl.north93.zgame.api.economy.IEconomyManager;
import pl.north93.zgame.api.economy.ITransaction;
import pl.north93.zgame.api.economy.cfg.CurrencyConfig;
import pl.north93.zgame.api.economy.cfg.EconomyConfig;
import pl.north93.zgame.api.global.component.annotations.PostInject;
import pl.north93.zgame.api.global.component.annotations.bean.Bean;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.exceptions.PlayerNotFoundException;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.IPlayerTransaction;
import pl.north93.zgame.api.global.redis.observable.IObservationManager;
import pl.north93.zgame.api.global.redis.observable.Value;

public class EconomyManagerImpl implements IEconomyManager
{
    @Inject
    private IObservationManager  observation;
    @Inject
    private INetworkManager      networkManager;
    // - - -
    private Value<EconomyConfig> config;

    @Bean
    private EconomyManagerImpl()
    {
    }

    @PostInject
    private void init()
    {
        this.config = this.observation.get(EconomyConfig.class, "economy_config");
    }

    @Override
    public CurrencyConfig getCurrency(final String name)
    {
        return findInCollection(this.config.get().getCurrencies(), CurrencyConfig::getName, name);
    }

    @Override
    public CurrencyRankingImpl getRanking(final ICurrency currency)
    {
        return new CurrencyRankingImpl(currency.getName());
    }

    @Override
    public ITransaction openTransaction(final ICurrency currency, final UUID playerId) throws PlayerNotFoundException
    {
        final IPlayerTransaction transaction = this.networkManager.getPlayers().transaction(playerId);
        return new TransactionImpl(currency, transaction, this.getRanking(currency));
    }

    @Override
    public ITransaction openTransaction(final ICurrency currency, final String playerName) throws PlayerNotFoundException
    {
        final IPlayerTransaction transaction = this.networkManager.getPlayers().transaction(playerName);
        return new TransactionImpl(currency, transaction, this.getRanking(currency));
    }

    public void setConfig(final EconomyConfig economyConfig)
    {
        this.config.set(economyConfig);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
