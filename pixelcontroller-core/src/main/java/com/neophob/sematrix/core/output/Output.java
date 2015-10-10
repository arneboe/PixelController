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
package com.neophob.sematrix.core.output;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.core.output.gamma.GammaType;
import com.neophob.sematrix.core.output.gamma.Gammatab;
import com.neophob.sematrix.core.properties.Configuration;
import com.neophob.sematrix.core.resize.IResize;
import com.neophob.sematrix.core.resize.PixelControllerResize;
import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.OutputMapping;
import com.neophob.sematrix.core.visual.Visual;
import com.neophob.sematrix.core.visual.VisualState;
import com.neophob.sematrix.core.visual.fader.IFader;
import com.neophob.sematrix.core.visual.layout.Layout;
import com.neophob.sematrix.core.visual.layout.LayoutModel;

/**
 * parent output class.
 * 
 * @author michu
 */
public abstract class Output implements IOutput {

    /** The log. */
    private static final transient Logger LOG = Logger.getLogger(Output.class.getName());

    /** The outputDeviceEnum. */
    private OutputDeviceEnum outputDeviceEnum;

    /** The matrix data. */
    protected transient MatrixData matrixData;

    /** The layout. */
    protected Layout layout;

    /** bit per pixel. */
    protected int bpp;

    /** counter used for buffer switching */
    private int totalNrOfOutputBuffers;
    private transient int switchBuffer;

    /**
     * this map contains twice as much entries as outputs exists for each output
     * device two buffers exists, one to display and one to work with
     */
    private transient Map<Integer, int[]> bufferMap;

    private GammaType gammaType;

    /** The user defined startup output gain. Output gain is the global gain value
    * that defines output brightness. Startup value is used initially to set the
    * current value which can then be overridden by the user at runtime *
    */
    private int startupOutputGain;

    /**
     * does this output device know if its connected to the matrix
     */
    protected boolean supportConnectionState = false;

    private transient PixelControllerResize resizeHelper;

    /**
     * Instantiates a new output.
     * 
     * @param outputDeviceEnum
     *            the output device enum
     * @param ph
     *            the ph
     * @param controller
     *            the controller
     * @param bpp
     *            the bpp
     */
    public Output(MatrixData matrixData, PixelControllerResize resizeHelper,
            OutputDeviceEnum outputDeviceEnum, Configuration ph, int bpp) {
        this.outputDeviceEnum = outputDeviceEnum;

        this.resizeHelper = resizeHelper;
        this.matrixData = matrixData;
        this.layout = ph.getLayout();
        this.bpp = bpp;
        this.gammaType = ph.getGammaType();
        this.startupOutputGain = ph.getStartupOutputGain();

        this.bufferMap = new HashMap<Integer, int[]>();
        this.totalNrOfOutputBuffers = ph.getNrOfScreens();
        this.switchBuffer = 0;

        if (this.gammaType == null) {
            this.gammaType = GammaType.NONE;
        }

        /* set the global output gain value to the startup value */
        VisualState.getInstance().setOutputGain(this.startupOutputGain / 100f);

        LOG.log(Level.INFO, "Output created: {0}, Layout: {1}, BPP: {2}, Gamma Correction: {3}, Output Gain: {4}",
                new Object[] { this.outputDeviceEnum, layout.getLayoutName(), this.bpp,
                        this.gammaType, this.startupOutputGain });
    }

    /**
     * Update the output device
     */
    public abstract void update();

    /**
     * Close to output device
     */
    public abstract void close();

    /**
     * get buffer for a output, this method respect the mapping, brightness
     * and global output gain (not affected by presets)
     * 
     * @param screenNr
     *            the screen nr
     * @return the buffer for screen
     */
    public int[] getBufferForScreen(int screenNr, boolean applyGamma) {
        int[] buffer = this.bufferMap.get(switchBuffer + screenNr);
        if (buffer == null || buffer.length == 0) {
            LOG.log(Level.SEVERE, "Failed to get entry for entry {0}, buffer size: "
                    + this.bufferMap.size(), (switchBuffer + screenNr));
            return null;
        }

        float brightness = VisualState.getInstance().getBrightness();
        float outputGain = VisualState.getInstance().getOutputGain();

        if (!applyGamma) {
            return Gammatab.applyBrightnessAndGammaTab(buffer, GammaType.NONE, brightness, outputGain);
        }
        return Gammatab.applyBrightnessAndGammaTab(buffer, this.gammaType, brightness, outputGain);
    }

    /**
     * return buffer for screen, applied gamma and brightness correction
     * 
     * @param screenNr
     * @return
     */
    public int[] getBufferForScreen(int screenNr) {
        return getBufferForScreen(screenNr, true);
    }

    /**
     * fade the buffer.
     * 
     * @param buffer
     *            the buffer
     * @param map
     *            the map
     * @return the int[]
     */
    private int[] doTheFaderBaby(int[] buffer, OutputMapping map) {
        IFader fader = map.getFader();
        if (fader.isStarted()) {
            return fader.getBuffer(buffer, VisualState.getInstance()
                    .getVisual(fader.getNewVisual()).getBuffer());
            // do not cleanup fader here, the box layout gets messed up!
            // the fader is cleaned up in the update system method
        }
        return buffer;
    }

    /**
     * input: 64*64*nrOfScreens buffer output: 8*8 buffer (resized from 64*64)
     * 
     * ImageUtils.java, Copyright (c) JForum Team
     * 
     * @param visual
     *            the visual
     * @param map
     *            the map
     * @return the screen buffer for device
     */
    private int[] getScreenBufferForDevice(Visual visual, OutputMapping map) {
        int[] buffer = visual.getBuffer();
        // apply output specific effect
        // buffer = map.getBuffer();
        // buffer = map.getFader().getBuffer(buffer);

        // apply the fader (if needed)
        buffer = doTheFaderBaby(buffer, map);

        // resize to the ouput buffer return image
        return resizeBufferForDevice(buffer, visual.getResizeOption(), matrixData.getDeviceXSize(),
                matrixData.getDeviceYSize());
    }

    /**
     * resize internal buffer to output size.
     * 
     * @param buffer
     *            the buffer
     * @param resizeName
     *            the resize name
     * @param deviceXSize
     *            the device x size
     * @param deviceYSize
     *            the device y size
     * @return RESIZED image
     */
    public int[] resizeBufferForDevice(int[] buffer, ResizeName resizeName, int deviceXSize,
            int deviceYSize) {

        IResize r = resizeHelper.getResize(resizeName);
        return r.resizeImage(buffer, matrixData.getBufferXSize(), matrixData.getBufferYSize(),
                deviceXSize, deviceYSize);
    }

    /**
     * strech the image for multiple outputs.
     * 
     * @param visual
     *            the visual
     * @param lm
     *            the lm
     * @param map
     *            the map
     * @param output
     *            the output
     * @return the screen buffer for device
     */
    private int[] getScreenBufferForDevice(Visual visual, LayoutModel lm, OutputMapping map) {
        // apply the fader (if needed)
        int[] buffer = doTheFaderBaby(visual.getBuffer(), map);

        int bufferWidth = matrixData.getBufferXSize();
        int bufferHeight = matrixData.getBufferYSize();

        int xStart = lm.getxStart(bufferWidth);
        int xWidth = lm.getxWidth(bufferWidth);
        int yStart = lm.getyStart(bufferHeight);
        int yWidth = lm.getyWidth(bufferHeight);

        int[] resizedBuffer = new int[bufferWidth * bufferHeight];

        // resize image (strech), example source image is 64x64 which gets
        // resized to
        // panel 1: X:0, 32 Y:0, 64
        // panel 2: X:32, 32 Y:0, 64
        float deltaX = xWidth / (float) bufferWidth;
        float deltaY = yWidth / (float) bufferHeight;

        int dst = 0;
        int src;
        float srcYofs = yStart;
        for (int y = 0; y < bufferHeight; y++) {
            float srcXofs = xStart + (int) (srcYofs * bufferWidth);
            for (int x = 0; x < bufferWidth; x++) {
                src = (int) (srcXofs);
                resizedBuffer[dst++] = buffer[src % resizedBuffer.length];
                srcXofs += deltaX;
            }
            srcYofs += deltaY;
        }

        // make sure that we use the PIXEL resize or the output is VERY blurred!
        // speak, do not use visual.getResizeOption(), or the output is
        // SOMETIMES very ugly!
        return resizeBufferForDevice(resizedBuffer, ResizeName.PIXEL_RESIZE,
                matrixData.getDeviceXSize(), matrixData.getDeviceYSize());
    }

    /**
     * fill the the preparedBufferMap instance with int[] buffers for all
     * screens.
     */
    public synchronized void prepareOutputBuffer(VisualState vs) {
        int[] buffer;
        Visual v;
        List<OutputMapping> allOutputMappings = vs.getAllOutputMappings();
        for (int screen = 0; screen < this.totalNrOfOutputBuffers; screen++) {
            LayoutModel lm = this.layout.getDataForScreen(screen, allOutputMappings);
            OutputMapping map = allOutputMappings.get(screen);

            v = vs.getVisual(lm.getVisualId());
            if (lm.screenDoesNotNeedStretching()) {
                buffer = this.getScreenBufferForDevice(v, map);
            } else {
                buffer = this.getScreenBufferForDevice(v, lm, map);
            }

            // the prepare method has to write to the currently not used range
            // of the bufferMap
            int pos = screen;
            if (this.switchBuffer != this.totalNrOfOutputBuffers) {
                pos += this.totalNrOfOutputBuffers;
            }
            this.bufferMap.put(pos, buffer);
        }
    }

    /**
     * switch currentBufferMap <-> preparedBufferMap instances
     */
    public void switchBuffers() {
        if (switchBuffer == 0) {
            switchBuffer = totalNrOfOutputBuffers;
        } else {
            switchBuffer = 0;
        }
    }

    /**
     * return the connection state if supported
     * 
     * @return
     */
    public boolean isConnected() {
        // overwrite me if supported!
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.outputDeviceEnum.toString();
    }

    /**
     * if device supports an error counter, overwrite me.
     * 
     * @return nr of failed frames
     */
    public long getErrorCounter() {
        // overwriteme
        return 0;
    }

    /**
     * if device supports a connection status, overwrite me. examples: connected
     * to /dev/aaa or IP Adress: 1.2.3.4
     * 
     * @return
     */
    public String getConnectionStatus() {
        return "";
    }

    /**
     * Gets the bpp.
     * 
     * @return bpp (bit per pixel)
     */
    public int getBpp() {
        return bpp;
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public OutputDeviceEnum getType() {
        return this.outputDeviceEnum;
    }

    /**
     * @return the supportConnectionState
     */
    public boolean isSupportConnectionState() {
        return supportConnectionState;
    }

    /**
     * @return the gammaType
     */
    public GammaType getGammaType() {
        return gammaType;
    }

}
