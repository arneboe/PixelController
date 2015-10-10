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
package com.neophob.sematrix.core.visual.fader;

import com.neophob.sematrix.core.visual.MatrixData;

/**
 * The Class Switch.
 */
public class Switch extends Fader {

    /**
     * Instantiates a new switch.
     */
    public Switch(MatrixData matrix, float fps) {
        super(matrix, FaderName.SWITCH, 50, fps);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.neophob.sematrix.core.fader.Fader#getBuffer(int[])
     */
    @Override
    public int[] getBuffer(int[] visual1Buffer, int[] visual2Buffer) {
        return visual1Buffer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.neophob.sematrix.core.fader.Fader#isDone()
     */
    @Override
    public boolean isDone() {
        return true;
    }

}
