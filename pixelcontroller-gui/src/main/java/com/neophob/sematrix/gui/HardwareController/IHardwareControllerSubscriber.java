package com.neophob.sematrix.gui.HardwareController;

/**
 * Created by arne on 25.12.2015.
 */
public interface IHardwareControllerSubscriber {
    void buttonPressed(final IHardwareController.HWButton button);
    void sliderChanged(final IHardwareController.HWSlider slider, final int newValue);
}
