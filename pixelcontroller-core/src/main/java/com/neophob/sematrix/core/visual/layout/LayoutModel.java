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
package com.neophob.sematrix.core.visual.layout;

/**
 * helper class used to layout panels.
 * There is one LayoutModel per screen. It contains all information needed to
 * layout the visual (fx) on that screen.
 *
 * @author michu
 */
public class LayoutModel {
	
	private int sameFxOnX;
	private int sameFxOnY;
	private int visualId;
	private float screenFragmentX;
	private float screenFragmentY;
	private float xStart,xWidth;
	private float yStart,yWidth;
	
	public LayoutModel(int sameFxOnX, int sameFxOnY, int ofsX, int ofsY, int visualId) {
		this.sameFxOnX = sameFxOnX;
		this.sameFxOnY = sameFxOnY;
		this.visualId = visualId;
		
		if (!screenDoesNotNeedStretching()) {
			screenFragmentX = 1.0f/sameFxOnX;
/*			if (sameFxOnX<2) {
				screenFragmentX = 1.0f;
			} else {
				screenFragmentX = 1.0f/sameFxOnX;				
			}*/
			
			if (sameFxOnY<2) {
				screenFragmentY = 1.0f;
			} else {
				screenFragmentY = 1.0f/sameFxOnY;				
			}
			
			xStart = ofsX*screenFragmentX;
			xWidth = screenFragmentX;
			yStart = ofsY*screenFragmentY;
			yWidth = screenFragmentY;
		}
	}

	public boolean screenDoesNotNeedStretching() {
		return sameFxOnX==1 && sameFxOnY==1;
	}
	
	public int getxStart(int bufferWidth) {
		return (int)(xStart*bufferWidth);
	}
	public int getxWidth(int bufferWidth) {
		return (int)(xWidth*bufferWidth);
	}
	public int getyStart(int bufferHeight) {
		return (int)(yStart*bufferHeight);
	}
	public int getyWidth(int bufferHeight) {
		return (int)(yWidth*bufferHeight);
	}
	public int getVisualId() {
		return visualId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("LayoutModel [sameFxOnX=%s, sameFxOnY=%s, visualId=%s, screenFragmentX=%s, screenFragmentY=%s, xStart=%s, xWidth=%s, yStart=%s, yWidth=%s]",
						sameFxOnX, sameFxOnY, visualId,
						screenFragmentX, screenFragmentY, xStart, xWidth,
						yStart, yWidth);
	}
	
	
}
