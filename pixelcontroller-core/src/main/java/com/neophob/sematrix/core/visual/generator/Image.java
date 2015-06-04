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
package com.neophob.sematrix.core.visual.generator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.neophob.sematrix.core.visual.effect.Options.SelectionListOption;
import org.apache.commons.lang3.StringUtils;

import com.neophob.sematrix.core.glue.FileUtils;
import com.neophob.sematrix.core.glue.ShufflerOffset;
import com.neophob.sematrix.core.resize.IResize;
import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.VisualState;

/**
 * display an image.
 * 
 * @author mvogt
 */
public class Image extends Generator {

    // list to store movie files used by shuffler
    private List<String> imageFiles;

    /** The Constant RESIZE_TYP. */
    private static final ResizeName RESIZE_TYP = ResizeName.QUALITY_RESIZE;

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(Image.class.getName());

    private SelectionListOption files = new SelectionListOption("File");

    /** The currently loaded file */
    private String filename;
    private int loadedPosition;

    private FileUtils fileUtils;

    private IResize resize;

    public Image(MatrixData matrix, FileUtils fu, IResize resize) {
        super(matrix, GeneratorName.IMAGE, RESIZE_TYP);
        this.fileUtils = fu;
        this.resize = resize;

        // find image files
        imageFiles = new ArrayList<String>();

        try {
            for (String s : fu.findImagesFiles()) {
                imageFiles.add(s);
            }
        } catch (NullPointerException e) {
            LOG.log(Level.SEVERE,
                    "Failed to search image files, make sure directory '" + fu.getImageDir()
                            + "' exist!");
            throw new IllegalArgumentException(
                    "Failed to search image files, make sure directory '" + fu.getImageDir()
                            + "' exist!");
        }

        for(String file : imageFiles) {
            files.addEntry(file);
        }
        this.loadFile(imageFiles.get(0));
        files.select(0);
        options.add(files);
        LOG.log(Level.INFO, "Image, found " + imageFiles.size() + " image files");
    }

    public void loadNextFile() {
        loadedPosition = (loadedPosition + 1) % imageFiles.size();
        this.loadFile(imageFiles.get(loadedPosition));
    }

    /**
     * load a new file.
     * 
     * @param filename
     *            the filename
     */
    public synchronized void loadFile(String filename) {
        if (StringUtils.isBlank(filename)) {
            LOG.log(Level.INFO, "Empty filename provided, call ignored!");
            return;
        }

        // only load if needed
        if (StringUtils.equals(filename, this.filename)) {
            LOG.log(Level.INFO, "new filename does not differ from old: " + filename);
            return;
        }

        try {
            int pos = imageFiles.indexOf(filename);
            if (pos == -1) {
                LOG.log(Level.INFO, "Filename {0} not found in list", filename);
            } else {
                loadedPosition = pos;
            }
            long t1 = System.currentTimeMillis();
            String fileToLoad = fileUtils.getImageDir() + File.separator + filename;

            LOG.log(Level.INFO, "load image " + fileToLoad);
            BufferedImage img = ImageIO.read(new File(fileToLoad));
            if (img == null || img.getHeight() < 2) {
                LOG.log(Level.WARNING, "Invalid image, image height is < 2!");
                return;
            }
            long t2 = System.currentTimeMillis();

            // convert to RGB colorspace
            int[] dataBuffInt = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0,
                    img.getWidth());
            long t3 = System.currentTimeMillis();

            LOG.log(Level.INFO, "resize to img " + filename + " " + internalBufferXSize + ", "
                    + internalBufferYSize + " using " + resize.getName());
            this.internalBuffer = resize.resizeImage(dataBuffInt, img.getWidth(), img.getHeight(),
                    internalBufferXSize, internalBufferYSize);
            this.filename = filename;

            short r, g, b;
            int rgbColor;

            // greyscale it
            for (int i = 0; i < internalBuffer.length; i++) {
                rgbColor = internalBuffer[i];
                r = (short) ((rgbColor >> 16) & 255);
                g = (short) ((rgbColor >> 8) & 255);
                b = (short) (rgbColor & 255);
                int val = (int) (r * 0.3f + g * 0.59f + b * 0.11f);
                internalBuffer[i] = val;
            }
            LOG.log(Level.INFO,
                    "Image {0} loaded in {1} ms. Load image: {2}ms, get data from image: {3}ms",
                    new Object[] { filename, System.currentTimeMillis() - t1, t2 - t1, t3 - t2 });
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to load image " + filename, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.neophob.sematrix.core.generator.Generator#update()
     */
    @Override
    public void update(int amount) {
        if(!files.getSelected().equals(filename)) {
            loadFile(files.getSelected());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.neophob.sematrix.core.generator.Generator#shuffle()
     */
    @Override
    public void shuffle() {
        int nr = new Random().nextInt(imageFiles.size());
        loadFile(imageFiles.get(nr));
        files.select(nr);
    }

    /**
     * Gets the filename.
     * 
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

}
