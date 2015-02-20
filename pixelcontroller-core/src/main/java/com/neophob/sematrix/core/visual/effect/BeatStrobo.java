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
import com.neophob.sematrix.core.sound.ISound;
import com.neophob.sematrix.core.visual.MatrixData;

/**
 * create a strobo effect.
 *
 * @author michu, arne
 */
public class BeatStrobo extends Effect {

	private boolean on; /**< Whether the strobe is on or off*/
	private int[] offBuffer; /**<buffer that is returned when the strobo is off */
	private ISound sound;
	public BeatStrobo(MatrixData matrix, ISound sound) {
		super(matrix, EffectName.BEAT_STROBO, ResizeName.QUALITY_RESIZE);
		offBuffer = new int[1];
		this.sound = sound;
	}

	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.effect.Effect#getBuffer(int[])
	 */
	public int[] getBuffer(int[] buffer) {
		if (on) {
			on = false;
			return buffer;
		}
		else
		{
			if(offBuffer.length != buffer.length) {
				offBuffer = new int[buffer.length];
			}
			return offBuffer;
		}

	}
		
	/* (non-Javadoc)
     * @see com.neophob.sematrix.core.effect.Effect#update()
     */
    @Override
	public void update() {
		//if it was on before, leave it on
		on = sound.isPang() | on;
	}
}
