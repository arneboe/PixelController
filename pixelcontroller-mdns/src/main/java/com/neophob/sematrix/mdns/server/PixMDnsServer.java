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
package com.neophob.sematrix.mdns.server;

/**
 * OSC Server interface exposed to PixelController Core
 * 
 * @author michu
 * 
 */
public interface PixMDnsServer {

    String REMOTE_TYPE_UDP = "_osc._udp.local";
    String REMOTE_TYPE_TCP = "_osc._tcp.local";

    /**
     * start the OSC server, blocks until registered
     */
    void startServer();

    /**
     * start the OSC server async
     */
    void startServerAsync();

    /**
     * start the OSC server
     */
    void stopServer();

    /**
     * @return listening port of the osc server
     */
    int getListeningPort();

    /**
     * 
     * @return registered name
     */
    String getRegisterName();

    /**
     * 
     * @return
     */
    boolean isUsingTcp();
}
