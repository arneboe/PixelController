/**
 * Copyright (C) 2011-2014 Michael Vogt <michu@neophob.com>
 *
 * This file is part of PixelController.
 *
 * PixelController is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PixelController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PixelController.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.neophob.sematrix.core.visual.effect;

import com.neophob.sematrix.core.visual.effect.Options.IOption;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.IShuffleState;
import com.neophob.sematrix.core.visual.MatrixData;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Effect.
 */
public abstract class Effect implements IShuffleState {

    /**
     * The Enum EffectName.
     * 
     * @author michu
     */
    public enum EffectName {
        PASSTHRU(0),
        INVERTER(1),
        ROTOZOOM(2),
        BEAT_HORIZONTAL_SHIFT(3),
        BEAT_VERTICAL_SHIFT(4),
        VOLUMINIZE(5),
        THRESHOLD(6),
        TEXTURE_DEFORMATION(7),
        ZOOM(8),
        FLIP_X(9),
        FLIP_Y(10),
        BPM_STROBO(11),
        ROTATE90(13),
        POSTERIZE(14),
        MOVING_WINDOW(15);

        /** The id. */
        private int id;

        /**
         * Instantiates a new effect name.
         * 
         * @param id
         *            the id
         */
        EffectName(int id) {
            this.id = id;
        }

        /**
         * Gets the id.
         * 
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * 
         * @return
         */
        public String guiText() {
            return WordUtils.capitalizeFully(StringUtils.replace(this.name(), "_", " "));
        }
    }

    /** The effect name. */
    private EffectName effectName;

    /** The resize option. */
    private ResizeName resizeOption;

    /** The internal buffer x size. */
    protected int internalBufferXSize;

    /** The internal buffer y size. */
    protected int internalBufferYSize;

    /**options of this effect */
    protected List<IOption> options = new ArrayList<IOption>(5);

    /**
     * Instantiates a new effect.
     *
     */
    public Effect(MatrixData matrix, EffectName effectName, ResizeName resizeOption) {
        this.effectName = effectName;
        this.resizeOption = resizeOption;
        this.internalBufferXSize = matrix.getBufferXSize();
        this.internalBufferYSize = matrix.getBufferYSize();
    }

    /**
     * return the image buffer.
     * 
     * @param buffer
     *            the buffer
     * @return the buffer
     */
    public abstract int[] getBuffer(int[] buffer);

    /**
     * Gets the resize option.
     * 
     * @return the resize option
     */
    public ResizeName getResizeOption() {
        return resizeOption;
    }

    /**
     * update an effect.
     */
    public void update() {
        // overwrite me if needed
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public int getId() {
        return this.effectName.getId();
    }

    /*
     * @see com.neophob.sematrix.core.glue.RandomizeState#shuffle()
     */
    public void shuffle() {
        // default shuffle method - do nothing
    }

    /**
     * @return the current state of all options. Used for saving
     */
    public String getOptionState() {
        String ret = "";
        for(IOption opt : options) {
            ret += opt.getName() + " " + opt.getValue() + " ";
        }
        return ret;
    }

    public void setOptionState(final String[] opts){
        for(int i = 0; i < opts.length; i += 2) {
            final String name = opts[i];
            final float value = Float.parseFloat(opts[i + 1]);
            for(IOption o :options) {
                if(o.getName().equals(name)) {
                    o.setValue(value);
                    break;
                }
            }
        }
    }

    @Override
    public String toString() {
        return String
                .format("Effect [effectName=%s, resizeOption=%s, internalBufferXSize=%s, internalBufferYSize=%s]",
                        effectName, resizeOption, internalBufferXSize, internalBufferYSize);
    }

    public List<IOption> getOptions()
    {
        return options;
    }
}
