package com.neophob.sematrix.core.visual.color;

import java.awt.*;

public class HsvColor
{
    public int h;
    public int s;
    public int v;
    public boolean skip = false;
    public HsvColor(final int h, final int s, final int v)
    {
        this.h = h;
        this.s = s;
        this.v = v;
    }
    public HsvColor(final boolean skip) {
        this.skip = skip;
    }

    public static HsvColor fromRGB(final int r, final int g, final int b) {
        float[] comp = new float[3];
        Color.RGBtoHSB(r, g, b, comp);
        comp[0]*= 360;
        comp[1]*= 100;
        comp[2]*= 100;
        return new HsvColor((int)comp[0], (int)comp[1], (int)comp[2]);
    }

    /**If true this is not a real. instead it is a skip marker */
    public boolean skip() {
        return skip;
    }
}