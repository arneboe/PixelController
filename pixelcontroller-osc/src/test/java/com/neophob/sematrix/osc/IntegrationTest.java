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
package com.neophob.sematrix.osc;

import com.neophob.sematrix.osc.model.OscMessage;
import com.neophob.sematrix.osc.server.OscMessageHandler;
import com.neophob.sematrix.osc.server.OscServerException;
import com.neophob.sematrix.osc.server.PixOscServer;
import com.neophob.sematrix.osc.server.impl.OscServerFactory;


public class IntegrationTest extends OscMessageHandler {

	PixOscServer srv;
	
	public IntegrationTest() throws OscServerException {
		System.out.println("create server");
		srv = OscServerFactory.createServerUdp(this, 9876, 1500);
		srv.startServer();
		System.out.println("done");
	}
	
	public void mainLoop() throws Exception {
		System.out.println("enter mainloop");
		while (true) {
//			System.out.println("packets: "+srv.getPacketCounter());
//			Thread.sleep(444);
		}
	}
	public void handleOscMessage(OscMessage msg) {
		System.out.println("Check");
		System.out.println(msg);		
	}

	public static void main(String args[]) throws Exception {
		new IntegrationTest().mainLoop();    
		System.out.println("bye");
	}


}
