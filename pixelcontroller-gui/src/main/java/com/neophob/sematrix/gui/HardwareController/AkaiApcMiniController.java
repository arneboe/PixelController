package com.neophob.sematrix.gui.HardwareController;
import javax.sound.midi.*;

/**
 * Created by arne on 26.12.2015.
 */
public class AkaiApcMiniController implements IHardwareController {

    private IHardwareControllerSubscriber subscriber;
    private MidiDevice device;
    private Receiver receiver;
    private ShortMessage msg; //buffered for repeated use

    public AkaiApcMiniController() {

        if (!(device.isOpen())) {
            try {
                device.open();
                receiver = device.getReceiver();
                msg = new ShortMessage();
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void subscribe(IHardwareControllerSubscriber sub) {
        subscriber = sub;
    }

    @Override
    public void setButtonState(int button, HWButtonState state) {
        if(button >= 0 && button < 64) //3 color push buttons
        {
            int cmd = 0;
            switch(state) {

                case RED:
                    cmd = 3;
                    break;
                case GREEN:
                    cmd = 01;
                    break;
                case YELLOW:
                    cmd = 05;
                    break;
                case ON:
                    throw new RuntimeException("The 3 color push buttons(0..63) do not support state.ON");
                case OFF:
                    cmd = 0;
                    break;
                case RED_BLINK:
                    cmd = 04;
                    break;
                case GREEN_BLINK:
                    cmd = 02;
                    break;
                case YELLOW_BLINK:
                    cmd = 06;
                    break;
                default:
                    throw new RuntimeException("Unknown button state");
            }
            send(button, cmd);
        }
        else if (button >= 64 && button < 72) { //red push buttons
            int cmd = 0;
            switch (state) {
                case OFF:
                    cmd = 0;
                    break;
                case RED:
                case ON:
                    cmd = 1;
                    break;
                case RED_BLINK:
                    cmd = 02;
                    break;
                case GREEN:
                case YELLOW:
                case GREEN_BLINK:
                case YELLOW_BLINK:
                    throw new RuntimeException("The red push buttons(64..71) do not support state." + state.toString());
                default:
                    throw new RuntimeException("Unknown button state");
            }
            send(button, cmd);
        }
        else if(button >= 82 && button < 90) { //green push buttons
            int cmd = 0;
            switch(state) {
                case GREEN:
                case ON:
                    cmd = 1;
                    break;
                case OFF:
                    cmd = 0;
                    break;
                case GREEN_BLINK:
                    cmd = 2;
                    break;
                case RED:
                case YELLOW:
                case RED_BLINK:
                case YELLOW_BLINK:
                    throw new RuntimeException("The green push buttons(82..89) do not support state." + state.toString());
                default:
                    throw new RuntimeException("Unknown button state");
            }
            send(button, cmd);
        }
    }

    private void send( final int byte1, final int byte2) {
        try {
            msg.setMessage(ShortMessage.NOTE_ON, byte1, byte2);
            receiver.send(msg, -1); //timestamp -1 means immediately
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }
}
