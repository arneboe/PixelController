package com.neophob.sematrix.gui.HardwareController;
import com.sun.media.sound.MidiInDeviceProvider;

import javax.sound.midi.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @see http://community.akaipro.com/akai_professional/topics/midi-information-for-apc-mini2
 *       for midi protocol
 */
public class AkaiApcMiniController implements IHardwareController, Receiver {

    protected static final Logger LOG = Logger.getLogger(AkaiApcMiniController.class.getName());
    private IHardwareControllerSubscriber subscriber;
    private MidiDevice receiverDevice;
    private MidiDevice transmitterDevice;
    private Receiver receiver;//used to send midi to the device
    private Transmitter transmitter;//usedto receive midi from the device
    private ShortMessage msg; //buffered for repeated use

    /**The different device names that the apc uses on different operating systems */
    private static final String[] deviceNames = {
            "APC MINI",//on windows 7
            "MINI [hw:1,0,0]"//on 3.17.8-1-MANJARO Linux
    };

    public AkaiApcMiniController() {

    }

    @Override
    public void subscribe(IHardwareControllerSubscriber sub) {
        subscriber = sub;
    }

    @Override
    public void setButtonState(int button, HWButtonState state) {
        if(button >= 0 && button < 64) //3 color push buttons
        {
            int cmd;
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
            int cmd;
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
            int cmd;
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

    @Override
    public boolean open() throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        //the apc mini provides two midi devices, one for input and one for output
        for(MidiDevice.Info info : infos) {
            if(isApcDeviceName(info.getName())) {
                LOG.log(Level.INFO, "Found apc mini device");
                MidiDevice dev = MidiSystem.getMidiDevice(info);
                //figure out if this the input or the output device
                try {
                    receiver = dev.getReceiver();
                    receiverDevice = dev;
                    LOG.log(Level.INFO, "Found apc mini receiver");
                    continue;
                }
                catch(MidiUnavailableException e) {}
                try {
                    transmitter = dev.getTransmitter();
                    transmitterDevice = dev;
                    LOG.log(Level.INFO, "Found apc mini transmitter");
                    continue;

                }
                catch(MidiUnavailableException e) {}
            }
        }
        if(receiver != null && transmitter != null) {
            transmitter.setReceiver(this);//subscribe to the transmitter to get midi data from the device
            receiverDevice.open();
            transmitterDevice.open();
            msg = new ShortMessage();
            return true;
        }
        return false;
    }

    /**Send a NOTE_ON message with two byte payload */
    private void send( final int byte1, final int byte2) {
        try {
            msg.setMessage(ShortMessage.NOTE_ON, byte1, byte2);
            receiver.send(msg, -1); //timestamp -1 means immediately
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    /**Is called when midi data is received from the device */
    @Override
    public void send(MidiMessage message, long timeStamp) {
        if(subscriber != null) {

            if(message.getStatus() == ShortMessage.NOTE_OFF) { //button released
                final int button = message.getMessage()[1];
                subscriber.buttonPressed(button);
            }
            else if(message.getStatus() == ShortMessage.CONTROL_CHANGE) { //slider moved
                final byte[] msg = message.getMessage();
                final int slider = msg[1];
                final int value = msg[2];
                subscriber.sliderChanged(slider, value);
            }

        }
    }

    private boolean isApcDeviceName(final String name) {
        for(final String devName : deviceNames) {
            if(name.equals(devName))
                return true;
        }
        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    @Override
    public void close() {
        if(null != transmitter) {
            transmitter.close();
            transmitter = null;
        }
        if(null != receiver) {
            receiver.close();
            receiver = null;
        }
        if(null != receiverDevice && receiverDevice.isOpen()) {
            receiverDevice.close();
            receiverDevice = null;
        }
        if(null != transmitterDevice && transmitterDevice.isOpen()) {
            transmitterDevice.close();
            transmitterDevice = null;
        }
        if(null != msg)
            msg = null;
    }
}
