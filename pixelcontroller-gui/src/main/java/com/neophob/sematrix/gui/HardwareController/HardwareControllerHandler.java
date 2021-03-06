package com.neophob.sematrix.gui.HardwareController;

import com.neophob.sematrix.core.preset.PresetService;
import com.neophob.sematrix.core.properties.ValidCommand;
import com.neophob.sematrix.gui.HardwareController.IHardwareController.HWButtonState;
import com.neophob.sematrix.gui.service.PixConServer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Connects a HardwareController to the PixConServer.
 * Handles two-way communication between the hardware and the PixConServer
 */
public class HardwareControllerHandler implements IHardwareControllerSubscriber, Observer {

    private static final int speedSlider = 48;
    private static final int brightnessSlider = 49;
    private static final int stroboSlider = 50;
    private static final int holdButton = 82;
    private static final int prevColorSetButton = 70;
    private static final int nextColorSetButton = 71;

    private static final  HWButtonState holdButtonActive = HWButtonState.GREEN_BLINK;
    private static final  HWButtonState holdButtonInactive = HWButtonState.GREEN;
    private static final  HWButtonState unusedPresetColor = HWButtonState.YELLOW;
    private static final HWButtonState usedPresetColor = HWButtonState.GREEN;
    private static final HWButtonState unusedRedColor = HWButtonState.OFF;
    private static final HWButtonState unusedGreenColor = HWButtonState.OFF;
    private final IHardwareController hw;
    private PixConServer server;

    /**If holdMode is enabled the presets are only switched for as long as the button is pressed, if it is released we
     * switch back to the previous preset */
    private boolean holdMode = false;
    private int buttonDown = -1; //number of the button that is currently beeing pressed (-1) if none

    private int selectedPreset = 0; //the preset that is currently selected
    private int oldPreset = 0; //only used in holdMode to switch back to the previous preset

    private int speed = 50;
    private int brightness = 50;
    private int stroboSpeed = 0;

    private int visual = 0;

    /**true if the controller is sending a message. used to avoid infinit recursion */
    private boolean sendingMsg = false;

    public HardwareControllerHandler(final PixConServer server, IHardwareController hw) {
        this.server = server;
        this.hw = hw;
        initController();
        hw.subscribe(this);
        server.observeVisualState(this); //register to get updates if the user changes something in the gui manually
        selectedPreset = server.getConfig().loadPresetOnStart();
        if(selectedPreset < 0) {
            selectedPreset = 0; //bad hack but the gui does the same
        }
        displayPreset(selectedPreset);
    }

    @Override
    public void buttonPressed(int button) {
        if(buttonDown != -1)//this code can only handle one button at a time
            return;
        buttonDown = button;
        if(button >= 0 && button < PresetService.NR_OF_PRESET_SLOTS) {
            oldPreset = selectedPreset;
            createMessage(ValidCommand.CHANGE_PRESET, (buttonToPreset(button)));
            createMessage(ValidCommand.LOAD_PRESET_AND_SET_VISUAL, visual);//the message will cause a call to displayPreset()
            createMessage(ValidCommand.GENERATOR_SPEED, speed);
            createMessage(ValidCommand.CHANGE_BRIGHTNESS, brightness);
            createMessage(ValidCommand.SET_STROBO_SPEED, stroboSpeed);
        }
        else if(button == holdButton) {
            holdMode = !holdMode;
            hw.setButtonState(holdButton, holdMode ? holdButtonActive : holdButtonInactive);
        }
        else if(button >= 64 && button < 69) {//select visual buttons
            hw.setButtonState(64 + visual, HWButtonState.RED);
            visual = button - 64;
            hw.setButtonState(64 + visual, HWButtonState.RED_BLINK);
            createMessage(ValidCommand.CHANGE_ALL_OUTPUT_VISUAL, visual);
            createMessage(ValidCommand.CURRENT_VISUAL, visual);
        }
        else if(button == prevColorSetButton) {
            sendMsg(ValidCommand.ROTATE_COLORSET_BACK);
        }
        else if(button == nextColorSetButton) {
            sendMsg(ValidCommand.ROTATE_COLORSET);
        }
    }

    @Override
    public void buttonReleased(int button) {
        if(buttonDown == button) //stop the user from pressing two buttons and releasing the wrong one first
            buttonDown = -1;
        else
            return;//wait for the correct button to be released first
        if(holdMode && button >= 0 && button < PresetService.NR_OF_PRESET_SLOTS) {//slot released
            createMessage(ValidCommand.CHANGE_PRESET, (oldPreset));
            createMessage(ValidCommand.LOAD_PRESET_AND_SET_VISUAL, visual);
            createMessage(ValidCommand.GENERATOR_SPEED, speed);
            createMessage(ValidCommand.CHANGE_BRIGHTNESS, brightness);
            createMessage(ValidCommand.SET_STROBO_SPEED, stroboSpeed);
        }
    }

    @Override
    public void sliderChanged(int slider, int newValue) {
        if(slider == speedSlider) {
            final int val = map(newValue, 0, 127, 0, 200);
            createMessage(ValidCommand.GENERATOR_SPEED, val);
            speed = val;
        }
        else if(slider == brightnessSlider)
        {
            final int val = map(newValue, 0, 127, 0, 100);
            brightness = val;
            createMessage(ValidCommand.CHANGE_BRIGHTNESS, val);
        }
        else if(slider == stroboSlider) {
            stroboSpeed =  map(newValue, 0, 127, 0, 255);
            createMessage(ValidCommand.SET_STROBO_SPEED, stroboSpeed);
        }

    }

    private void createMessage(ValidCommand validCommand, float newValue) {
        String[] msg = new String[2];
        msg[0] = "" + validCommand;
        msg[1] = "" + (int) newValue;
        sendingMsg = true;
        server.sendMessage(msg);
        sendingMsg = false;
    }

    private void sendMsg(ValidCommand command) {
        String[] msg = new String[1];
        msg[0] = "" + command;
        sendingMsg = true;
        server.sendMessage(msg);
        sendingMsg = false;
    }

    /**Sets all push button colors to the inital values */
    private void initController() {
        //FIXME blink is just for testing
        for(int i = 0; i < 64; ++i) {//3 color push buttons
            hw.setButtonState(i, unusedPresetColor);
        }
        for(int i = 64; i < 72; ++i) { //red push buttons
            hw.setButtonState(i, unusedRedColor);
        }   
        for(int i = 82; i < 90; ++i) { //green push buttons
            hw.setButtonState(i, unusedGreenColor);
        }
        for(int i = 64; i < 69; ++i) { //visual selection buttons
            hw.setButtonState(i, HWButtonState.RED);
        }
        hw.setButtonState(64 + visual, HWButtonState.RED_BLINK);

        hw.setButtonState(holdButton, holdMode ? holdButtonActive : holdButtonInactive);
        hw.setButtonState(prevColorSetButton, HWButtonState.RED);
        hw.setButtonState(nextColorSetButton, HWButtonState.RED);
    }

    /**Display @p preset  */
    private void displayPreset(final int preset) {
        //turn old button off
        //NOTE: selectedPreset can be -1 if the user did not specify a default preset in the config.properties
        if(selectedPreset >= 0 && selectedPreset < 64) {
            hw.setButtonState(presetToButton(selectedPreset), unusedPresetColor);
        }
        final int buttonNo = presetToButton(preset);
        //turn new button on
        if(buttonNo >= 0 && buttonNo < 64) {
            hw.setButtonState(buttonNo, usedPresetColor);
        }
        selectedPreset = preset;
    }


    /**The hardware starts indexing the buttons from bottom to top while the presets are indexed from top to bottom */
    private int presetToButton(final int preset) {
        //buttons are located in a 8x8 grid. We just need to invert the y-value
        final int presetRow = preset / 8;
        final int hardwareRow = 8 - presetRow - 1;
        final int col = preset % 8;
        return hardwareRow * 8 + col;
    }

    private int buttonToPreset(final int button) {
        /**The inverse conversion is the same, it is only in a different method for readability purposes */
        return presetToButton(button);
    }

    private int map(int x, int in_min, int in_max, int out_min, int out_max)
    {//http://stackoverflow.com/questions/7505991/arduino-map-equivalent-function-in-java
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }


    /**Is called whenever someone changes settings of the pixConServer  */
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof List<?>) {
            for (Object obj : (List<?>) arg) {
                String s = (String) obj;
                String[] tmp = s.split(" ");
                //we only care for brightness, speed and preset since that are the only things that can be controlled
                //using the hardware controller right now
                if(tmp[0].equals("CHANGE_PRESET")) {
                    assert(tmp.length > 1);//protocol requires at least one parameter :-)
                    final int preset = Integer.parseInt(tmp[1]);
                    if(preset != selectedPreset) {
                        displayPreset(preset);
                    }
                }
                else if(tmp[0].equals("CHANGE_BRIGHTNESS") && !sendingMsg) {
                    //override brightness changes by anyone else
                    createMessage(ValidCommand.CHANGE_BRIGHTNESS, brightness);

                }
                else if(tmp[0].equals("GENERATOR_SPEED") && !sendingMsg) {
                    //override speed changes by anyone else
                    createMessage(ValidCommand.GENERATOR_SPEED, speed);
                }
            }
        }
    }
}
