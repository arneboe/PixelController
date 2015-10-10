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
package com.neophob.sematrix.core.resize;

/**
 * This filter is optimized for pixel oriented images.
 * 
 * @author michu
 */
class NearestNeighbourResize extends Resize {

    public NearestNeighbourResize() {
        super(ResizeName.PIXEL_RESIZE);
    }

    /**
     * @note copy&paste from http://tech-algorithm.com/articles/nearest-neighbor-image-scaling/
     * @param buffer
     * @param w1 current width
     * @param h1 current height
     * @param w2 new width
     * @param h2 new height
     * @return
     */
    public int[] resizeImage(final int[] buffer, final int w1, final int h1, final int w2, final int h2) {
        int[] temp = new int[w2*h2] ;
        final int x_ratio = (int)((w1<<16)/w2) +1;
        final int y_ratio = (int)((h1<<16)/h2) +1;
        int x2, y2;
        for (int i=0;i<h2;i++) {
            for (int j=0;j<w2;j++) {
                x2 = ((j*x_ratio)>>16) ;
                y2 = ((i*y_ratio)>>16) ;
                temp[(i*w2)+j] = buffer[(y2*w1)+x2] ;
            }
        }
        return temp;
    }

}
