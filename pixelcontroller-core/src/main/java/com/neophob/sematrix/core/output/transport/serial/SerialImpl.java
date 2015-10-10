/**
 * Copyright (C) 2011-2014 Michael Vogt <michu@neophob.com>
 *
 * This file is part of PixelController.
 *
 * PixelController is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PixelController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PixelController.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.neophob.sematrix.core.output.transport.serial;

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

public class SerialImpl implements ISerial {

    private static final Logger LOG = Logger.getLogger(SerialImpl.class.getName());

    private SerialP5 processingSerial;

    public SerialImpl() {
        processingSerial = null;
    }

    @Override
    public void openPort(String name, int baud) {
        LOG.log(Level.INFO, "Try to open serial port {0}", name);
        processingSerial = new SerialP5(name, baud);
    }

    @Override
    public int available() {
        if (processingSerial == null) {
            return 0;
        }
        return processingSerial.available();
    }

    @Override
    public void clear() {
        if (processingSerial != null) {
            processingSerial.clear();
        }
    }

    @Override
    public void write(byte[] bytes) {
        if (processingSerial != null) {
            processingSerial.write(bytes);
        }
    }

    @Override
    public void closePort() {
        if (processingSerial != null) {
            LOG.log(Level.INFO, "Close serial port");
            processingSerial.stop();
        }
    }

    @Override
    public String readString() {
        if (processingSerial == null) {
            return "";
        }
        return processingSerial.readString();
    }

    @Override
    public byte[] readBytes() {
        if (processingSerial == null) {
            return new byte[0];
        }
        return processingSerial.readBytes();
    }

    @Override
    public OutputStream getOutputStream() {
        if (processingSerial == null) {
            return null;
        }
        return processingSerial.output;
    }

    /**
     * serial port names are CASE SENSITIVE. this sounds logically on unix
     * platform however com1 will not work on windows, there all names need to
     * be in uppercase (COM1)
     * 
     * see https://github.com/neophob/PixelController/issues/30 for more details
     * 
     * @param configuredName
     * @return
     */
    @Override
    public String getSerialPortName(String configuredName) {
        for (String portName : SerialP5.list()) {
            if (StringUtils.equalsIgnoreCase(portName, configuredName)) {
                return portName;
            }
        }

        LOG.log(Level.SEVERE, "Could not find configured serial name: {0}, try anyway...",
                configuredName);
        // we didn't found the port, hope that the provided name will work...
        return configuredName;
    }

    @Override
    public String[] getAllSerialPorts() {
        return SerialP5.list();
    }

    @Override
    public boolean isConnected() {
        if (processingSerial == null) {
            return false;
        }
        return processingSerial.output != null;
    }

    @Override
    public String getConnectedPortname() {
        if (isConnected()) {
            processingSerial.port.getName();
        }
        return "";
    }

}
