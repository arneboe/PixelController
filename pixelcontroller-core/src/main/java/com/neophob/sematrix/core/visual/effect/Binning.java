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

import java.util.Random;

/**
 * @author michu
 */
public class Binning extends Effect {

	private FloatRangeOption numBins = new FloatRangeOption("Bins", 2, 40, 3);
	private int binCount = 3;
	private int binColors[] = new int[3];
	private Random random = new Random();
	public Binning(MatrixData matrix) {
		super(matrix, EffectName.BINNING, ResizeName.PIXEL_RESIZE);
		options.add(numBins);
		binColors[0] = 0;
		binColors[1] = 128;
		binColors[2] = 255;
	}

	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.effect.Effect#getBuffer(int[])
	 */
	public int[] getBuffer(int[] buffer) {
		int[] ret = new int[buffer.length];
		int colorDistance = 256 / binCount + 1;
		if(binCount != (int) numBins.getValue()) {
			binCount = (int) numBins.getValue();
			binColors = new int[binCount];
			colorDistance = 256 / binCount  + 1;
			final int dist = 256 / (binCount - 1) - 1;
			for(int i = 0; i < binColors.length; ++i) {
				binColors[i] = dist * i;
			}
		}
		for (int i=0; i<buffer.length; i++) {
			try {

				ret[i] = binColors[(buffer[i] / colorDistance)];

			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.print("b");
			}
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.effect.Effect#shuffle()
	 */
	@Override
	public void shuffle() {
		final int max = (int)numBins.getUpper();
		final int min = (int)numBins.getLower();
		numBins.setValue(random.nextInt((max - min) + 1) + min);
	}
}
