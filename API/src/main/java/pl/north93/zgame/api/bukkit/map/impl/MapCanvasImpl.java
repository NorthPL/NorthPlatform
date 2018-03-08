package pl.north93.zgame.api.bukkit.map.impl;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.bukkit.map.MapPalette;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.bukkit.map.IMapCanvas;
import pl.north93.zgame.api.bukkit.map.MapColor;

final class MapCanvasImpl implements IMapCanvas
{
    private static final int SINGLE_MAP_SIDE = 128;
    private final int xSize, ySize;
    private final byte[] buffer;

    public MapCanvasImpl(final int xSize, final int ySize, final byte[] buffer)
    {
        this.xSize = xSize;
        this.ySize = ySize;
        this.buffer = buffer;
    }

    public MapCanvasImpl(final int xSize, final int ySize)
    {
        this(xSize, ySize, new byte[xSize * ySize]);
    }

    public static MapCanvasImpl createFromMaps(final int xMaps, final int yMaps)
    {
        return new MapCanvasImpl(xMaps * SINGLE_MAP_SIDE, yMaps * SINGLE_MAP_SIDE);
    }

    @Override
    public int getHeight()
    {
        return this.ySize;
    }

    @Override
    public int getWidth()
    {
        return this.xSize;
    }

    @Override
    public void setPixel(final int x, final int y, final byte color)
    {
        if (x < 0 || y < 0 || x >= this.xSize || y >= this.ySize)
            return;

        this.buffer[this.calculateIndex(x, y)] = color;
    }

    @Override
    public void putImage(final int modifierX, final int modifierY, final BufferedImage image)
    {
        final int width = image.getWidth();
        final int height = image.getHeight();

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                final byte color = (byte) MapColor.find(new Color(image.getRGB(x, y), true));
                this.setPixel(modifierX + x, modifierY + y, color);
            }
        }
    }

    @Override
    public void putCanvas(final int x, final int y, final IMapCanvas canvas)
    {
        final MapCanvasImpl impl = (MapCanvasImpl) canvas;

        for (int actualY = 0; actualY < impl.ySize; actualY++)
        {
            for (int actualX = 0; actualX < impl.xSize; actualX++)
            {
                final byte pixel = impl.getPixel(actualX, actualY);
                this.setPixel(actualX + x, actualY + y, pixel);
            }
        }
    }

    @Override
    public void fill(final byte color)
    {
        Arrays.fill(this.buffer, color);
    }

    @Override
    public byte getPixel(final int x, final int y)
    {
        return this.buffer[this.calculateIndex(x, y)];
    }

    @Override
    public byte[] getBytes()
    {
        return this.buffer;
    }

    public MapCanvasImpl getSubMapCanvas(final int xMap, final int yMap)
    {
        // definujemy nowa tablice na mape 128x128 pixeli
        final byte[] subMap = new byte[SINGLE_MAP_SIDE * SINGLE_MAP_SIDE];

        // szukamy punktu poczatkowego skad zaczynamy kopiowac
        final int startPoint = this.calculateIndex(xMap * 128, yMap * 128);

        for (int lines = 0, destLoc = 0; lines < SINGLE_MAP_SIDE; lines++, destLoc += SINGLE_MAP_SIDE)
        {
            // szukamy startu aktualnej linijki. kazda linijka ma 128 pixele
            final int currentLineStart = startPoint + (lines * this.xSize);
            System.arraycopy(this.buffer, currentLineStart, subMap, destLoc, SINGLE_MAP_SIDE);
        }

        return new MapCanvasImpl(SINGLE_MAP_SIDE, SINGLE_MAP_SIDE, subMap);
    }

    @Override
    public void writeDebugImage(final File location)
    {
        final BufferedImage image = new BufferedImage(this.xSize, this.ySize, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < this.xSize; x++)
        {
            for (int y = 0; y < this.ySize; y++)
            {
                image.setRGB(x, y, MapPalette.getColor(this.getPixel(x, y)).getRGB());
            }
        }
        try
        {
            ImageIO.write(image, "png", location);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public IMapCanvas clone()
    {
        final byte[] bytes = new byte[this.buffer.length];
        System.arraycopy(this.buffer, 0, bytes, 0, this.buffer.length);

        return new MapCanvasImpl(this.xSize, this.ySize, bytes);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || this.getClass() != o.getClass())
        {
            return false;
        }

        final MapCanvasImpl mapCanvas = (MapCanvasImpl) o;
        return Arrays.equals(this.buffer, mapCanvas.buffer);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(this.buffer);
    }

    private int calculateIndex(final int x, final int y)
    {
        return y * this.xSize + x;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("xSize", this.xSize).append("ySize", this.ySize).toString();
    }
}
