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
package com.neophob.sematrix.gui.guibuilder;

import com.neophob.sematrix.core.visual.effect.Options.Options;

import java.util.Map;

/**
 * this interface is used to define gui actions are needed because of 
 * keyboard input device
 *  
 * @author mvogt
 *
 */
public interface GuiCallbackAction {

    /**
     * select a visual
     * 
     * @param n
     */
    void activeVisual(int n);

    /**
     * 
     * 
     * @return
     */
    boolean isTextfieldInEditMode();
    
    /**
     * activate next tab
     */
    void selectNextTab();
    void selectPreviousTab();
    
	void updateGuiElements(Map<String, String> map);

    /**update the corresponding option display */
    void updateGuiOptions(Options opts);
}
