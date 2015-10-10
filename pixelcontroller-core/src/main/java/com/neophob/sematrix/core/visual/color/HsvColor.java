package com.neophob.sematrix.core.visual.color;

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

    /**If true this is not a real. instead it is a skip marker */
    public boolean skip() {
        return skip;
    }
}