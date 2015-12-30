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
    private static final int holdButton = 82;

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
            sendMsg(ValidCommand.LOAD_PRESET);//the message will cause a call to displayPreset()
        }
        else if(button == holdButton) {
            holdMode = !holdMode;
            hw.setButtonState(holdButton, holdMode ? holdButtonActive : holdButtonInactive);
        }
    }

    @Override
    public void buttonReleased(int button) {
        if(buttonDown == button) //int is used instead of bool to stop the user from pressing two buttons and releasing the wrong one first
            buttonDown = -1;
        else
            return;//wait for the correct button to be released first
        if(holdMode && button >= 0 && button < PresetService.NR_OF_PRESET_SLOTS) {//slot released
            createMessage(ValidCommand.CHANGE_PRESET, (oldPreset));
            sendMsg(ValidCommand.LOAD_PRESET);//the message will cause a call to displayPreset()
            //displayPreset(buttonToPreset(oldPreset));
        }
    }

    @Override
    public void sliderChanged(int slider, int newValue) {
        if(slider == speedSlider) {
            final int val = map(newValue, 0, 127, 0, 200);
            createMessage(ValidCommand.GENERATOR_SPEED, val);
        }
        else if(slider == brightnessSlider)
        {
            final int val = map(newValue, 0, 127, 0, 100);
            createMessage(ValidCommand.CHANGE_BRIGHTNESS, val);
        }

    }

    private void createMessage(ValidCommand validCommand, float newValue) {
        String[] msg = new String[2];
        msg[0] = "" + validCommand;
        msg[1] = "" + (int) newValue;
        server.sendMessage(msg);
    }

    private void sendMsg(ValidCommand command) {
        String[] msg = new String[1];
        msg[0] = "" + command;
        server.sendMessage(msg);
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

        hw.setButtonState(64, HWButtonState.RED);
        hw.setButtonState(65, HWButtonState.RED);
        hw.setButtonState(holdButton, holdMode ? holdButtonActive : holdButtonInactive);
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
                else if(tmp[0].equals("CHANGE_BRIGHTNESS")) {

                }
                else if(tmp[0].equals("CHANGE_BRIGHTNESS"))  {
                    //FIXME there is no way to update the controllers sliders since they dont have motors :(
                }
                else if(tmp[0].equals("GENERATOR_SPEED"))
                {
                    //FIXME there is no way to update the controllers sliders since they dont have motors :(
                }
            }
        }
    }
}
