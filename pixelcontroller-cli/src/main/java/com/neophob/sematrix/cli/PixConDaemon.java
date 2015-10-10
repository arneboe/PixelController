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
package com.neophob.sematrix.cli;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.core.api.CallbackMessage;
import com.neophob.sematrix.core.api.PixelController;
import com.neophob.sematrix.core.api.impl.PixelControllerFactory;
import com.neophob.sematrix.core.perf.PerfTests;

/**
 * PixelController (local) CLI Daemon
 * 
 * @author michu
 * 
 */
public class PixConDaemon extends CallbackMessage<String> {

    /** The log. */
    private static final Logger LOG = Logger.getLogger(PixConDaemon.class.getName());

    private PixelController pixelController;

    /**
     * 
     */
    public PixConDaemon() {
        LOG.log(Level.INFO, "Initialize...");
        pixelController = PixelControllerFactory.initialize(this);
        LOG.log(Level.INFO, "\n\nPixelController " + pixelController.getVersion()
                + " - http://www.pixelinvaders.ch\n\n");
        pixelController.start();

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore it
            }
        }
    }

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("-perf")) {
            LOG.log(Level.INFO, "Basic Performance tests");
            new PerfTests(10, 8).startPerfTest();
            new PerfTests(500000, 8).startPerfTest();
            new PerfTests(500000, 16).startPerfTest();
            new PerfTests(500000, 32).startPerfTest();
            return;
        }
        new PixConDaemon();
    }

    @Override
    public void handleMessage(String msg) {
        // LOG.info(msg);
    }

}
