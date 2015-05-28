package com.neophob.sematrix.core.visual.effect.Options;

import java.util.ArrayList;

/**
 * Created by arne on 5/28/15.
 */
public class SelectionListOption implements IOption {

    private final String name;
    private ArrayList<String> entries = new ArrayList<String>();
    private int selection = -1;


    public SelectionListOption(final String name) {
        this.name = name;
    }

    public void addEntry(final String value) {
        entries.add(value);
    }

    public void select(final int index) {
        selection = index;
    }

    @Override
    public String getName() {
        return name;
    }

    public ArrayList<String> getEntries() {
        return entries;
    }

    @Override
    /**
     * This is a really stupid interface but it works for controlp5.
     * We just use the float as index of the list
     */
    public void setValue(float value) {
        assert(value >= 0);
        assert(value < entries.size());
        select((int) value);
    }

    @Override
    public float getValue() {
        return selection;
    }
}
