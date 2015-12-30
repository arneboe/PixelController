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
package com.neophob;

import java.util.logging.Level;

import com.neophob.sematrix.gui.HardwareController.AkaiApcMiniController;
import com.neophob.sematrix.gui.HardwareController.HardwareControllerHandler;
import processing.core.PApplet;

import com.neophob.sematrix.gui.service.impl.LocalServer;


/**
 * The Class PixelController.
 *
 * @author michu
 */
public class PixelControllerP5 extends AbstractPixelControllerP5 {  

	private AkaiApcMiniController akai;
	private HardwareControllerHandler akaiHandler;
	public void initPixelController() {
		pixelController = new LocalServer(this);
		pixelController.start();
		LOG.log(Level.INFO, "LocalServer created");

		akai = new AkaiApcMiniController();
		if(akai.open()) {
			akaiHandler = new HardwareControllerHandler(pixelController, akai);
			LOG.log(Level.INFO, "Connected akai apc mini controller");
		}
		else
		{
			LOG.log(Level.INFO, "Could not connect to akai apc mini controller");
		}
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { PixelControllerP5.class.getName().toString() });
	}

}
