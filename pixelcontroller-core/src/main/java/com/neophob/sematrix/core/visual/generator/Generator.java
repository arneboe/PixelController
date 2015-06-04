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
package com.neophob.sematrix.core.visual.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.core.visual.effect.Options.IOption;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.IShuffleState;
import com.neophob.sematrix.core.visual.MatrixData;

/**
 * matrix display buffer class
 * 
 * the internal buffer is much larger than the actual device. the buffer for the
 * matrix is recalculated each frame. reason: better display quality
 * 
 * @author mvogt
 * 
 */
public abstract class Generator implements IShuffleState {

    /**
     * The Enum GeneratorName.
     */
    public enum GeneratorName {
        PASSTHRU(0),
        BLINKENLIGHTS(1),
        IMAGE(2),
        PLASMA(3),
        COLOR_SCROLL(4),
        FIRE(5),
        METABALLS(6),
        PIXELIMAGE(7),
        COLOR_FADE(8),
        DROPS(9),
        CELL(10),
        PLASMA_ADVANCED(11),
        STATIC_COLOR(12),
        /* use the visual 0 as generator */
        VISUAL_ZERO(13),
        NOISE(14);

        /** The id. */
        private int id;

        /**
         * Instantiates a new generator name.
         * 
         * @param id
         *            the id
         */
        GeneratorName(int id) {
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

    /** The log. */
    private static final Logger LOG = Logger.getLogger(Generator.class.getName());

    /** The name. */
    private GeneratorName name;

    /** The resize option. */
    private ResizeName resizeOption;

    // internal, larger buffer
    /** The internal buffer. */
    public int[] internalBuffer;

    /** The internal buffer x size. */
    protected int internalBufferXSize;

    /** The internal buffer y size. */
    protected int internalBufferYSize;

    /** is the generator selected and thus active? */
    protected boolean active;

    /**options of this generator */
    protected List<IOption> options = new ArrayList<IOption>(5);

    /**
     * Instantiates a new generator.
     * @param name
     *            the name
     * @param resizeOption
     *            the resize option
     */
    public Generator(MatrixData matrix, GeneratorName name, ResizeName resizeOption) {
        this.name = name;
        this.resizeOption = resizeOption;
        this.internalBufferXSize = matrix.getBufferXSize();
        this.internalBufferYSize = matrix.getBufferYSize();
        this.internalBuffer = new int[internalBufferXSize * internalBufferYSize];

        LOG.log(Level.INFO,
                "Generator: internalBufferSize: {0} ({1}/{2}), name: {3}, resize option: {4} ",
                new Object[] { internalBuffer.length, internalBufferXSize, internalBufferYSize,
                        name, resizeOption.toString() });

        // add to list
        this.active = false;
    }

    /**
     * update the generator.
     */
    public abstract void update(int amount);

    /**
     * deinit generator.
     */
    public void close() {
        // nothing todo
    }

    /**
     * Gets the internal buffer x size.
     * 
     * @return the internal buffer x size
     */
    public int getInternalBufferXSize() {
        return internalBufferXSize;
    }

    /**
     * Gets the internal buffer y size.
     * 
     * @return the internal buffer y size
     */
    public int getInternalBufferYSize() {
        return internalBufferYSize;
    }

    /**
     * Gets the internal buffer size.
     * 
     * @return the internal buffer size
     */
    public int getInternalBufferSize() {
        return internalBuffer.length;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public GeneratorName getName() {
        return name;
    }

    /**
     * Gets the resize option.
     * 
     * @return the resize option
     */
    public ResizeName getResizeOption() {
        return resizeOption;
    }

    /**
     * Gets the buffer.
     * 
     * @return the buffer
     */
    public int[] getBuffer() {
        return internalBuffer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.neophob.sematrix.core.glue.RandomizeState#shuffle()
     */
    public void shuffle() {
        // default shuffle method - do nothing
    }

    /**
     * this method get called if the generator gets activated
     */
    protected void nowActive() {

    }

    /**
     * this method get called if the generator gets inactive
     */
    protected void nowInactive() {

    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public int getId() {
        return this.name.getId();
    }

    /**
     * is generator selected?
     * 
     * @return
     */
    public boolean isActive() {
        return active;
    }

    /**
     * is generator in use?
     * 
     * @return
     */
    public boolean isInUse() {
        return true;
    }

    /**
     * update state
     * 
     * @param active
     */
    public void setActive(boolean active) {
        if (!active && this.active) {
            nowInactive();
        } else if (active && !this.active) {
            nowActive();
        }
        this.active = active;
    }

    /**
     * pass through mode means that only one generator is used, no mixer, no
     * effect. use case: 24bpp input from the osc generator video screenshot
     * 
     * @return
     */
    public boolean isPassThoughModeActive() {
        return false;
    }

    @Override
    public String toString() {
        return String
                .format("Generator [name=%s, resizeOption=%s, internalBufferXSize=%s, internalBufferYSize=%s, active=%s]",
                        name, resizeOption, internalBufferXSize, internalBufferYSize, active);
    }


    //FIXME copy&paste code from effect
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

    public List<IOption> getOptions()
    {
        return options;
    }

}
