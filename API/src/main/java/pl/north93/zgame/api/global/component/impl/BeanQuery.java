package pl.north93.zgame.api.global.component.impl;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.component.IBeanQuery;

class BeanQuery implements IBeanQuery, Predicate<BeanContainer>
{
    private String name;
    private Class<?> type;
    private boolean exactType = false;

    @Override
    public BeanQuery name(final String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public BeanQuery type(final Class<?> clazz)
    {
        this.type = clazz;
        return this;
    }

    @Override
    public BeanQuery requireExactTypeMatch()
    {
        this.exactType = true;
        return this;
    }

    @Override
    public boolean test(final BeanContainer beanContainer)
    {
        if (! StringUtils.isEmpty(this.name) && ! beanContainer.getName().equals(this.name))
        {
            return false;
        }

        if (this.type != null)
        {
            if (this.exactType)
            {
                if (this.type != beanContainer.getType())
                {
                    return false;
                }
            }
            else
            {
                return beanContainer.getType().isAssignableFrom(this.type);
            }
        }

        return true;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("name", this.name).append("type", this.type).append("exactType", this.exactType).toString();
    }
}
