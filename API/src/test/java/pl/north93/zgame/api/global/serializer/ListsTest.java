package pl.north93.zgame.api.global.serializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import pl.north93.zgame.api.global.serializer.msgpack.MsgPackSerializationFormat;
import pl.north93.zgame.api.global.serializer.platform.NorthSerializer;
import pl.north93.zgame.api.global.serializer.platform.impl.NorthSerializerImpl;

public class ListsTest
{
    private final NorthSerializer<byte[]> serializer = new NorthSerializerImpl<>(new MsgPackSerializationFormat());

    @Test
    public void emptyArrayListTest()
    {
        final byte[] bytes = this.serializer.serialize(ArrayList.class, new ArrayList<>());
        final Object deserialized = this.serializer.deserialize(ArrayList.class, bytes);

        Assert.assertSame(ArrayList.class, deserialized.getClass());

        final List deserializedList = (List) deserialized;
        Assert.assertEquals(0, deserializedList.size());
    }

    @Test
    public void arrayListWithStrings()
    {
        final ArrayList<String> strings = new ArrayList<>(Arrays.asList("test1", "test2", "test3"));

        final byte[] bytes = this.serializer.serialize(ArrayList.class, strings);
        final Object deserialized = this.serializer.deserialize(ArrayList.class, bytes);

        Assert.assertSame(ArrayList.class, deserialized.getClass());

        final List deserializedList = (List) deserialized;
        Assert.assertEquals(strings, deserializedList);
    }

    @Test
    public void arrayListWithManyTypes()
    {
        final ArrayList<Object> strings = new ArrayList<>(Arrays.asList(100, "test", true));

        final byte[] bytes = this.serializer.serialize(ArrayList.class, strings);
        final Object deserialized = this.serializer.deserialize(ArrayList.class, bytes);

        Assert.assertSame(ArrayList.class, deserialized.getClass());

        final List deserializedList = (List) deserialized;
        Assert.assertEquals(strings, deserializedList);
    }
}
