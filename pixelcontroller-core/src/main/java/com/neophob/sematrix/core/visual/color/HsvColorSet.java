package com.neophob.sematrix.core.visual.color;

import com.neophob.sematrix.core.common.MathHelpers;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;

/**
 * A ColorSet that is initialized using hsv colors
 */
//FIXME this class should extend ColorSet because most of the methods are identical!
public class HsvColorSet implements IColorSet
{
    private final String name;
    private ArrayList<HsvColor> colors;
    /**look up table for rgb values*/
    private transient int[] precalc;

    public HsvColorSet(final String name, final ArrayList<HsvColor> colors)
    {
        this.name = name;
        this.colors = colors;
        precalc = new int[256];
        precalculateRbgValues(precalc, colors);
    }

    private void precalculateRbgValues(final int[] lut, final ArrayList<HsvColor> colors)
    {
        if(colors.size() == 0)
        {
            throw new RuntimeException("A hsv colorset should consist of at least one color");
        }
        else if(colors.size() == 1)
        {
            throw new NotImplementedException();
        }
        else
        {
            //FIXME use the rainbow color map from fastled instead of the spectrum one
            final int blockSize = 127 / (colors.size() - 1);
            int startIdx = 0;
            for (int i = 0; i < colors.size() - 1; ++i) {
                final HsvColor currentColor = colors.get(i);
                final HsvColor nextColor = colors.get(i + 1);
                if(currentColor.skip()) {
                    continue;
                }
                int endIdx = startIdx + blockSize;
                if (i == colors.size() - 2) {//if 127 is not divisable by colors.size() we ll compensate for that by making the last block larger
                    endIdx = 128; //is exclusive, therefore 128 is correct
                }
                interpolateColors(currentColor, nextColor, startIdx, endIdx, lut);
                startIdx = endIdx;
            }
            //copy inverted colors
            int j = 127;
            for(int i = 128; i < 256; ++i, --j) {
                lut[i] = lut[j];
            }

        }
    }

    /**
     * Fills the lookup table
     * @param from color of startIdx
     * @param to color of endIdx
     * @param startIdx first index that will be filled
     * @param endIdx first index that will not be filled anymore
     * @param lut the look up table that will be filled
     */
    private void interpolateColors(HsvColor from, HsvColor to, final int startIdx, final int endIdx, final int[] lut)
    {
        assert(endIdx > startIdx);
        final int numValues = endIdx - startIdx;
        final double[] hs = MathHelpers.linspace(from.h, to.h, numValues);
        final double[] ss = MathHelpers.linspace(from.s, to.s, numValues);
        final double[] vs = MathHelpers.linspace(from.v, to.v, numValues);

        for(int i = startIdx; i < endIdx; ++i)
        {
            final int colIdx = i - startIdx;
            //division is done to convert to java.awt.color specification
            final double h = hs[colIdx] / 360f;
            final double s = ss[colIdx] / 100f;
            final double v = vs[colIdx] / 100f;
            lut[i] = java.awt.Color.HSBtoRGB((float)h, (float)s, (float)v);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSmoothColor(int pos)
    {
        assert(pos >= 0);
        assert(pos < 256);
        return precalc[pos];
    }

    @Override
    public int[] convertToColorSetImage(int[] buffer)
    {
        final int len = buffer.length;
        int[] ret = new int[len];
        for (int i = 0; i < len; i++) {
            // use only 8bpp here!
            ret[i] = precalc[buffer[i] & 255];
        }
        return ret;
    }

    @Override
    public int compareTo(IColorSet otherColorSet)
    {
        if (otherColorSet.getName() == null && this.getName() == null) {
            return 0;
        }
        if (this.getName() == null) {
            return 1;
        }
        if (otherColorSet.getName() == null) {
            return -1;
        }
        return this.getName().compareTo(otherColorSet.getName());
    }
}
