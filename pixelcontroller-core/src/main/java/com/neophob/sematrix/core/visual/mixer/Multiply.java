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
package com.neophob.sematrix.core.visual.mixer;

import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.Visual;

/**
 * The Class Multiply.
 */
public class Multiply extends Mixer {

    /**
     * Instantiates a new multiply.
     * 
     * @param controller
     *            the controller
     */
    public Multiply() {
        super(MixerName.MULTIPLY, ResizeName.QUALITY_RESIZE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.neophob.sematrix.core.mixer.Mixer#getBuffer(com.neophob.sematrix.
     * core.glue.Visual)
     */
    public int[] getBuffer(Visual visual) {
        if (visual.getEffect2() == null) {
            return visual.getEffect1Buffer();
        }

        int[] src1 = visual.getEffect1Buffer();
        int[] src2 = visual.getEffect2Buffer();
        int[] dst = new int[src1.length];

        for (int i = 0; i < src1.length; i++) {
            int pixelOne = src1[i] & 255;
            int pixelTwo = src2[i] & 255;

            dst[i] = mul(pixelOne, pixelTwo);
        }

        return dst;
    }

    private static int norm(int v) {
        if (v > 127) {
            v = 255 - v;
        }
        return v;
    }

    private static int normDiff(int v) {
        if (v > 127) {
            return 255 - v;
        }
        return 0;
    }

    public static int mul(int u, int v) {
        int a = norm(u) * norm(v) / 128;
        int b = normDiff(u) * normDiff(v) / 128;
        if (b > 127) {
            return 255 - a;
        }
        return a;
    }

}
