/**
 * Copyright (C) 2011-2013 Michael Vogt <michu@neophob.com>
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
package com.neophob.sematrix.core.generator;

import java.util.Random;

import com.neophob.sematrix.core.glue.Collector;
import com.neophob.sematrix.core.glue.MatrixData;
import com.neophob.sematrix.core.glue.ShufflerOffset;
import com.neophob.sematrix.core.resize.Resize.ResizeName;

/**
 * The Class ColorScroll.
 *
 * @author McGyver
 */
public class ColorScroll extends Generator {

    /** The scroll mode. */
    private ScrollMode scrollMode;
    
    /** The frame count. */
    private int frameCount;
    
    /** The internal buffer x size2. */
    private int internalBufferXSize2;
    
    /** The internal buffer y size2. */
    private int internalBufferYSize2;

    
    /**
     * The Enum ScrollMode.
     */
    public enum ScrollMode{
	    LEFT_TO_RIGHT(0),
	    RIGHT_TO_LEFT(1),
	    TOP_TO_BOTTOM(2),
	    BOTTOM_TO_TOP(3),
	    RIGHTBOTTOM_TO_LEFTTOP(4),
	    LEFTBOTTOM_TO_RIGHTTOP(5),
	    RIGHTTOP_TO_LEFTBOTTOM(6),
	    LEFTTOP_TO_RIGHTBOTTOM(7),
	    MIDDLE_TO_SIDES_VERT(8),
	    SIDES_TO_MIDDLE_VERT(9),
	    MIDDLE_TO_SIDES_HORIZ(10),
	    SIDES_TO_MIDDLE_HORIZ(11),
	    EXPLODE_CIRCLE(12),
	    IMPLODE_CIRCLE(13),
	    EXPLODE_DIAMOND(14),
	    IMPLODE_DIAMOND(15);
    	
    	/** The mode. */
	    private int mode;
    	
    	/**
	     * Instantiates a new scroll mode.
	     *
	     * @param mode the mode
	     */
	    private ScrollMode(int mode) {
    		this.mode = mode;
    	}
    	
    	/**
	     * Gets the mode.
	     *
	     * @return the mode
	     */
	    public int getMode() {
    		return mode;
    	}
    	 
	    public String getDisplayName() {
	    	return this.name().replace("_", " ");
	    }
	    
    	/**
	     * Gets the scroll mode.
	     *
	     * @param nr the nr
	     * @return the scroll mode
	     */
	    public static ScrollMode getScrollMode(int nr) {
    		for (ScrollMode s: ScrollMode.values()) {
    			if (s.getMode() == nr) {
    				return s;
    			}
    		}    		
    		return null;
    	}
	    
    }
    
    /**
     * Instantiates a new colorscroll.
     *
     * @param controller the controller
     * @param colorList the color list
     */
    public ColorScroll(MatrixData matrix) {
        super(matrix, GeneratorName.COLOR_SCROLL, ResizeName.QUALITY_RESIZE);

        scrollMode = ScrollMode.EXPLODE_CIRCLE;

        internalBufferXSize2 = internalBufferXSize/2;
        internalBufferYSize2 = internalBufferYSize/2;
    }

    
    /* (non-Javadoc)
     * @see com.neophob.sematrix.core.generator.Generator#update()
     */
    @Override
    public void update() {
        
        //do not remove, sanity check
        if (scrollMode==null) {
            scrollMode = ScrollMode.EXPLODE_CIRCLE;            
        }
        
        // scroll colors on x axis
        switch (scrollMode) {
            case LEFT_TO_RIGHT:
                leftToRight();
                break;
            case RIGHT_TO_LEFT:
                rightToLeft();
                break;
            case TOP_TO_BOTTOM:
                topToBottom();
                break;
            case BOTTOM_TO_TOP:
                bottomToTop();
                break;
            case RIGHTBOTTOM_TO_LEFTTOP:
                rightBottomToLeftTop();
                break;
            case LEFTBOTTOM_TO_RIGHTTOP:
                leftBottomToRightTop();
                break;
            case RIGHTTOP_TO_LEFTBOTTOM:
                rightTopToLeftBottom();
                break;
            case LEFTTOP_TO_RIGHTBOTTOM:
                leftTopToRightBottom();
                break;
            case MIDDLE_TO_SIDES_VERT:
                middleToSidesVertical();
                break;
            case SIDES_TO_MIDDLE_VERT:
                sidesToMiddleVertical();
                break;
            case MIDDLE_TO_SIDES_HORIZ:
                middleToSidesHorizontal();
                break;
            case SIDES_TO_MIDDLE_HORIZ:
                sidesToMiddleHorizontal();
                break;
            case EXPLODE_CIRCLE:
                explodeCircle();
                break;
            case IMPLODE_CIRCLE:
                implodeCircle();
                break;
            case EXPLODE_DIAMOND:
            	explodeDiamond();
            	break;
            case IMPLODE_DIAMOND:
            	imploadDiamond();
            	break;
        }
        
        frameCount++;
    }

    /**
     * Sets the scroll mode.
     *
     * @param scrollMode the new scroll mode
     */
    void setScrollMode(int scrollMode) {
        ScrollMode sm = ScrollMode.getScrollMode(scrollMode);          
        //sanity check
        if (sm!=null) {
            this.scrollMode = sm;
        }                 
    }

    
	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.generator.Generator#shuffle()
	 */
	@Override
	public void shuffle() {
		if (Collector.getInstance().getShufflerSelect(ShufflerOffset.COLOR_SCROLL)) {
			Random rand = new Random();
			int nr = rand.nextInt(ScrollMode.values().length);
			this.setScrollMode(nr);
		}
	}
    
    /**
     * Gets the color.
     *
     * @param val the val
     * @return the color
     */
    private int getColor(int val) {
    	return (val+frameCount)&0xff;
    }
    

    /**
     * Left to right.
     */
    private void leftToRight() {
        for (int x = 0; x < internalBufferXSize; x++) {	
        	int col = getColor(x);
            for (int y = 0; y < internalBufferYSize; y++) {
                this.internalBuffer[y * internalBufferXSize + x] = col;
            }
        }
    }

    /**
     * Right to left.
     */
    private void rightToLeft() {
        for (int x = 0; x < internalBufferXSize; x++) {
            int xRev = internalBufferXSize - x - 1;
            
            int col = getColor(x);
            for (int y = 0; y < internalBufferYSize; y++) {
                this.internalBuffer[y * internalBufferXSize + xRev] = col;
            }
        }
    }

    /**
     * Top to bottom.
     */
    private void topToBottom() {
        for (int y = 0; y < internalBufferYSize; y++) {
            int yRev = internalBufferYSize - y - 1;
            int col = getColor(y);
            for (int x = 0; x < internalBufferXSize; x++) {
                this.internalBuffer[yRev * internalBufferXSize + x] = col;
            }
        }
    }

    /**
     * Bottom to top.
     */
    private void bottomToTop() {
        for (int y = 0; y < internalBufferYSize; y++) {
            int col = getColor(y);
            for (int x = 0; x < internalBufferXSize; x++) {
                this.internalBuffer[y * internalBufferXSize + x] = col;
            }
        }
    }

    /**
     * Right bottom to left top.
     */
    private void rightBottomToLeftTop() {
        int bigSide = Math.max(internalBufferXSize, internalBufferYSize);
        for (int diagStep = 0; diagStep < 2 * bigSide; diagStep++) {

            int col = getColor(diagStep);

            int diagPixelCount = diagStep;
            int diagOffset = 0;
            if (diagStep >= bigSide) {
                diagPixelCount = (2 * bigSide) - diagStep;
                diagOffset = diagStep - bigSide;
            }

            for (int diagCounter = 0; diagCounter < diagPixelCount; diagCounter++) {
                int x = diagOffset + diagCounter;
                int y = diagPixelCount - diagCounter - 1 + diagOffset;
                setPixel(x, y, col);
            }
        }
    }

    /**
     * Left bottom to right top.
     */
    private void leftBottomToRightTop() {
        int bigSide = Math.max(internalBufferXSize, internalBufferYSize);
        for (int diagStep = 0; diagStep < 2 * bigSide; diagStep++) {
        	int col = getColor(diagStep);

            int diagPixelCount = diagStep;
            int diagOffset = 0;
            if (diagStep >= bigSide) {
                diagPixelCount = (2 * bigSide) - diagStep;
                diagOffset = diagStep - bigSide;
            }

            for (int diagCounter = 0; diagCounter < diagPixelCount; diagCounter++) {
                int x = internalBufferXSize - 1 - (diagOffset + diagCounter);
                int y = diagPixelCount - diagCounter - 1 + diagOffset;
                setPixel(x, y, col);
            }
        }
    }

    /**
     * Right top to left bottom.
     */
    private void rightTopToLeftBottom() {
        int bigSide = Math.max(internalBufferXSize, internalBufferYSize);
        for (int diagStep = 0; diagStep < 2 * bigSide; diagStep++) {
        	int col = getColor(diagStep);

            int diagPixelCount = diagStep;
            int diagOffset = 0;
            if (diagStep >= bigSide) {
                diagPixelCount = (2 * bigSide) - diagStep;
                diagOffset = diagStep - bigSide;
            }

            for (int diagCounter = 0; diagCounter < diagPixelCount; diagCounter++) {
                int x = diagOffset + diagCounter;
                int y = internalBufferYSize - 1 - (diagPixelCount - diagCounter - 1 + diagOffset);
                setPixel(x, y, col);
            }
        }
    }

    /**
     * Left top to right bottom.
     */
    private void leftTopToRightBottom() {
        int bigSide = Math.max(internalBufferXSize, internalBufferYSize);
        for (int diagStep = 0; diagStep < 2 * bigSide; diagStep++) {
        	int col = getColor(diagStep);

        	int diagPixelCount = diagStep;
            int diagOffset = 0;
            if (diagStep >= bigSide) {
                diagPixelCount = (2 * bigSide) - diagStep;
                diagOffset = diagStep - bigSide;
            }

            for (int diagCounter = 0; diagCounter < diagPixelCount; diagCounter++) {
                int x = internalBufferXSize - 1 - (diagOffset + diagCounter);
                int y = internalBufferYSize - 1 - (diagPixelCount - diagCounter - 1 + diagOffset);
                setPixel(x, y, col);
            }
        }
    }

    /**
     * Middle to sides vertical.
     */
    private void middleToSidesVertical() {
        int ySize = internalBufferYSize;

        for (int x = 0; x < internalBufferXSize2; x++) {
        	int col = getColor(x);

            for (int y = 0; y < ySize; y++) {
                this.internalBuffer[y * internalBufferXSize + x] = col;
                this.internalBuffer[y * internalBufferXSize + internalBufferXSize - x - 1] = col;
            }
        }
    }

    /**
     * Sides to middle vertical.
     */
    private void sidesToMiddleVertical() {
        int ySize = internalBufferYSize;

        for (int x = 0; x < internalBufferXSize2; x++) {

            int xRev = (internalBufferXSize2) - x - 1;
            int col = getColor(x);
            for (int y = 0; y < ySize; y++) {
                this.internalBuffer[y * internalBufferXSize + xRev] = col;
                this.internalBuffer[y * internalBufferXSize + internalBufferXSize - xRev - 1] = col;
            }
        }
    }

    /**
     * Middle to sides horizontal.
     */
    private void middleToSidesHorizontal() {
        int xSize = internalBufferXSize;

        for (int y = 0; y < internalBufferYSize2; y++) {

        	int col = getColor(y);

            for (int x = 0; x < xSize; x++) {
                this.internalBuffer[y * internalBufferXSize + x] = col;
                this.internalBuffer[(internalBufferYSize - y - 1) * internalBufferXSize + x] = col;
            }
        }
    }

    /**
     * Sides to middle horizontal.
     */
    private void sidesToMiddleHorizontal() {
        int xSize = internalBufferXSize;

        for (int y = 0; y < internalBufferYSize2; y++) {

            int yRev = internalBufferYSize2 - y - 1;
            int col = getColor(y);

            for (int x = 0; x < xSize; x++) {
                this.internalBuffer[yRev * internalBufferXSize + x] = col;
                this.internalBuffer[(internalBufferYSize - yRev - 1) * internalBufferXSize + x] = col;
            }
        }
    }

    /**
     * Implode circle.
     */
    private void implodeCircle() {

        for (int i = 0; i < internalBufferXSize; i++) {
            for (int j = 0; j < internalBufferYSize; j++) {
                //calculate distance to center:
                double x = (internalBufferXSize2) - i;
                double y = (internalBufferYSize2) - j;
                double r = Math.sqrt((x * x) + (y * y));
                int col = getColor((int)r);
                setPixel(i, j, col);
            }
        }
    }
    
    /**
     * Explode circle.
     */
    private void explodeCircle() {

        double maxX = (internalBufferXSize2) - 0;
        double maxY = (internalBufferYSize2) - 0;
        double maxR = Math.sqrt((maxX * maxX) + (maxY * maxY));

        for (int i = 0; i < internalBufferXSize; i++) {
            for (int j = 0; j < internalBufferYSize; j++) {
                //calculate distance to center:
                double x = (internalBufferXSize2) - i;
                double y = (internalBufferYSize2) - j;
                double r = Math.sqrt((x * x) + (y * y));
                r = maxR - r;
                int col = getColor((int)r);
                setPixel(i, j, col);
            }
        }

    }
    
    
    

    /**
     * Explode diamond.
     */
    private void imploadDiamond() {

        for (int i = 0; i < internalBufferXSize; i++) {
            for (int j = 0; j < internalBufferYSize; j++) {
                //calculate distance to center:
                double x = (internalBufferXSize2) - i;
                double y = (internalBufferYSize2) - j;
                double r = Math.abs(x) + Math.abs(y);
                int col = getColor((int)r);
                setPixel(i, j, col);
            }
        }

    }

    /**
     * Explode diamond.
     */
    private void explodeDiamond() {
        double maxX = (internalBufferXSize2) - 0;
        double maxY = (internalBufferYSize2) - 0;
        double maxR = Math.sqrt((maxX * maxX) + (maxY * maxY));

        for (int i = 0; i < internalBufferXSize; i++) {
            for (int j = 0; j < internalBufferYSize; j++) {
                //calculate distance to center:
                double x = (internalBufferXSize2) - i;
                double y = (internalBufferYSize2) - j;
                double r = Math.abs(x) + Math.abs(y);
                r = maxR - r;
                int col = getColor((int)r);
                setPixel(i, j, col);
            }
        }

    }
    
    /**
     * Sets the pixel.
     *
     * @param x the x
     * @param y the y
     * @param col the col
     */
    private void setPixel(int x, int y, int col) {
        if (y >= 0 && y < internalBufferYSize && x >= 0 && x < internalBufferXSize) {
            this.internalBuffer[y * internalBufferXSize + x] = col;
        }
    }


    /**
     * @return the scrollMode
     */
    public ScrollMode getScrollMode() {
        if (scrollMode==null) {
            return ScrollMode.EXPLODE_CIRCLE;
        }
        return scrollMode;
    }


    /**
     * @param scrollMode the scrollMode to set
     */
    public void setScrollMode(ScrollMode scrollMode) {
        this.scrollMode = scrollMode;
    }
    
    
    

}
