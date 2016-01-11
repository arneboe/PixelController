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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.sound.ISound;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.effect.Options.FloatValueOption;
import com.neophob.sematrix.core.visual.effect.Options.SelectionListOption;

/**
 * create some drops
 * 
 * TODO add more geometrics forms (ellipse, rectangle...) replace Math.sqrt with
 * something faster
 * 
 * @author michu
 */
public class Geometrics extends Generator {


    private FloatValueOption thicknessOption = new FloatValueOption("THICKNESS", 1, 30, 10);
    private FloatValueOption maxDropsOption = new FloatValueOption("MAX_DROPS", 1, 20, 5);
    private SelectionListOption startLocationOption = new SelectionListOption("START_LOCATION");
    private FloatValueOption lowerColorLimitOption = new FloatValueOption("MIN_COLOR", 0, 255, 10);
    private FloatValueOption upperColorLimitOption = new FloatValueOption("MAX_COLOR", 0, 255, 128);

    private List<Drop> drops;
    private List<Drop> tmp;
    private ISound sound;
    public int[] internalBufferTmp;
    private Random rndGen = new Random();

    public Geometrics(MatrixData matrix, ISound sound) {
        super(matrix, GeneratorName.DROPS, ResizeName.QUALITY_RESIZE);
        drops = new ArrayList<Drop>();
        tmp = new ArrayList<Drop>();
        this.sound = sound;
        options.add(thicknessOption);
        options.add(maxDropsOption);
        options.add(lowerColorLimitOption);
        options.add(upperColorLimitOption);
        startLocationOption.addEntry("RANDOM");
        startLocationOption.addEntry("CENTER");
        startLocationOption.addEntry("BOTTOM");
        startLocationOption.addEntry("TOP");
        startLocationOption.setValue(0);
        options.add(startLocationOption);

        internalBufferTmp = new int[internalBuffer.length];
    }

    private int random(int min, int max) {
        int ret = rndGen.nextInt(Math.abs(max - min));
        return ret + min;
    }

    private void addDrop() {
        final int thick = (int) thicknessOption.getValue();
        int x = 0;
        int y = 0;
        final String startLocation = startLocationOption.getSelected();
        if(startLocation.equals("RANDOM")) {
            x = random(thick, internalBufferXSize);
            y = random(thick, internalBufferYSize);
        }
        else if(startLocation.equals("TOP")) {
            x = random(thick, internalBufferXSize);
            y = 0;
        }
        else if(startLocation.equals("BOTTOM")) {
            x = random(thick, internalBufferXSize);
            y = internalBufferYSize - 1;
        }
        else if(startLocation.equals("CENTER")) {
            x = internalBufferXSize / 2;
            y = internalBufferYSize / 2;
        }
        final int lowerColorLimit = (int)lowerColorLimitOption.getValue();
        int upperColorLimit = (int)upperColorLimitOption.getValue();
        if(upperColorLimit <= lowerColorLimit) upperColorLimit = lowerColorLimit + 1;
        drops.add(new Drop(x, y, random(lowerColorLimit, upperColorLimit)));
    }
    @Override
    public void update(int amount) {
        final int maxDrops = (int)maxDropsOption.getValue();
        if ((sound.isBeat() || drops.isEmpty()) && drops.size() < maxDrops) {
            addDrop();
        }

        tmp.clear();

        // clear background
        Arrays.fill(this.internalBufferTmp, 0);
        for (Drop d : drops) {
            d.update(amount);
            if (d.done()) {
                tmp.add(d);
            }
        }

        // copy temp buffer to internal buffer, fixes flickering
        System.arraycopy(internalBufferTmp, 0, internalBuffer, 0, internalBuffer.length);

        // remove drops that are updated
        if (!tmp.isEmpty()) {
            drops.removeAll(tmp);
        }
    }

    /**
     * Class for Raindrops effect.
     * 
     * @author michu
     */
    private final class Drop {

        /** The drop size. */
        int xpos, ypos, dropcolor, dropSize;

        /** The finished. */
        boolean finished;

        /**
         * Instantiates a new drop.
         */
        private Drop(int x, int y, int color) {
            xpos = x;
            ypos = y;
            dropcolor = color;
            finished = false;
        }

        /**
         * Update.
         */
        private void update(int amount) {
            for (int n = 0; n < amount; n++) {
                if (!finished) {
                    if (dropSize < internalBufferXSize * 2) {
                        dropSize++;
                    } else {
                        finished = true;
                    }
                }
            }
            drawCircle((int) thicknessOption.getValue());
        }

        /**
         * Done.
         * 
         * @return true, if successful
         */
        private boolean done() {
            return finished;
        }

        /**
         * draw circle
         */
        private void drawCircle(final int thickness) {
            int dropsizeThickness = dropSize - thickness;

            boolean drawOnscreen = false;
            for (int i = 0; i < internalBufferXSize; i++) {
                for (int j = 0; j < internalBufferYSize; j++) {
                    // calculate distance to center:
                    int x = xpos - i;
                    int y = ypos - j;
                    double r = Math.sqrt((x * x) + (y * y));

                    if (r < dropSize && r > dropsizeThickness) {
                        if (j >= 0 && j < internalBufferYSize && i >= 0 && i < internalBufferXSize) {
                            internalBufferTmp[j * internalBufferXSize + i] = dropcolor;
                            drawOnscreen = true;
                        }
                    }
                }
            }

            // detect if the circle is finished
            if (dropSize > thickness && !drawOnscreen) {
                finished = true;
            }
        }
    }

}
