package com.neophob.sematrix.gui.HardwareController;

/**
 * Created by arne on 25.12.2015.
 */
public interface IHardwareControllerSubscriber {
    void buttonPressed(final int button);
    void buttonReleased(final int button);
    void sliderChanged(final int slider, final int newValue);
}
