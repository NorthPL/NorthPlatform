package pl.north93.zgame.api.economy.impl.shared;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.economy.IAccountAccessor;
import pl.north93.zgame.api.economy.ICurrency;
import pl.north93.zgame.api.global.metadata.MetaKey;
import pl.north93.zgame.api.global.metadata.MetaStore;
import pl.north93.zgame.api.global.network.players.IPlayer;

class PlayerAccessor implements IAccountAccessor
{
    private final IPlayer   player;
    private final ICurrency currency;
    private final MetaKey   prefix;

    public PlayerAccessor(final IPlayer player, final ICurrency currency)
    {
        this.player = player;
        this.currency = currency;
        this.prefix = MetaKey.get("currency:" + currency.getName());
    }

    public void setAmount(final double newAmount)
    {
        this.player.getMetaStore().setDouble(this.prefix, newAmount);
    }

    @Override
    public IPlayer getAssociatedPlayer()
    {
        return this.player;
    }

    @Override
    public ICurrency getCurrency()
    {
        return this.currency;
    }

    @Override
    public double getAmount()
    {
        final MetaStore metaStore = this.player.getMetaStore();
        return metaStore.contains(this.prefix) ? metaStore.getDouble(this.prefix) : this.currency.getStartValue();
    }

    @Override
    public boolean has(final double amount)
    {
        return this.getAmount() >= amount;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("player", this.player).append("currency", this.currency).toString();
    }
}
