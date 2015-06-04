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
package com.neophob.sematrix.core.glue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

/**
 * The Enum ShufflerOffset.
 *
 * @author michu
 */
public enum ShufflerOffset {
	
	GENERATOR_A(0),
	GENERATOR_B(1),
	EFFECT_A(2),
	EFFECT_B(3),
	MIXER(4),
	MIXER_OUTPUT(5),
	FADER_OUTPUT(6),
	OUTPUT(7),
	EFFECT_OPTIONS(8),
	GENERATOR_OPTIONS(9),
	BEAT_WORK_MODE(10),
	GENERATORSPEED(11),
	COLORSET(12);

	/** The ofs. */
	private int ofs;
	
	/**
	 * Instantiates a new shuffler offset.
	 *
	 * @param ofs the ofs
	 */
	ShufflerOffset(int ofs) {
		this.ofs = ofs;
	}
	
	/**
	 * Gets the offset.
	 *
	 * @return the offset
	 */
	int getOffset() {
		return ofs;
	}
	
	/**
	 * 
	 * @return
	 */
	public String guiText() {
		return WordUtils.capitalizeFully(StringUtils.replace(this.name(), "_", " "));		
	}
}
