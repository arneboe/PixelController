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
package com.neophob.sematrix.core.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.core.visual.layout.MatrixLayout;
import org.apache.commons.lang3.StringUtils;

import com.neophob.sematrix.core.output.OutputDeviceEnum;
import com.neophob.sematrix.core.output.gamma.GammaType;
import com.neophob.sematrix.core.output.gamma.RGBAdjust;
import com.neophob.sematrix.core.visual.layout.BoxLayout;
import com.neophob.sematrix.core.visual.layout.HorizontalLayout;
import com.neophob.sematrix.core.visual.layout.Layout;

/**
 * load and save properties files.
 * 
 * note: fields marked with transient are not included in the serialization this
 * means all options that are not relevant for the gui should be marked as
 * transient
 * 
 * @author michu
 */
public class Configuration implements Serializable {

    private static final long serialVersionUID = -742970229384663801L;

    private static final transient Logger LOG = Logger.getLogger(Configuration.class.getName());

    private static final transient String ERROR_MULTIPLE_CABLING_METHOD_CONFIGURATED = "Multiple cabling options (snake cabling and custom mapping) configured, illegal configuration!";
    private static final transient String ERROR_INVALID_OUTPUT_MAPPING = "Invalid output mapping entries, output.mapping > output.resolution.x*output.resolution.y";
    private static final transient String FAILED_TO_PARSE = "Failed to parse {0}";

    private static final transient int MAXIMAL_PIXELS_PER_UNIVERSE = 170;

    protected Properties config = null;

    private OutputDeviceEnum deviceType = null;

    // output specific settings
    private List<Integer> i2cAddr = null;
    private List<String> rainbowduinoV3SerialDevices = null;
    private List<DeviceConfig> lpdDevice = null;
    private List<DeviceConfig> tpm2netDevice = null;
    private List<DeviceConfig> artNetDevice = null;
    private List<DeviceConfig> e131Device = null;
    private List<ColorFormat> colorFormat = null;

    /** define how the panels are arranged, used by pixelinvaders panels */
    private transient List<Integer> panelOrder = null;

    private transient List<String> pixelInvadersBlacklist;

    private transient Map<Integer, RGBAdjust> pixelInvadersCorrectionMap = new HashMap<Integer, RGBAdjust>();

    /**The device matrix. Access is [y][x], i.e. [row][col] */
    private DeviceConfig[][] deviceConfig;

    /** The x resolution of each device */
    private int deviceXResolution = 0;

    /** The y resolution of each */
    private int deviceYResolution = 0;

    /** user selected gamma correction */
    private GammaType gammaType;

    /** user selected startup output gain */
    private int startupOutputGain = 0;

    private String pixelinvadersNetIp;
    private int pixelinvadersNetPort;


    public Configuration(Properties config) {
        this.config = config;

        deviceType = parseDeviceType();
        LOG.log(Level.INFO, "Device type is " + deviceType.toString());
        switch(deviceType) {
            case E1_31:
                //TODO load additional config values?
                break;
            case NULL:
                deviceXResolution = ConfigDefault.DEFAULT_PIXELINVADERS_PANEL_RESOULTION;
                deviceYResolution = ConfigDefault.DEFAULT_PIXELINVADERS_PANEL_RESOULTION;
                break;
            default:
        }
        deviceConfig = parseDeviceLayout();
        LOG.log(Level.INFO, "Device config: Rows: " + deviceConfig.length + ", Columns: " + deviceConfig[0].length);
        final int totalDevices = deviceConfig.length * deviceConfig[0].length;
        deviceXResolution = parseOutputXResolution();
        deviceYResolution = parseOutputYResolution();

        //FIXME make output mapping work again
        //int outputMappingSize = getOutputMappingValues().length;
       // if (isOutputSnakeCabeling() && outputMappingSize > 0) {
       //     LOG.log(Level.SEVERE, ERROR_MULTIPLE_CABLING_METHOD_CONFIGURATED);
      //      throw new IllegalArgumentException(ERROR_MULTIPLE_CABLING_METHOD_CONFIGURATED);
      //  }

   /*     final int entries = this.deviceXResolution * this.deviceYResolution;
        if (outputMappingSize > 0 && outputMappingSize > entries) {
            String s = " (" + outputMappingSize + ">" + entries + ")";
            LOG.log(Level.SEVERE, ERROR_INVALID_OUTPUT_MAPPING + s);
            throw new IllegalArgumentException(ERROR_INVALID_OUTPUT_MAPPING + s);
        }
*/

        // add default color format RGB if nothing is configured
        int nrOfColorFormat = getColorFormatFromCfg();
        if (nrOfColorFormat < totalDevices) {
            if (nrOfColorFormat > 0) {
                LOG.log(Level.WARNING, "ColorFormat count mismatch, use RGB as default value!");
            }
            for (int i = nrOfColorFormat; i < totalDevices; i++) {
                colorFormat.add(ColorFormat.RGB);
            }
        }

        gammaType = parseGammaCorrection();
        startupOutputGain = parseStartupOutputGain();
    }

    private OutputDeviceEnum parseDeviceType() {
        final String value = config.getProperty(ConfigConstant.DEVICE_TYPE);
        return OutputDeviceEnum.valueOf(value);
    }

    /**
     * Parses the boolean.
     *
     * @param property
     *            the property
     * @return true, if successful
     */
    private boolean parseBoolean(String property) {
        String rawConfig = config.getProperty(property);
        if (StringUtils.isNotBlank(rawConfig)) {
            try {
                return Boolean.parseBoolean(rawConfig);
            } catch (Exception e) {
                LOG.log(Level.WARNING, FAILED_TO_PARSE, rawConfig);
            }
        }
        return false;
    }

    /**
     * get a int value from the config file.
     * 
     * @param property
     *            the property
     * @return the int
     */
    private int parseInt(String property, int defaultValue) {
        String rawConfig = config.getProperty(property);
        if (StringUtils.isNotBlank(rawConfig)) {
            try {
                int val = Integer.parseInt(StringUtils.strip(rawConfig));
                if (val >= 0) {
                    return val;
                } else {
                    LOG.log(Level.WARNING, "Ignored negative value {0}", rawConfig);
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, FAILED_TO_PARSE, rawConfig);
            }
        }
        return defaultValue;
    }

    private float parseFloat(String property, float defaultValue) {
        String rawConfig = config.getProperty(property);
        if (StringUtils.isNotBlank(rawConfig)) {
            try {
                float val = Float.parseFloat(StringUtils.strip(rawConfig));
                if (val >= 0) {
                    return val;
                } else {
                    LOG.log(Level.WARNING, "Ignored negative value {0}", rawConfig);
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, FAILED_TO_PARSE, rawConfig);
            }
        }
        return defaultValue;
    }

    /**
     *
     * @param property
     * @return
     */
    private int parseInt(String property) {
        return parseInt(property, 0);
    }

    /**
     * Gets the property.
     * 
     * @param key
     *            the key
     * @return the property
     */
    public String getProperty(String key) {
        return config.getProperty(key);
    }

    /**
     * Gets the property.
     * 
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     * @return the property
     */
    public String getProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    /**
     * 
     * @return
     */
    public DeviceConfig getOutputDeviceLayout() {
        String value = config.getProperty(ConfigConstant.OUTPUT_DEVICE_LAYOUT);
        try {
            if (value != null) {
                return DeviceConfig.valueOf(value);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, FAILED_TO_PARSE, value);
        }

        return DeviceConfig.NO_ROTATE;
    }



    /**
     * get the size of the software emulated matrix.
     * 
     * @return the size or -1 if nothing was defined
     */
    public int getLedPixelSize() {

        String tmp = config.getProperty(ConfigConstant.CFG_PIXEL_SIZE, ""
                + ConfigDefault.DEFAULT_GUI_PIXELSIZE);
        try {
            return Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, FAILED_TO_PARSE, e);
        }
        return ConfigDefault.DEFAULT_GUI_PIXELSIZE;

    }

    /**
     * Gets the color format from cfg.
     * 
     * @return the color format from cfg
     */
    private int getColorFormatFromCfg() {
        colorFormat = new ArrayList<ColorFormat>();
        String rawConfig = config.getProperty(ConfigConstant.CFG_PANEL_COLOR_ORDER);

        if (StringUtils.isNotBlank(rawConfig)) {
            for (String s : rawConfig.split(ConfigConstant.DELIM)) {
                try {
                    ColorFormat cf = ColorFormat.valueOf(StringUtils.strip(s));
                    colorFormat.add(cf);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, FAILED_TO_PARSE, s);
                }
            }
        }

        return colorFormat.size();
    }

    /**
     * 
     * @return
     */
    private GammaType parseGammaCorrection() {
        GammaType ret = GammaType.NONE;

        String rawConfig = config.getProperty(ConfigConstant.CFG_PANEL_GAMMA_TAB);
        if (StringUtils.isBlank(rawConfig)) {
            return ret;
        }

        try {
            ret = GammaType.valueOf(rawConfig);
        } catch (Exception e) {
            LOG.log(Level.WARNING, FAILED_TO_PARSE, ConfigConstant.CFG_PANEL_GAMMA_TAB);
        }
        return ret;
    }

    /**
     * Parses the startup global output gain
     *
     * @return int gain value from cfg or default
     */
    private int parseStartupOutputGain() {
        String tmp = config.getProperty(ConfigConstant.STARTUP_OUTPUT_GAIN, ""
                + ConfigDefault.DEFAULT_STARTUP_OUTPUT_GAIN);
        try {
            int ti = Integer.parseInt(tmp);
            if (ti < 0 || ti > 100) {
                LOG.log(Level.WARNING, ConfigConstant.STARTUP_OUTPUT_GAIN + ", invalid startup gain value: "
                    + ti);
            } else {
                return ti;
            }
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, FAILED_TO_PARSE, e);
        }
        return ConfigDefault.DEFAULT_STARTUP_OUTPUT_GAIN;
    }

    /**
     * get configured udp ip.
     * 
     * @return the udp ip
     */
    public String getUdpIp() {
        return config.getProperty(ConfigConstant.UDP_IP);
    }

    /**
     * get configured udp port.
     *
     * @return the udp port
     */
    public int getUdpPort() {
        return parseInt(ConfigConstant.UDP_PORT, ConfigDefault.DEFAULT_UDP_PORT);
    }

      /**
     * get configured OPC ip.
       *
     * @return the tcp ip
     */
    public String getOpcIp() {
        return config.getProperty(ConfigConstant.OPC_IP);
    }

    /**
     * get configured OPC port.
     *
     * @return the tcp port
     */
    public int getOpcPort() {
        return parseInt(ConfigConstant.OPC_PORT, ConfigDefault.DEFAULT_OPC_PORT);
    }

    /**
     * get configured e131 ip.
     * 
     * @return the e131 controller ip
     */
    public String getE131Ip() {
        return config.getProperty(ConfigConstant.E131_IP);
    }

    /**
     * how many pixels (=3 Channels) per DMX universe
     * 
     * @return
     */
    public int getE131PixelsPerUniverse() {
        int ppU = parseInt(ConfigConstant.E131_PIXELS_PER_UNIVERSE, MAXIMAL_PIXELS_PER_UNIVERSE);
        if (ppU > MAXIMAL_PIXELS_PER_UNIVERSE) {
            LOG.log(Level.WARNING, "Invalid configuration found, "
                    + ConfigConstant.E131_PIXELS_PER_UNIVERSE + "=" + ppU + ". Maximal value is "
                    + MAXIMAL_PIXELS_PER_UNIVERSE + ", I fixed that for you.");
            ppU = MAXIMAL_PIXELS_PER_UNIVERSE;
        }
        return ppU;
    }

    /**
     * get first arnet universe id
     * 
     * @return
     */
    public int getE131StartUniverseId() {
        return parseInt(ConfigConstant.E131_FIRST_UNIVERSE_ID, 0);
    }

    public int getRandomModeLifetime() {
        return parseInt(ConfigConstant.RANDOMMODE_LIFETIME, 0);
    }

    /**
     * get configured artnet ip.
     * 
     * @return the art net ip
     */
    public String getArtNetIp() {
        return config.getProperty(ConfigConstant.ARTNET_IP);
    }

    /**
     * how many pixels (=3 Channels) per DMX universe
     * 
     * @return
     */
    public int getArtNetPixelsPerUniverse() {
        int ppU = parseInt(ConfigConstant.ARTNET_PIXELS_PER_UNIVERSE, MAXIMAL_PIXELS_PER_UNIVERSE);
        if (ppU > MAXIMAL_PIXELS_PER_UNIVERSE) {
            LOG.log(Level.WARNING, "Invalid configuration found, "
                    + ConfigConstant.E131_PIXELS_PER_UNIVERSE + "=" + ppU + ". Maximal value is "
                    + MAXIMAL_PIXELS_PER_UNIVERSE + ", I fixed that for you.");
            ppU = MAXIMAL_PIXELS_PER_UNIVERSE;
        }
        return ppU;
    }

    /**
     * get first arnet universe id
     *
     * @return
     */
    public int getArtNetStartUniverseId() {
        return parseInt(ConfigConstant.ARTNET_FIRST_UNIVERSE_ID, 0);
    }

    /**
     *
     * @return
     */
    public String getArtNetBroadcastAddr() {
        return config.getProperty(ConfigConstant.ARTNET_BROADCAST_ADDR, "");
    }



    /**
     * 
     * @return
     */
    public List<DeviceConfig> getArtNetDevice() {
        return artNetDevice;
    }


    /**
     * 
     * @return
     */
    public List<DeviceConfig> getE131Device() {
        return e131Device;
    }


    public int getRpiWs2801SpiSpeed() {
        return parseInt(ConfigConstant.RPI_WS2801_SPI_SPEED, 0);
    }

    /**
     * Parses the mini dmx devices x.
     * 
     * @return the int
     */
    public int parseOutputXResolution() {
        return parseInt(ConfigConstant.OUTPUT_DEVICE_RESOLUTION_X, ConfigDefault.DEFAULT_RESOLUTION);
    }

    /**
     *
     * @return A matrix of device configs [y][x].
     */
    public DeviceConfig[][] parseDeviceLayout() {
        final String value = config.getProperty(ConfigConstant.DEVICE_LAYOUT);
        final String[] rows = value.split(";");

        DeviceConfig[][] config = new DeviceConfig[rows.length][];

        for(int i = 0; i < rows.length; ++i) {
            final String row = rows[i];
            final String[] columns = row.split(",");
            config[i] = new DeviceConfig[columns.length];
            for(int j = 0; j < columns.length; ++j) {
                config[i][j] = DeviceConfig.valueOf(columns[j].trim());
            }
        }

        //check if all rows have the same length
        final int row0Length = config[0].length;
        for(DeviceConfig[] row : config) {
            if(row.length != row0Length)
                throw new IllegalStateException("e131.layout parsing failed. All rows should have equal length");
        }

        return config;
    }


    /**
     * Parses the mini dmx devices y.
     * 
     * @return the int
     */
    public int parseOutputYResolution() {
        return parseInt(ConfigConstant.OUTPUT_DEVICE_RESOLUTION_Y, ConfigDefault.DEFAULT_RESOLUTION);
    }

    /**
     * 
     * @return
     */
    public boolean isOutputSnakeCabeling() {
        return parseBoolean(ConfigConstant.OUTPUT_DEVICE_SNAKE_CABELING);
    }

    /**
     * baudrate of the minidmx device
     * 
     * @return the int
     */
    public int parseMiniDmxBaudRate() {
        return parseInt(ConfigConstant.MINIDMX_BAUDRATE);
    }

    /**
     * 
     * @return
     */
    public int parseTpm2BaudRate() {
        return parseInt(ConfigConstant.TPM2_BAUDRATE);
    }

    /**
     * 
     * @return
     */
    public String getTpm2Device() {
        return config.getProperty(ConfigConstant.TPM2_DEVICE);
    }

    /**
     * baudrate of the minidmx device
     * 
     * @return the int
     */
    public float parseFps() {
        return parseFloat(ConfigConstant.FPS, ConfigDefault.DEFAULT_FPS);
    }

    public float parseRemoteFps() {
        return parseFloat(ConfigConstant.REMOTE_CLIENT_FPS, ConfigDefault.DEFAULT_REMOTE_CLIENT_FPS);
    }

    public boolean parseRemoteConnectionUseCompression() {
        return parseBoolean(ConfigConstant.REMOTE_CLIENT_USE_COMPRESSION);
    }

    /**
     * 
     * @return
     */
    public int loadPresetOnStart() {
        return parseInt(ConfigConstant.STARTUP_LOAD_PRESET_NR,
                ConfigDefault.DEFAULT_STARTUP_LOAD_PRESET_NR);
    }

    /**
     * Start randommode.
     * 
     * @return true, if successful
     */
    public boolean startRandommode() {
        return parseBoolean(ConfigConstant.STARTUP_IN_RANDOM_MODE);
    }

    public boolean startRandomPresetmode() {
        return parseBoolean(ConfigConstant.STARTUP_IN_RANDOM_PRESET_MODE);
    }

    /**
     * Gets the nr of screens.
     * 
     * @return the nr of screens
     */
    public int getNrOfScreens() {
        deviceConfig.length * deviceConfig[0].length
    }

    /**
     * Parses the mini dmx devices y.
     * 
     * @return the int
     */
    public int getNrOfAdditionalVisuals() {
        return parseInt(ConfigConstant.ADDITIONAL_VISUAL_SCREENS, 0);
    }

    /**
     * 
     * @return
     */
    public int getDebugWindowMaximalXSize() {
        return parseInt(ConfigConstant.DEBUG_WINDOW_MAX_X_SIZE,
                ConfigDefault.DEFAULT_GUI_WINDOW_MAX_X_SIZE);
    }

    /**
     * 
     * @return
     */
    public int getDebugWindowMaximalYSize() {
        return parseInt(ConfigConstant.DEBUG_WINDOW_MAX_Y_SIZE,
                ConfigDefault.DEFAULT_GUI_WINDOW_MAX_Y_SIZE);
    }

    /**
     * Gets the layout.
     * 
     * @return the layout
     */
    public Layout getLayout() {
        return new MatrixLayout()
        if (devicesInRow1 > 0 && devicesInRow2 == 0) {
            return new HorizontalLayout(devicesInRow1);
        }

        if (devicesInRow1 > 0 && devicesInRow2 > 0 && devicesInRow1 == devicesInRow2) {
            return new BoxLayout(devicesInRow1, devicesInRow2);
        }

        throw new IllegalStateException("Illegal device configuration detected!");
    }

    /**
     * Gets the i2c addr.
     * 
     * @return i2c address for rainbowduino devices
     */
    public List<Integer> getI2cAddr() {
        return i2cAddr;
    }

    /**
     * 
     * @return
     */
    public List<String> getRainbowduinoV3SerialDevices() {
        return this.rainbowduinoV3SerialDevices;
    }

    /**
     * Gets the lpd device.
     * 
     * @return options to display lpd6803 displays
     */
    public List<DeviceConfig> getLpdDevice() {
        return lpdDevice;
    }

    /**
     * Gets the tpm2net device.
     * 
     * @return options to display Stealth displays
     */
    public List<DeviceConfig> getTpm2NetDevice() {
        return tpm2netDevice;
    }

    /**
     * Gets the color format.
     * 
     * @return the color format
     */
    public List<ColorFormat> getColorFormat() {
        return colorFormat;
    }


    public List<Integer> getPanelOrder() {
        return panelOrder;
    }

    /**
     * Gets the output device.
     * 
     * @return the configured output device
     */
    public OutputDeviceEnum getOutputDevice() {
        return this.deviceType;
    }

    /**
     * Gets the device x resolution.
     * 
     * @return the device x resolution
     */
    public int getDeviceXResolution() {
        return deviceXResolution;
    }

    /**
     * Gets the device y resolution.
     * 
     * @return the device y resolution
     */
    public int getDeviceYResolution() {
        return deviceYResolution;
    }

    public int[] getOutputMappingValues() {
        String rawConfig = config.getProperty(ConfigConstant.OUTPUT_MAPPING);
        if (rawConfig == null) {
            return new int[0];
        }

        String[] tmp = rawConfig.split(",");
        if (tmp == null || tmp.length == 0) {
            return new int[0];
        }

        int ofs = 0;
        int[] ret = new int[tmp.length];
        for (String s : tmp) {
            try {
                ret[ofs] = Integer.decode(s.trim());
                ofs++;
            } catch (Exception e) {
                LOG.log(Level.WARNING, FAILED_TO_PARSE, s);
            }
        }
        return ret;
    }

    /**
     * @return the pixelInvadersBlacklist
     */
    public List<String> getPixelInvadersBlacklist() {
        return pixelInvadersBlacklist;
    }

    /**
     * get color adjust for one or multiple panels
     * 
     * @return
     */
    public Map<Integer, RGBAdjust> getPixelInvadersCorrectionMap() {
        return pixelInvadersCorrectionMap;
    }

    /**
     * get configured tpm2net ip.
     * 
     * @return the tpm2net ip
     */
    public String getTpm2NetIpAddress() {
        return config.getProperty(ConfigConstant.TPM2NET_IP);
    }

    /**
     * 
     * @return
     */
    public String getPixelinvadersNetIp() {
        return pixelinvadersNetIp;
    }

    /**
     * 
     * @return
     */
    public int getPixelinvadersNetPort() {
        return pixelinvadersNetPort;
    }

    /**
     * return user selected gamma correction
     * 
     * @return
     */
    public GammaType getGammaType() {
        return gammaType;
    }

    /**
     * return user selected startup output gain
     * 
     * @return
     */
    public int getStartupOutputGain() {
        return startupOutputGain;
    }

    /**
     * 
     * @return
     */
    public float getSoundSilenceThreshold() {
        String s = StringUtils.trim(config.getProperty(ConfigConstant.SOUND_SILENCE_THRESHOLD));
        if (StringUtils.isNotBlank(s)) {
            try {
                float f = Float.parseFloat(s);
                if (f >= 0.0f && f <= 1.0f) {
                    return f;
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, FAILED_TO_PARSE, s);
            }
        }
        return ConfigDefault.DEFAULT_SOUND_THRESHOLD;
    }

    /**
     * 
     * @return
     */
    public int getPresetLoadingFadeTime() {
        return parseInt(ConfigConstant.PRESET_LOADING_FADE_TIME,
                ConfigDefault.DEFAULT_PRESET_LOADING_FADE_TIME);
    }

    /**
     * 
     * @return
     */
    public int getVisualFadeTime() {
        return parseInt(ConfigConstant.VISUAL_FADE_TIME, ConfigDefault.DEFAULT_VISUAL_FADE_TIME);
    }

    public int getOscListeningPort() {
        return parseInt(ConfigConstant.NET_OSC_LISTENING_PORT,
                ConfigDefault.DEFAULT_NET_OSC_LISTENING_PORT);
    }

}
