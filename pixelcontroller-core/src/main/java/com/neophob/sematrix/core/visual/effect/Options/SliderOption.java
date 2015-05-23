package com.neophob.sematrix.core.visual.effect.Options;

public class SliderOption implements IOption {

    private final String name;
    private final float min;
    private final float max;
    private float value;

    public SliderOption(final String name, final float min, final float max, final float value)
    {
        this.name = name;
        this.min = min;
        this.max = max;
        this.value = value;
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
    public float getValue() { return value; }
    public void setValue(final float v) { value = v; }
}
