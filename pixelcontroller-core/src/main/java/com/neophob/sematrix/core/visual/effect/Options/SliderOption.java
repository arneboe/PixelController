package com.neophob.sematrix.core.visual.effect.Options;

public class SliderOption implements IOption {

    private final String name;
    private final float min;
    private final float max;

    public SliderOption(final String name, final float min, final float max)
    {
        this.name = name;
        this.min = min;
        this.max = max;
    }
    @Override
    public String getName() {
        return name;
    }

    public float getLower()
    {
        return min;
    }

    public float getUpper()
    {
        return max;
    }
}
