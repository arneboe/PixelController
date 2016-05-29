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
package com.neophob.sematrix.core.visual.layout;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.core.visual.OutputMapping;

/**
 * this class defines how multiple panels are arranged.
 * 
 * @author michu
 */
public abstract class Layout implements Serializable {

    /**
     * The Enum LayoutName.
     */
    public enum LayoutName {

        /** The HORIZONTAL. */
        HORIZONTAL(0),

        /** The BOX. */
        BOX(1),
        MATRIX(2);

        /** The id. */
        private int id;

        /**
         * Instantiates a new layout name.
         * 
         * @param id
         *            the id
         */
        LayoutName(int id) {
            this.id = id;
        }

        /**
         * Gets the id.
         * 
         * @return the id
         */
        public int getId() {
            return id;
        }
    }

    /** The log. */
    private static final transient Logger LOG = Logger.getLogger(Layout.class.getName());

    /** The layout name. */
    private LayoutName layoutName;

    /** The row1 size. */
    protected int rowCount;

    /** The row2 size. */
    protected int colCount;

    public Layout(LayoutName layoutName, final int rowCount, final int colCount) {
        this.layoutName = layoutName;
        this.rowCount = rowCount;
        this.colCount = colCount;

        LOG.log(Level.INFO, "Layout created: {0}, rows: {1}, cols: {2}", new Object[] {
                layoutName.toString(), rowCount, colCount });
    }

    /**
     * Gets the data for screen.
     * 
     * @param screenNr
     *            the screen nr
     * @return the data for screen
     */
    public abstract LayoutModel getDataForScreen(int screenNr, List<OutputMapping> ioMapping);




    /**
     * Gets the row1 size.
     * 
     * @return the row1 size
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * Gets the row2 size.
     * 
     * @return the row2 size
     */
    public int getColCount() {
        return colCount;
    }

    /**
     * Gets the layout name.
     * 
     * @return the layout name
     */
    public LayoutName getLayoutName() {
        return layoutName;
    }

}
