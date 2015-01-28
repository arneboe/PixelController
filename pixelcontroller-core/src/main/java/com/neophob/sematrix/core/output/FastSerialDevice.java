package com.neophob.sematrix.core.output;


import com.neophob.sematrix.core.properties.ApplicationConfigurationHelper;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends rgb data to a serial port with one byte overhead per frame.
 * This device fires frames as fast as possible.
 * Each frame is initiated by a zero byte.
 * Therefore zero is not a valid color. I.e. the leds will never be fully off. However the difference between
 * 1 and zero is invisibly small anyway.
 * @author Arne Böckmann
 */
public class FastSerialDevice extends Output {

    private static final Logger LOG = Logger.getLogger(FastSerialDevice.class.getName());
    private int baud = 115200;//FIXME 250000 should be possible with the arduino
    private Serial port;
    private String serialPortName;
    private boolean initialized = false;


    public FastSerialDevice(String portName, ApplicationConfigurationHelper ph) {
        super(OutputDeviceEnum.FASTSERIAL, ph, 8);
        //FIXME maybe reduce bbp to 4 if the speed is not high enough. Wont be visible anyway.

        if (portName != null && !portName.trim().isEmpty()) {
            LOG.log(Level.INFO, "Opening serial port: {0}", portName);
            serialPortName = portName;
            try {
                openPort(portName);
            } catch (NoSerialPortFoundException e) {
                e.printStackTrace();
            }
            initialized = true;
        }
    }


    /**
     * Open serial port with given name.
     *
     * @param portName the port name
     * @throws NoSerialPortFoundException if the port could not be opened
     */
    private void openPort(String portName) throws NoSerialPortFoundException {
        if (portName == null) {
            return;
        }

        try {
            port = new Serial(portName, this.baud);
            Thread.sleep(Serial.serialTimeout); //give it time to initialize
            if(null == port.port) {
                throw new NoSerialPortFoundException("Unable to initialize serial port");
            }
            LOG.log(Level.INFO, "Opened serial port: {0}", portName);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to open port <" + portName + ">", e);
            if (port != null) {
                port.stop();
            }
            port = null;
            throw new NoSerialPortFoundException("Failed to open port " + portName + ": " + e);
        }
    }



    @Override
    public void update() {

    }

    @Override
    public void close() {

    }
}
