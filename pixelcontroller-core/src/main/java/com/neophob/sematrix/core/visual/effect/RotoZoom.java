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

import java.util.Random;

import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.effect.Options.FloatValueOption;
import com.neophob.sematrix.core.visual.effect.Options.SelectionListOption;
import com.neophob.sematrix.core.visual.fader.CrossfaderHelper;

/**
 * The Class RotoZoom.
 * 
 * @author michu
 * 
 *         ripped from http://www.openprocessing.org/visuals/?visualID=8030
 */
public class RotoZoom extends RotoZoomEffect {

    // endless zooming or zoomin-zoomout...
    public enum WORKMODE {
        PINGPONG,
        ZOOM
    }

    private float angle;
    private int angleOrig;
    private float angleDiff;
    private float scale, scale2;
    private float scaleOrig;
    private float faderPos;
    private float dscalee = 0.01f;
    private WORKMODE workmode = WORKMODE.ZOOM;
    private FloatValueOption angleOption = new FloatValueOption("Speed", 0, 359, 2.5f);
    private SelectionListOption workmodeOption = new SelectionListOption("Mode");
    /**
     * Instantiates a new roto zoom.
     *
     * @param scale
     *            the scale
     * @param angle
     *            the angle
     */
    public RotoZoom(MatrixData matrix, float scale, float angle) {
        super(matrix, EffectName.ROTOZOOM, ResizeName.QUALITY_RESIZE);
        this.scale = scale;
        this.scaleOrig = scale;
        this.angle = angle;
        this.faderPos = 0.0f;
        this.angleDiff = 0.02f;
        options.add(angleOption);

        for(WORKMODE mode : WORKMODE.values()) {
            workmodeOption.addEntry(mode.toString());
        }
        workmodeOption.select(0);
        options.add(workmodeOption);
    }

    /**
     * Gets the angle.
     * 
     * @return the angle
     */
    public int getAngle() {
        return angleOrig;
    }

    /**
     * Sets the angle.
     * 
     * @param angle
     *            from -127 to 127
     * @return the int
     */
    private void setAngle(int angle) {
        if (angle > 127) {
            angle = 127;
        }
        if (angle < -127) {
            angle = -127;
        }

        this.angleOrig = angle;

        // 137 sound funny - but correct
        // using 137 - the max value is 10 used for the diff!
        if (angle > 0) {
            angle = 137 - angle;
        } else {
            angle = -137 - angle;
        }

        if (angle != 0) {
            float f = (1.0f / (float) angle) * 2;
            this.angleDiff = f;
        } else {
            this.angleDiff = 0.0f;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.neophob.sematrix.core.effect.Effect#getBuffer(int[])
     */
    public int[] getBuffer(int[] buffer) {
        int[] rotoZoomedBuffer = rotoZoom(scale, angle, buffer);

        // the crossfade is used for the endless zoom option
        if (workmode == WORKMODE.ZOOM && faderPos > 0.0f) {
            return CrossfaderHelper.getBuffer8Bit(faderPos, rotoZoomedBuffer,
                    rotoZoom(scale2, angle, buffer));
        }
        return rotoZoomedBuffer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.neophob.sematrix.core.effect.Effect#update()
     */
    @Override
    public void update() {
        if(angleOrig != angleOption.getValue()) {
            setAngle((int)angleOption.getValue());
        }
        workmode = WORKMODE.values()[(int) workmodeOption.getValue()];

        angle += this.angleDiff;
        scale -= dscalee;

        if (workmode == WORKMODE.ZOOM) {
            if (this.scale < 0.4f) {
                faderPos += 0.04f;
                scale2 -= dscalee;

                if (faderPos > 0.98f) {
                    // finished fading - reset values
                    this.faderPos = 0.0f;
                    this.scale = this.scale2;
                    this.scale2 = this.scaleOrig;
                }
            }
        } else {
            // WORKMODE.PINGPONG
            if (scale < 0.13f || scale > 1.6f) {
                dscalee *= -1;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.neophob.sematrix.core.effect.Effect#shuffle()
     */
    @Override
    public void shuffle() {
       int tmpAngle = (new Random().nextInt(255)) - 128;
       this.setAngle(tmpAngle);
    }

}
