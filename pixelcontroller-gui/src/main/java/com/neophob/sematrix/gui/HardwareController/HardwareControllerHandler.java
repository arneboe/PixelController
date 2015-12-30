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
    private static final  HWButtonState unusedPresetColor = HWButtonState.YELLOW;
    private static final HWButtonState usedPresetColor = HWButtonState.GREEN;
    private static final HWButtonState unusedRedColor = HWButtonState.OFF;
    private static final HWButtonState unusedGreenColor = HWButtonState.OFF;
    private final IHardwareController hw;
    private PixConServer server;

    private int selectedPreset = 0; //the preset that is currently selected

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
        if(button >= 0 && button < PresetService.NR_OF_PRESET_SLOTS) {
            createMessage(ValidCommand.CHANGE_PRESET, button);
            sendMsg(ValidCommand.LOAD_PRESET);
            displayPreset(button);//highlight the button on the controller
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
    }

    /**Display @p preset  */
    private void displayPreset(final int preset) {
        //turn old button off
        //NOTE: selectedPreset can be -1 if the user did not specify a default preset in the config.properties
        if(selectedPreset >= 0 && selectedPreset < 64) {
            hw.setButtonState(selectedPreset, unusedPresetColor);
        }
        //turn new button on
        if(preset >= 0 && preset < 64) {
            hw.setButtonState(preset, usedPresetColor);
        }
        selectedPreset = preset;
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
                else if(tmp[0].equals("CHANGE_BRIGHTNESS"))
                {
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
