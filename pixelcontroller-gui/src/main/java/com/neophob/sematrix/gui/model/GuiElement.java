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
package com.neophob.sematrix.gui.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public enum GuiElement {

    CURRENT_VISUAL, CURRENT_OUTPUT,

    GENERATOR_ONE_DROPDOWN, GENERATOR_TWO_DROPDOWN, EFFECT_ONE_DROPDOWN, EFFECT_TWO_DROPDOWN, MIXER_DROPDOWN,

    BUTTON_TOGGLE_FREEZE, BUTTON_TOGGLE_INTERNAL_VISUALS, BUTTON_RANDOM_CONFIGURATION, BUTTON_RANDOM_PRESET, BUTTONS_RANDOM_MODE,
    C1_2,

    BRIGHTNESS,

    GENERATOR_SPEED, BEAT_WORKMODE,

    RANDOM_ELEMENT,

    TEXTUREDEFORM_OPTIONS, ZOOM_OPTIONS,

    // single output settings
    OUTPUT_SELECTED_VISUAL_DROPDOWN, OUTPUT_FADER_DROPDOWN,

    // all output settings
    OUTPUT_ALL_SELECTED_VISUAL_DROPDOWN, OUTPUT_ALL_FADER_DROPDOWN,

    COLOR_SET_DROPDOWN,

    // preset tab
    PRESET_BUTTONS, LOAD_PRESET, SAVE_PRESET, DELETE_PRESET,

    SAVE_SCREENSHOT, SOUND_MODE_DROPDOWN,

    ADD_COLORSET, C1_3, C1_4, C1_5, STROBO;

    /**
     * 
     * @return
     */
    public String guiText() {
        return WordUtils.capitalizeFully(StringUtils.replace(this.name(), "_", " "));
    }

    /**
     * 
     * @param s
     * @return
     */
    public static GuiElement getGuiElement(String s) {
        for (GuiElement gw : GuiElement.values()) {
            if (gw.toString().equalsIgnoreCase(s) || gw.guiText().equalsIgnoreCase(s)) {
                return gw;
            }
        }
        return null;
    }
}
