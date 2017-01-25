package pl.north93.zgame.api.economy.impl.client;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.economy.ICurrency;
import pl.north93.zgame.api.economy.ITransaction;
import pl.north93.zgame.api.global.metadata.MetaKey;
import pl.north93.zgame.api.global.metadata.MetaStore;
import pl.north93.zgame.api.global.network.players.IPlayer;
import pl.north93.zgame.api.global.network.players.IPlayerTransaction;

public class TransactionImpl implements ITransaction
{
    private final ICurrency          currency;
    private final IPlayerTransaction playerTransaction;
    private boolean isClosed;

    public TransactionImpl(final ICurrency currency, final IPlayerTransaction playerTransaction)
    {
        this.currency = currency;
        this.playerTransaction = playerTransaction;
    }

    @Override
    public IPlayer getAssociatedPlayer()
    {
        this.checkClosed();
        return this.playerTransaction.getPlayer();
    }

    @Override
    public int getAmount()
    {
        this.checkClosed();
        final MetaStore metaStore = this.getAssociatedPlayer().getMetaStore();
        final MetaKey prefix = this.getPrefix();
        if (metaStore.contains(prefix))
        {
            return metaStore.getInteger(prefix);
        }
        else
        {
            return this.currency.getStartValue();
        }
    }

    @Override
    public void setAmount(final int newAmount)
    {
        this.checkClosed();
        this.getAssociatedPlayer().getMetaStore().setInteger(this.getPrefix(), newAmount);
    }

    @Override
    public void close() throws Exception
    {
        this.checkClosed();
        this.isClosed = true;
        this.playerTransaction.close();
    }

    private void checkClosed()
    {
        if (this.isClosed)
        {
            throw new RuntimeException("Transaction is already closed");
        }
    }

    private MetaKey getPrefix()
    {
        return MetaKey.get("currency:" + this.currency.getName());
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("currency", this.currency).append("playerTransaction", this.playerTransaction).append("isClosed", this.isClosed).toString();
    }
}
