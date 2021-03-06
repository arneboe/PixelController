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

import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.core.properties.ValidCommand;
import com.neophob.sematrix.gui.guibuilder.GeneratorGui;
import com.neophob.sematrix.gui.guibuilder.GuiCallbackAction;
import com.neophob.sematrix.gui.service.PixConServer;

/**
 * 
 * PixelController Keyboard handler
 * 
 * @author mvogt
 * 
 */
public final class KeyboardHandler {

    private static final Logger LOG = Logger.getLogger(KeyboardHandler.class.getName());

    private static GuiCallbackAction registerGuiClass;
    private static PixConServer pixConServer;
    public static GeneratorGui gui;

    private KeyboardHandler() {
        // no instance
    }

    /**
     * 
     * @param key
     */
    public static void keyboardHandler(char key, int keyCode) {

        // if a user press a key during the setup - ignore it!
        if (registerGuiClass == null || pixConServer == null) {
            LOG.log(Level.INFO, "Keyboard handler not initialized yet.");
            return;
        }

        // if we edit a textfield, ignore keyboard shortcuts
        if (registerGuiClass.isTextfieldInEditMode()) {
            return;
        }

        switch (key) {

        // change current Colorset
            case 'C':
                sendMsg(ValidCommand.ROTATE_COLORSET);
                break;
            case 'D':
                sendMsg(ValidCommand.ROTATE_COLORSET_BACK);
                break;

            // change current generator 1
            case 'F':
                sendMsg(ValidCommand.ROTATE_GENERATOR_A);
                break;

            // change current generator 2
            case 'G':
                sendMsg(ValidCommand.ROTATE_GENERATOR_B);
                break;

            // change current effect 1
            case 'W':
                sendMsg(ValidCommand.ROTATE_EFFECT_A);
                break;

            // change current effect 2
            case 'E':
                sendMsg(ValidCommand.ROTATE_EFFECT_B);
                break;

            // change current mixer
            case 'M':
                sendMsg(ValidCommand.ROTATE_MIXER);
                break;

            // randomize
            case 'R':
                sendMsg(ValidCommand.RANDOMIZE);
                break;

            default:
                break;
        }

        if (registerGuiClass != null) {
            // select previous/next tab
            switch (keyCode) {
                case KeyEvent.VK_LEFT:
                    gui.decreaseBrightness();
                    break;
                case KeyEvent.VK_RIGHT:
                    gui.increaseBrightness();
                    break;
                case KeyEvent.VK_UP:
                    if(gui != null)
                    {
                        gui.increaseSpeed();
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if(gui != null)
                    {
                        gui.decreaseSpeed();
                    }
                    break;
                case KeyEvent.VK_F1:
                    createMessage(ValidCommand.CHANGE_PRESET, 11);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
                case KeyEvent.VK_F2:
                    createMessage(ValidCommand.CHANGE_PRESET, 12);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
                case KeyEvent.VK_F3:
                    createMessage(ValidCommand.CHANGE_PRESET, 13);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
                case KeyEvent.VK_F4:
                    createMessage(ValidCommand.CHANGE_PRESET, 14);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
                case KeyEvent.VK_F5:
                    createMessage(ValidCommand.CHANGE_PRESET, 15);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
                case KeyEvent.VK_F6:
                    createMessage(ValidCommand.CHANGE_PRESET, 16);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
                case KeyEvent.VK_F7:
                    createMessage(ValidCommand.CHANGE_PRESET, 17);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
                case KeyEvent.VK_F8:
                    createMessage(ValidCommand.CHANGE_PRESET, 18);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
                case KeyEvent.VK_F9:
                    createMessage(ValidCommand.CHANGE_PRESET, 19);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
                case KeyEvent.VK_F10:
                    createMessage(ValidCommand.CHANGE_PRESET, 20);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
                case KeyEvent.VK_F11:
                    createMessage(ValidCommand.CHANGE_PRESET, 21);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
                case KeyEvent.VK_F12:
                    createMessage(ValidCommand.CHANGE_PRESET, 22);
                    sendMsg(ValidCommand.LOAD_PRESET);
                    pixConServer.refreshGuiState();
                    break;
            }
        }

        if (key >= '1' && key <= '9') {
            if (registerGuiClass != null) {
                createMessage(ValidCommand.CHANGE_PRESET, (int) key - 49);
                sendMsg(ValidCommand.LOAD_PRESET);
                pixConServer.refreshGuiState();
            }
        }

        if(key == '0')
        {
            if (registerGuiClass != null) {
                createMessage(ValidCommand.CHANGE_PRESET, 10);
                sendMsg(ValidCommand.LOAD_PRESET);
                pixConServer.refreshGuiState();
            }
        }
    }

    private static void createMessage(ValidCommand validCommand, float newValue) {
        String[] msg = new String[2];
        msg[0] = "" + validCommand;
        msg[1] = "" + (int) newValue;
        pixConServer.sendMessage(msg);
    }

    private static void sendMsg(ValidCommand command) {
        String[] msg = new String[1];
        msg[0] = "" + command;
        pixConServer.sendMessage(msg);
    }

    /**
     * @param registerGuiClass
     *            the registerGuiClass to set
     */
    public static void init(GuiCallbackAction registerGuiClass, PixConServer pixConServer) {
        KeyboardHandler.registerGuiClass = registerGuiClass;
        KeyboardHandler.pixConServer = pixConServer;
    }

}
