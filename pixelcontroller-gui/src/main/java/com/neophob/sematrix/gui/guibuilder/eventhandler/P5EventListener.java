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
package com.neophob.sematrix.gui.guibuilder.eventhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.core.properties.ValidCommand;
import com.neophob.sematrix.core.visual.effect.Options.IOption;
import com.neophob.sematrix.core.visual.effect.Options.Options;
import com.neophob.sematrix.gui.guibuilder.GeneratorGui;
import com.neophob.sematrix.gui.model.GuiElement;
import com.neophob.sematrix.gui.service.PixConServer;

import controlP5.ControlEvent;
import controlP5.ControlListener;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * GUI Listener
 * 
 * this class translate the gui stuff into a string array and send the result to
 * the PixelController MessageProcessor
 * 
 * @author michu
 * 
 */
public final class P5EventListener implements ControlListener {

    /** The log. */
    private static final Logger LOG = Logger.getLogger(P5EventListener.class.getName());

    private static final int CALLBACK_TIMEOUT = 100;

    private boolean internalVisualVisible = true;

    private long lastCallbackEvent;

    private PixConServer pixConSrv;

    private GeneratorGui callback;

    /**
     * 
     * @param callback
     */
    public P5EventListener(PixConServer pixConSrv, GeneratorGui callback) {
        this.callback = callback;
        this.pixConSrv = pixConSrv;
    }

    /**
     * 
     */
    public void controlEvent(ControlEvent theEvent) {
        // DropdownList is of type ControlGroup.
        // A controlEvent will be triggered from inside the ControlGroup class.
        // therefore you need to check the originator of the Event with
        // if (theEvent.isGroup())
        // to avoid an error message thrown by controlP5.
        float value = -1f;
        int intVal;
        String name;

        if (theEvent.isGroup()) {
            // check if the Event was triggered from a ControlGroup
            // LOG.log(Level.INFO,
            // theEvent.getGroup().getValue()+" from "+theEvent.getGroup());
            value = theEvent.getGroup().getValue();
        } else if (theEvent.isController()) {
            // LOG.log(Level.INFO,
            // theEvent.getController().getValue()+" from "+theEvent.getController());
            value = theEvent.getController().getValue();
        } else if (theEvent.isTab()) {
            // events from tabs are ignored
            return;
        }
        intVal = (int) value;

        GuiElement selection = null;

        try {
            selection = GuiElement.getGuiElement(theEvent.getName());
        } catch (Exception e) {
            LOG.log(Level.INFO, "Failed to parse <" + theEvent.getName() + ">", e);
            return;
        }

        if (selection == null &&
            theEvent.getName().startsWith("OPTION")) {//FIXME "OPTION" should be a constant somewhere
            //dynamic options are not transferred using the messages, instead they are set directly.
            //Not sure if this is a good idea but its faster to implement and I don't have a lot of time right now
            IOption opt = null;
            //FIXME this sucks haaard
            if(theEvent.getName().contains("EFFECT_A")) {
                opt = callback.getActiveOption(theEvent.getName(), Options.Target.EFFECT_A);
            }
            else if(theEvent.getName().contains("EFFECT_B")) {
                opt = callback.getActiveOption(theEvent.getName(), Options.Target.EFFECT_B);
            }
            else
            {
                throw new NotImplementedException();
            }
            if(null != opt) {
                opt.setValue(value);
            }
            return;
        }
        else if(selection == null)  {
            LOG.log(Level.INFO, "Null selection <" + theEvent.getName() + ">, details: " + theEvent);
            return;
        }

        switch (selection) {
            case EFFECT_ONE_DROPDOWN:
            case EFFECT_TWO_DROPDOWN:
                LOG.log(Level.INFO, "EFFECT Value: " + value);
                handleEffect(value, selection);
                break;

            case GENERATOR_ONE_DROPDOWN:
            case GENERATOR_TWO_DROPDOWN:
                LOG.log(Level.INFO, selection + " Value: " + value);
                handleGenerator(value, selection);
                break;

            case MIXER_DROPDOWN:
                LOG.log(Level.INFO, selection + " Value: " + value);
                createMessage(ValidCommand.CHANGE_MIXER, value);
                break;

            case BUTTON_RANDOM_CONFIGURATION:
                LOG.log(Level.INFO, selection + " Value: " + value);
                createMessage(ValidCommand.RANDOMIZE, value);
                break;

            case BUTTONS_RANDOM_MODE:
                LOG.log(Level.INFO, selection + " Value: " + value);
                handleRandomMode(value);
                break;

            case BUTTON_RANDOM_PRESET:
                LOG.log(Level.INFO, selection + " Value: " + value);
                createMessage(ValidCommand.PRESET_RANDOM, value);
                break;

            case CURRENT_VISUAL:
                LOG.log(Level.INFO, selection + " Value: " + value);
                createMessage(ValidCommand.CURRENT_VISUAL, value);
                break;

            case CURRENT_OUTPUT:
                List<Boolean> outputs = new ArrayList<Boolean>();
                if (theEvent.getGroup().getArrayValue() == null) {
                    LOG.log(Level.WARNING, "no array data provided");
                    return;
                }

                for (float f : theEvent.getGroup().getArrayValue()) {
                    if (f == 0 ? outputs.add(Boolean.FALSE) : outputs.add(Boolean.TRUE))
                        ;
                }
                LOG.log(Level.INFO, selection + ": " + value);
                createMessage(ValidCommand.CURRENT_OUTPUT, value);
                break;

            case FX_ROTOZOOMER:
                LOG.log(Level.INFO, selection + ": " + intVal);
                createMessage(ValidCommand.CHANGE_ROTOZOOM, intVal);
                break;

            case BLINKENLIGHTS_DROPDOWN:
                name = getTextFromCaptionLabel(theEvent);
                LOG.log(Level.INFO, selection + " " + name);
                createMessage(ValidCommand.BLINKEN, name);
                break;

            case IMAGE_DROPDOWN:
                name = getTextFromCaptionLabel(theEvent);
                LOG.log(Level.INFO, selection + " " + name);
                createMessage(ValidCommand.IMAGE, name);
                break;

            case OUTPUT_FADER_DROPDOWN:
                LOG.log(Level.INFO, selection + " " + value);
                createMessage(ValidCommand.CHANGE_OUTPUT_FADER, value);
                break;

            case OUTPUT_SELECTED_VISUAL_DROPDOWN:
                LOG.log(Level.INFO, selection + " " + value);
                createMessage(ValidCommand.CHANGE_OUTPUT_VISUAL, value);
                break;

            case OUTPUT_ALL_SELECTED_VISUAL_DROPDOWN:
                LOG.log(Level.INFO, selection + " " + value);
                createMessage(ValidCommand.CHANGE_ALL_OUTPUT_VISUAL, value);
                break;

            case OUTPUT_ALL_FADER_DROPDOWN:
                LOG.log(Level.INFO, selection + " " + value);
                createMessage(ValidCommand.CHANGE_ALL_OUTPUT_FADER, value);
                break;

            case TEXTUREDEFORM_OPTIONS:
                LOG.log(Level.INFO, selection + " " + value);
                createMessage(ValidCommand.TEXTDEF, value);
                break;

            case ZOOM_OPTIONS:
                LOG.log(Level.INFO, selection + " " + value);
                createMessage(ValidCommand.ZOOMOPT, value);
                break;

            case COLORSCROLL_OPTIONS:
                LOG.log(Level.INFO, selection + " " + value);
                createMessage(ValidCommand.COLOR_SCROLL_OPT, value);
                break;

            case TEXTFIELD:
                name = theEvent.getStringValue();
                LOG.log(Level.INFO, selection + " " + name);
                createMessage(ValidCommand.TEXTWR, name);
                break;

            case TEXTWR_OPTION:
                LOG.log(Level.INFO, selection + " " + value);
                createMessage(ValidCommand.TEXTWR_OPTION, value);
                break;

            case RANDOM_ELEMENT:
                if (theEvent.getGroup().getArrayValue() == null) {
                    LOG.log(Level.WARNING, "no array data provided");
                    return;
                }

                String param = "";
                for (float ff : theEvent.getGroup().getArrayValue()) {
                    if (ff < 0.5f) {
                        param += "0 ";
                    } else {
                        param += "1 ";
                    }
                }
                LOG.log(Level.INFO, selection + " " + param);
                createShufflerMessage(param);
                break;

            case COLOR_SET_DROPDOWN:
                LOG.log(Level.INFO, selection + " " + value);
                createMessage(ValidCommand.CURRENT_COLORSET, value);
                break;

            case PRESET_BUTTONS:
                LOG.log(Level.INFO, selection + " " + intVal);
                createMessage(ValidCommand.CHANGE_PRESET, intVal);
                callback.updateCurrentPresetState();
                break;

            case LOAD_PRESET:
                LOG.log(Level.INFO, "LOAD_PRESET");
                createMessage(ValidCommand.LOAD_PRESET, "");
                callback.updateCurrentPresetState();
                break;

            case SAVE_PRESET:
                LOG.log(Level.INFO, "SAVE_PRESET");
                createMessage(ValidCommand.SAVE_PRESET, callback.getCurrentPresetName());
                callback.updateCurrentPresetState();//to show the new name
                break;

            case BUTTON_TOGGLE_FREEZE:
                createMessage(ValidCommand.FREEZE, "");
                break;

            case BUTTON_TOGGLE_INTERNAL_VISUALS:
                toggleInternalVisuals();
                break;

            case BRIGHTNESS:
                float brightness = value;
                createMessage(ValidCommand.CHANGE_BRIGHTNESS, brightness);
                break;

            case SAVE_SCREENSHOT:
                createMessage(ValidCommand.SCREENSHOT, "");
                break;

            case GENERATOR_SPEED:
                createMessage(ValidCommand.GENERATOR_SPEED, value);
                break;

            case BEAT_WORKMODE:
                createMessage(ValidCommand.BEAT_WORKMODE, value);
                break;

            default:
                LOG.log(Level.INFO, "Invalid Object: " + selection + ", Value: " + value);
                break;
        }
    }

    private String getTextFromCaptionLabel(ControlEvent theEvent) {
        if (theEvent.getGroup().getCaptionLabel() == null) {
            return "";
        }
        return theEvent.getGroup().getCaptionLabel().getText();
    }

    /**
     * 
     * @param msg
     */
    private void singleSendMessageOut(String[] msg) {
        if (System.currentTimeMillis() - lastCallbackEvent < CALLBACK_TIMEOUT) {
            // do not flood the gui
            return;
        }

        pixConSrv.sendMessage(msg);
        lastCallbackEvent = System.currentTimeMillis();
    }


    private void createMessage(ValidCommand validCommand, float newValue) {
        String[] msg = new String[2];
        msg[0] = "" + validCommand;
        msg[1] = "" + (int) newValue;
        singleSendMessageOut(msg);
    }

    private void createMessageFromString(final String cmd, final float newValue) {
        String[] msg = new String[2];
        msg[0] = cmd;
        msg[1] = "" + (int) newValue;
        singleSendMessageOut(msg);
    }

    private void toggleInternalVisuals() {
        if (internalVisualVisible) {
            internalVisualVisible = false;
        } else {
            internalVisualVisible = true;
        }
    }

    /**
     * @return the internalVisualVisible
     */
    public boolean isInternalVisualVisible() {
        return internalVisualVisible;
    }

    /**
     * 
     * @param validCommand
     * @param newValue
     */
    private void createMessage(ValidCommand validCommand, String newValue) {
        String[] msg = new String[2];
        msg[0] = "" + validCommand;
        msg[1] = newValue;
        singleSendMessageOut(msg);
    }

    /**
     * 
     * @param param
     */
    private void createShufflerMessage(String param) {
        String[] msg = new String[param.length() + 1];
        msg[0] = "" + ValidCommand.CHANGE_SHUFFLER_SELECT;
        String[] tmp = param.split(" ");
        System.arraycopy(tmp, 0, msg, 1, tmp.length);
        singleSendMessageOut(msg);
    }

    /**
     * toggle random mode on and off
     * 
     * @param newValue
     */
    private void handleRandomMode(float newValue) {
        String[] msg = new String[2];

        if (newValue == 0.0) {
            msg[0] = "" + ValidCommand.RANDOM;
            msg[1] = "ON";
        } else if (newValue == 1.0) {
            msg[0] = "" + ValidCommand.RANDOM_PRESET_MODE;
            msg[1] = "ON";
        } else if (newValue == -1.0) {
            msg[0] = "" + ValidCommand.RANDOM;
            msg[1] = "OFF";
        }

        singleSendMessageOut(msg);
    }

    /**
     * 
     * @param newValue
     * @param source
     */
    private void handleEffect(float newValue, GuiElement source) {
        String[] msg = new String[2];

        if (source == GuiElement.EFFECT_ONE_DROPDOWN) {
            msg[0] = "" + ValidCommand.CHANGE_EFFECT_A;
        } else {
            msg[0] = "" + ValidCommand.CHANGE_EFFECT_B;
        }
        msg[1] = "" + (int) newValue;
        singleSendMessageOut(msg);
    }

    /**
     * 
     * @param newValue
     * @param source
     */
    private void handleGenerator(float newValue, GuiElement source) {
        String[] msg = new String[2];

        if (source == GuiElement.GENERATOR_ONE_DROPDOWN) {
            msg[0] = "" + ValidCommand.CHANGE_GENERATOR_A;
        } else {
            msg[0] = "" + ValidCommand.CHANGE_GENERATOR_B;
        }
        msg[1] = "" + (int) newValue;
        singleSendMessageOut(msg);
    }

}
