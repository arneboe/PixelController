package com.neophob.sematrix.core.visual.effect.Options;


import java.util.ArrayList;
import java.util.List;

/**Small wrapper around the option list to distinguish between options for different things */
public class Options {
    public enum Target {
        EFFECT_A,
        EFFECT_B,
        GEN_A,
        GEN_B
    }
    private List<IOption> options = new ArrayList<IOption>();
    private Target target = Target.EFFECT_A;

    public Options(List<IOption> opts, Target target)
    {
        this.options = opts;
        this.target = target;
    }

    public List<IOption> getOptions() {
        return options;
    }

    public Target getTarget() {
        return target;
    }

}
