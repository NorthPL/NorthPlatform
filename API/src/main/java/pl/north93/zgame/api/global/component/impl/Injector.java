package pl.north93.zgame.api.global.component.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

import pl.north93.zgame.api.global.API;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.component.annotations.bean.Named;

public class Injector
{
    private static final Field F_MODIFIERS;

    static
    {
        try
        {
            F_MODIFIERS = Field.class.getDeclaredField("modifiers");
            F_MODIFIERS.setAccessible(true);
        }
        catch (final NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void inject(final Object instance)
    {
        final ComponentManagerImpl manager = ComponentManagerImpl.instance;
        final Class<?> clazz = instance.getClass();

        final AbstractBeanContext context = manager.getOwningContext(clazz);

        for (final Field field : clazz.getDeclaredFields())
        {
            field.setAccessible(true);

            final Inject injectAnn = field.getAnnotation(Inject.class);
            if (injectAnn == null)
            {
                continue;
            }

            if (Modifier.isFinal(field.getModifiers()))
            {
                try
                {
                    F_MODIFIERS.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                }
                catch (final IllegalAccessException ignored) // will never happen
                {
                }
            }

            final BeanQuery query = new BeanQuery();
            query.type(field.getType());
            final Named namedAnn = field.getAnnotation(Named.class);
            if (namedAnn != null)
            {
                query.name(namedAnn.value());
            }

            final Object bean;
            try
            {
                bean = context.getBeanContainer(query).getValue(field);
            }
            catch (final Exception e)
            {
                API.getLogger().log(Level.SEVERE, "Failed to resolve bean when processing injection: " + instance.getClass().getName(), e);
                return;
            }

            try
            {
                field.set(instance, bean);
            }
            catch (final IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }
}
