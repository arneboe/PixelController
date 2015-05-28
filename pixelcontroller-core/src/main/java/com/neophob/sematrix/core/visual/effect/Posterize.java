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

import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.effect.Options.FloatRangeOption;

/**
 * compress color information
 */
public class Posterize extends Effect {

    FloatRangeOption posterizeLevel = new FloatRangeOption("Level", 1, 20, 5);
    public Posterize(MatrixData matrix) {
        super(matrix, EffectName.POSTERIZE, ResizeName.QUALITY_RESIZE);
        options.add(posterizeLevel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.neophob.sematrix.core.effect.Effect#getBuffer(int[])
     */
    public int[] getBuffer(int[] buffer) {
        final int level = (int) posterizeLevel.getValue();
        int[] ret = new int[buffer.length];
        for (int i = 0; i < buffer.length; i++) {
            ret[i] = (buffer[i] >> level) << level;
        }
        return ret;
    }
}
