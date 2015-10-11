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
import com.neophob.sematrix.core.visual.effect.Options.FloatRangeOption;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * create a strobo effect.
 *
 * @author michu, arne
 */
public class BeatStrobo extends Effect {

	private boolean on; /**< Whether the strobe is on or off*/
	private int cycleTime; /**<The time between two flashes */
	private int flashTime; /**<How long a flash takes */
	private long lastTime; /**<The time of the last update() call in millis*/
	private long lastFlashTime; /**<Timestamp of the time that the flash was enabled */
	private int[] offBuffer; /**<buffer that is returned when the strobo is off */
	private FloatRangeOption flashTimeOption = new FloatRangeOption("LENGTH", 0.01f, 1, 0.05f);
	private ISound sound;

	public BeatStrobo(MatrixData matrix, ISound sound) {
		super(matrix, EffectName.BPM_STROBO, ResizeName.QUALITY_RESIZE);
		this.sound = sound;
		lastTime = System.currentTimeMillis();
		offBuffer = new int[1]; //will be resized later!
		options.add(flashTimeOption);
	}


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

    @Override
	public void update() {
		final long currentTime = System.currentTimeMillis();
		if(sound.isBeat())
		{
            System.out.println("beat " + getCurrentTimeStamp());
			on = true;
			lastFlashTime = currentTime;
		}
		else if(currentTime - lastFlashTime >= flashTime)
		{
			on = false;
		}
	}

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

}
