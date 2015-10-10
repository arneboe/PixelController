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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.core.properties.Configuration;
import com.neophob.sematrix.core.properties.ColorFormat;
import com.neophob.sematrix.core.properties.DeviceConfig;
import com.neophob.sematrix.core.resize.PixelControllerResize;
import com.neophob.sematrix.core.visual.MatrixData;

/**
 * The Class ResolutionAwareOutput.
 * 
 * 
 * @author michu
 */
public abstract class OnePanelResolutionAwareOutput extends Output {

    /** The log. */
    private static final transient Logger LOG = Logger
            .getLogger(OnePanelResolutionAwareOutput.class.getName());

    /** does the image needs to be rotated? */
    protected DeviceConfig displayOption;

    /** The output color format. */
    protected ColorFormat colorFormat;

    /** flip each 2nd scanline? */
    protected boolean snakeCabeling;

    /** Manual mapping */
    protected int[] mapping;

    /** The initialized. */
    protected boolean initialized;

    /**
     * Instantiates a new ResolutionAwareOutput.
     * 
     * @param outputDeviceEnum
     *            the outputDeviceEnum
     * @param ph
     *            the ph
     * @param controller
     *            the controller
     */
    public OnePanelResolutionAwareOutput(MatrixData matrixData, PixelControllerResize resizeHelper,
            OutputDeviceEnum outputDeviceEnum, Configuration ph, int bpp) {
        super(matrixData, resizeHelper, outputDeviceEnum, ph, bpp);

        this.snakeCabeling = ph.isOutputSnakeCabeling();
        this.mapping = ph.getOutputMappingValues();

        // get the mini dmx layout
        this.displayOption = ph.getOutputDeviceLayout();
        if (this.displayOption == null) {
            this.displayOption = DeviceConfig.NO_ROTATE;
        }

        this.colorFormat = ColorFormat.RBG;
        if (ph.getColorFormat().size() > 0) {
            this.colorFormat = ph.getColorFormat().get(0);
        }

        LOG.log(Level.INFO, "Output Settings:");
        LOG.log(Level.INFO,
                "\tResolution: " + matrixData.getDeviceXSize() + "/" + matrixData.getDeviceYSize());
        LOG.log(Level.INFO, "\tSnakeCabeling: " + snakeCabeling);
        LOG.log(Level.INFO, "\tRotate: " + displayOption);
        LOG.log(Level.INFO, "\tColorFormat: " + colorFormat);
        LOG.log(Level.INFO, "\tOutput Mapping entry size: " + this.mapping.length);
    }

    /**
     * transform the buffer (rotate, flip 2nd scanline)
     * 
     * @return the rotated buffer
     */
    public int[] getTransformedBuffer() {
        // rotate buffer (if needed)
        int[] transformedBuffer = RotateBuffer.transformImage(super.getBufferForScreen(0),
                displayOption, matrixData.getDeviceXSize(), matrixData.getDeviceYSize());

        if (this.snakeCabeling) {
            // flip each 2nd scanline
            transformedBuffer = OutputHelper.flipSecondScanline(transformedBuffer,
                    matrixData.getDeviceXSize(), matrixData.getDeviceYSize());
        } else if (this.mapping.length > 0) {
            // do manual mapping
            transformedBuffer = OutputHelper.manualMapping(transformedBuffer, mapping,
                    matrixData.getDeviceXSize(), matrixData.getDeviceYSize());
        }

        return transformedBuffer;
    }

}
