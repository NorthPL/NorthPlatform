package pl.north93.zgame.api.economy.impl.client;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.economy.IEconomyManager;
import pl.north93.zgame.api.economy.impl.shared.EconomyManagerImpl;
import pl.north93.zgame.api.global.component.Component;
import pl.north93.zgame.api.global.component.annotations.IncludeInScanning;

@IncludeInScanning("pl.north93.zgame.api.economy.impl.shared")
public class EconomyComponent extends Component
{
    private EconomyManagerImpl economyManager;

    @Override
    protected void enableComponent()
    {
        this.economyManager = new EconomyManagerImpl();
    }

    @Override
    protected void disableComponent()
    {
    }

    public IEconomyManager getEconomyManager()
    {
        return this.economyManager;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("economyManager", this.economyManager).toString();
    }
}
