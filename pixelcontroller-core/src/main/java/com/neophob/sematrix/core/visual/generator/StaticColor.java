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

import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.effect.Options.FloatValueOption;

import java.util.Arrays;

public class StaticColor extends Generator {

    private int frameCount;
    private FloatValueOption colorOption = new FloatValueOption("Color", 0, 128, 64);

    public StaticColor(MatrixData matrix) {
        super(matrix, GeneratorName.STATIC_COLOR, ResizeName.PIXEL_RESIZE);
        options.add(colorOption);
    }

    @Override
    public void update(int amount) {
        Arrays.fill(this.internalBuffer, (int) colorOption.getValue());
    }
}