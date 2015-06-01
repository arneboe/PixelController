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
 * create a strobo effect.
 *
 * @author michu, arne
 */
public class BpmStrobo extends Effect {

	private boolean on; /**< Whether the strobe is on or off*/
	private int offTime; /**<The time of half a strobe cycle. I.e. how long the strobe should be off */
	private int cycleTime; /**<The time between two flashes */
	private int flashTime; /**<How long a flash takes */
	private long lastTime; /**<The time of the last update() call in millis*/
	private long lastFlashTime; /**<Timestamp of the time that the flash was enabled */
	private int[] offBuffer; /**<buffer that is returned when the strobo is off */
	private FloatRangeOption bpmOption = new FloatRangeOption("BPM", 1, 300, 150);
	private FloatRangeOption flashTimeOption = new FloatRangeOption("Flash Len", 0.01f, 1, 0.01f);

	public BpmStrobo(MatrixData matrix) {
		super(matrix, EffectName.BPM_STROBO, ResizeName.QUALITY_RESIZE);
		lastTime = System.currentTimeMillis();
		setBpm((int)bpmOption.getValue());
		offBuffer = new int[1]; //will be resized later!
		options.add(bpmOption);
		options.add(flashTimeOption);
	}

	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.effect.Effect#getBuffer(int[])
	 */
	public int[] getBuffer(int[] buffer) {
		if (on) {
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
		setBpm((int)bpmOption.getValue());
		flashTime = (int)(flashTimeOption.getValue() * 1000.0f);
		final long currentTime = System.currentTimeMillis();
		if(currentTime - lastFlashTime >= flashTime) {
			on = false;
		}

		if(currentTime - lastTime >= cycleTime) {
			lastTime = currentTime;
			lastFlashTime = currentTime;
			on = true;
		}
	}

	public void setBpm(int bpm) {
		final double bpms = bpm / 60.0 / 1000.0;
		cycleTime = (int)(1.0/bpms);
	}
}
