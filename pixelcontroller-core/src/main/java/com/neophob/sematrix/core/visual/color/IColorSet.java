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
package com.neophob.sematrix.core.visual.color;

public interface IColorSet extends Comparable<IColorSet> {

    /**
     * get ColorSet name
     * 
     * @return
     */
    String getName();

    void setName(final String n);

    /**
     * return a color defined in this color set
     * 
     * @param pos
     * @return
     */
    int getSmoothColor(int pos);

    /**
     * colorize an image buffer
     * 
     * @param buffer
     *            8bpp image
     * @param cs
     *            ColorSet to apply
     * @return 24 bpp image
     */
    int[] convertToColorSetImage(int[] buffer);

}