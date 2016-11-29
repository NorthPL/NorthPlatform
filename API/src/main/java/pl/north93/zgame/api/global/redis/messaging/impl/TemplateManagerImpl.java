package pl.north93.zgame.api.global.redis.messaging.impl;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import pl.north93.zgame.api.global.redis.messaging.Template;
import pl.north93.zgame.api.global.redis.messaging.TemplateFactory;
import pl.north93.zgame.api.global.redis.messaging.TemplateGeneric;
import pl.north93.zgame.api.global.redis.messaging.TemplateManager;
import pl.north93.zgame.api.global.redis.messaging.annotations.MsgPackUseTemplateOf;
import pl.north93.zgame.api.global.redis.messaging.templates.ArrayListTemplate;
import pl.north93.zgame.api.global.redis.messaging.templates.BooleanTemplate;
import pl.north93.zgame.api.global.redis.messaging.templates.IntegerTemplate;
import pl.north93.zgame.api.global.redis.messaging.templates.LongTemplate;
import pl.north93.zgame.api.global.redis.messaging.templates.ShortTemplate;
import pl.north93.zgame.api.global.redis.messaging.templates.StringTemplate;
import pl.north93.zgame.api.global.redis.messaging.templates.UuidTemplate;

public class TemplateManagerImpl implements TemplateManager
{
    private final TemplateFactory        templateFactory;
    private final Map<Class, Template>   templateCache;

    public TemplateManagerImpl(final TemplateFactory templateFactory)
    {
        this.templateFactory = templateFactory;
        this.templateCache = new IdentityHashMap<>();

        this.registerTemplate(ArrayList.class, new ArrayListTemplate(null));
        this.registerTemplate(Boolean.class, new BooleanTemplate());
        this.registerTemplate(Integer.class, new IntegerTemplate());
        this.registerTemplate(Short.class, new ShortTemplate());
        this.registerTemplate(Long.class, new LongTemplate());
        this.registerTemplate(String.class, new StringTemplate());
        this.registerTemplate(UUID.class, new UuidTemplate());
    }

    @Override
    public void registerTemplate(final Class<?> clazz, final Template template)
    {
        if (clazz.isInterface())
        {
            throw new IllegalArgumentException("Can't register template for interface.");
        }
        this.templateCache.put(clazz, template);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Template<T> getTemplate(final Class<T> clazz)
    {
        final Template fromCache = this.templateCache.get(clazz);
        if (fromCache != null)
        {
            return (Template<T>) fromCache;
        }

        final boolean isCustomTemplate = clazz.isAnnotationPresent(MsgPackUseTemplateOf.class);
        if (isCustomTemplate)
        {
            return this.getTemplate((Class<T>) clazz.getAnnotation(MsgPackUseTemplateOf.class).value());
        }

        if (clazz.isInterface() || clazz == Object.class)
        {
            return new DynamicTemplate<>(); // templatka która będzie w locie zapisywać konkretny typ i go odczytywać
        }

        final Template template = this.templateFactory.createTemplate(this, clazz);
        this.registerTemplate(clazz, template);
        return template;
    }

    @Override
    public <T> Template<?> getTemplate(final Class<T> clazz, final ParameterizedType genericType)
    {
        final TemplateGeneric template = (TemplateGeneric) this.getTemplate(clazz);
        return template.setGenericType(genericType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Template<?> getTemplate(final Class<T> clazz, final Class<?> genericType)
    {
        final TemplateGeneric template = (TemplateGeneric) this.getTemplate(clazz);
        return template.setGenericType(genericType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] serialize(final Object object)
    {
        return this.serialize(object.getClass(), object);
    }

    @Override
    public byte[] serialize(final Class<?> clazz, final Object object)
    {
        final Template template = this.getTemplate(clazz);
        final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();

        try
        {
            //noinspection unchecked
            template.serializeObject(this, packer, object);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("An exception has been thrown while serializing: " + object, e);
        }

        return packer.toByteArray();
    }

    @Override
    public <T> T deserialize(final Class<T> clazz, final byte[] bytes)
    {
        final MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);
        final Template template = this.getTemplate(clazz);

        try
        {
            //noinspection unchecked
            return (T) template.deserializeObject(this, unpacker);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("An exception has been thrown while deserializing object of type: " + clazz.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] serializeList(final Class<T> listOf, final List<T> list)
    {
        final Template template = this.getTemplate(ArrayList.class, listOf);
        final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();

        try
        {
            template.serializeObject(this, packer, list);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("An exception has been thrown while serializing:" + list, e);
        }

        return packer.toByteArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> deserializeList(final Class<T> listOf, final byte[] bytes)
    {
        final Template template = this.getTemplate(ArrayList.class, listOf);
        final MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);

        try
        {
            return (List<T>) template.deserializeObject(this, unpacker);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("An exception has been thrown while deserializing list", e);
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("templateFactory", this.templateFactory).append("templateCache", this.templateCache).toString();
    }
}
