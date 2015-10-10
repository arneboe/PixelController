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
/**
 * blinkenlights processing lib.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author		Michael Vogt
 * @modified	16.12.2010
 * @version		v0.5
 */

package com.neophob.sematrix.core.visual.generator.blinken;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.neophob.sematrix.core.visual.generator.blinken.jaxb.Blm;
import com.neophob.sematrix.core.visual.generator.blinken.jaxb.Header;

/**
 * Blinkenlight files library
 * 
 */
public class BlinkenLibrary {

    private static final Logger LOG = Logger.getLogger(BlinkenLibrary.class.getName());

    // the marshalled .blm file
    private Blm blm;

    private BlinkenImage[] frames;

    private Unmarshaller unmarshaller;

    public static final String NAME = "blinkenlights-mini";
    public static final String VERSION = "v0.5";

    /**
     * 
     * @param parent
     */
    public BlinkenLibrary() {
        try {
            JAXBContext context = JAXBContext.newInstance(Blm.class.getPackage().getName());
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            LOG.log(Level.SEVERE, "Failed to initialize Blinkenlights lib!", e);
        }

    }

    /**
     * load a new bml file
     * 
     * @param filename
     * @param maximalSize
     *            maximal height or width of an image
     */
    public boolean loadFile(String filename) {
        long start = System.currentTimeMillis();

        try {
            // make sure input file exist
            long t1 = System.currentTimeMillis();
            blm = (Blm) unmarshaller.unmarshal(new File(filename));
            long t2 = System.currentTimeMillis();

            if (Integer.parseInt(blm.getChannels()) != 1) {
                LOG.log(Level.WARNING, "Bml file using " + blm.getChannels()
                        + " channels - only 1 channel files are supported!");
                return false;
            }
            this.frames = extractFrames(128);

            long timeNeeded = System.currentTimeMillis() - start;
            LOG.log(Level.INFO, "Loaded file {0} / {1} frames in {2}ms (Unmarshall: {3}ms)",
                    new Object[] { filename, frames.length, timeNeeded, t2 - t1 });

            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to load " + filename + ", Error: ", e);
            return false;
        }
    }

    /**
     * creates a PImage-array of gif frames in a GifDecoder object
     * 
     * @return
     */
    public BlinkenImage[] extractFrames(int color) {
        return BlinkenHelper.grabFrames(blm, color);
    }

    /**
     * total frame numbers of current movie
     * 
     * @return how many frames this movie contains
     */
    public int getNrOfFrames() {
        return blm.getFrame().size();
    }

    /**
     * get meta information (title, duration...) about the loaded file
     * 
     * @return the header object
     */
    public Header getHeader() {
        return blm.getHeader();
    }

    /**
     * get the marshalled object
     * 
     * @return the marshalled blinkenlights file
     */
    public Blm getRawObject() {
        return blm;
    }

    public BlinkenImage[] getFrames() {
        return frames;
    }

    public int getFrameCount() {
        if (frames == null) {
            return 0;
        }
        return frames.length;
    }

    public BlinkenImage getFrame(int nr) {
        if (frames == null) {
            return null;
        }
        return frames[nr % frames.length];
    }

    /**
     * return the version of the library.
     * 
     * @return String
     */
    public static String version() {
        return VERSION;
    }

}
