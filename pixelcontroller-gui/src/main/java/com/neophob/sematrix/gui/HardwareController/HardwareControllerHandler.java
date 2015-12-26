package com.neophob.sematrix.gui.HardwareController;

import com.neophob.sematrix.core.properties.ValidCommand;
import com.neophob.sematrix.gui.service.PixConServer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by arne on 25.12.2015.
 */
public class HardwareControllerHandler implements IHardwareControllerSubscriber {

    private PixConServer server;
    private static final IHardwareController.HWSlider speedSlider = IHardwareController.HWSlider.SL_0;
    private static final IHardwareController.HWSlider brightnessSlider = IHardwareController.HWSlider.SL_1;
    public HardwareControllerHandler(final PixConServer server) {
        this.server = server;
    }

    public void subscribeTo(IHardwareController ctrl)
    {
        ctrl.subscribe(this);
    }


    @Override
    public void buttonPressed(int button) {

        createMessage(ValidCommand.CHANGE_PRESET, buttonToPreset(button));
        sendMsg(ValidCommand.LOAD_PRESET);
        server.refreshGuiState();
    }

    @Override
    public void sliderChanged(int slider, int newValue) {
        if(slider == speedSlider) {
            final int val = map(newValue, 0, 127, 0, 200);
            createMessage(ValidCommand.GENERATOR_SPEED, val);
            server.refreshGuiState();
        }
        else if(slider == brightnessSlider)
        {
            final int val = map(newValue, 0, 127, 0, 100);
            createMessage(ValidCommand.CHANGE_BRIGHTNESS, val);
            server.refreshGuiState();
        }
        else
        {
            throw new NotImplementedException();
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

    private int buttonToPreset(IHardwareController.HWButton button)
    {
        //this is tedious but fast
        switch (button) {
            case PB_0:
                return 0;
            case PB_1:
                return 1;
            case PB_2:
                return 2;
            case PB_3:
                return 3;
            default:
                throw new NotImplementedException();
        }
    }

    private int map(int x, int in_min, int in_max, int out_min, int out_max)
    {//http://stackoverflow.com/questions/7505991/arduino-map-equivalent-function-in-java
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }


}
