package com.neophob.sematrix.core.visual.effect.Options;

public class FloatRangeOption implements IOption {

    private final String name;
    private final float min;
    private final float max;
    private float value;
    private boolean hasChanged = false;

    public FloatRangeOption(final String name, final float min, final float max, final float value)
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
    public void setValue(final float v) {
        value = v;
        hasChanged = true;
    }
    public boolean changed() {
      boolean ret = hasChanged;
      hasChanged = false;
        return ret;
    }
}
