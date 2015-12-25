package com.neophob.sematrix.gui.HardwareController;

import java.util.HashMap;

/**
 * Created by arne on 25.12.2015.
 */
public interface IHardwareController {

    enum HWButton {
        PB_0(0),
        PB_1(1),
        PB_2(2),
        PB_3(3);

        public final int value;

        HWButton(final int val){
            this.value = val;
        }
    }
    enum HWButtonColor {
        RED, GREEN, YELLOW, ON, OFF;
    }

    enum HWSlider {
        SL_0(0),
        SL_1(1),
        SL_2(2);

        public final int value;

        HWSlider(final int val){
            this.value = val;
        }
    }

    void subscribe(IHardwareControllerSubscriber sub);
    void setButtonColor(final HWButton button, final HWButtonColor col);
}
