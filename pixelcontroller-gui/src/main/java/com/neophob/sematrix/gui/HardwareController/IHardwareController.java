package com.neophob.sematrix.gui.HardwareController;

import javax.sound.midi.MidiUnavailableException;

/**
 * Created by arne on 25.12.2015.
 */
public interface IHardwareController {

    enum HWButton {
        PB_0(0),
        PB_1(1),
        PB_2(2),
        PB_3(3),
        PB_4(4),
        PB_5(5),
        PB_6(6),
        PB_7(7),
        PB_8(8),
        PB_9(9),
        PB_10(10),
        PB_11(11),
        PB_12(12),
        PB_13(13),
        PB_14(14),
        PB_15(15),
        PB_16(16),
        PB_17(17),
        PB_18(18),
        PB_19(19),
        PB_20(20),
        PB_21(21),
        PB_22(22),
        PB_23(23),
        PB_24(24),
        PB_25(25),
        PB_26(26),
        PB_27(27),
        PB_28(28),
        PB_29(29),
        PB_30(30),
        PB_31(31),
        PB_32(32),
        PB_33(33),
        PB_34(34),
        PB_35(35),
        PB_36(36),
        PB_37(37),
        PB_38(38),
        PB_39(39),
        PB_40(40),
        PB_41(41),
        PB_42(42),
        PB_43(43),
        PB_44(44),
        PB_45(45),
        PB_46(46),
        PB_47(47),
        PB_48(48),
        PB_49(49),
        PB_50(50),
        PB_51(51),
        PB_52(52),
        PB_53(53),
        PB_54(54),
        PB_55(55),
        PB_56(56),
        PB_57(57),
        PB_58(58),
        PB_59(59),
        PB_60(60),
        PB_61(61),
        PB_62(62),
        PB_63(63);

        public final int value;

        HWButton(final int val){
            this.value = val;
        }
    }
    enum HWButtonState {
        RED, GREEN, YELLOW, ON, OFF, RED_BLINK, GREEN_BLINK, YELLOW_BLINK
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

    /**@note not all buttons may support all states.  */
    void setButtonState(final int button, final HWButtonState state);

    /**Opens a connection to the device.
     * @return True if the device was opened, false otherwise */
    boolean open() throws MidiUnavailableException;

}
