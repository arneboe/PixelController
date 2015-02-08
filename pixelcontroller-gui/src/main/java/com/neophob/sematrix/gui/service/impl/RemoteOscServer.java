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
package com.neophob.sematrix.gui.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.neophob.sematrix.core.api.CallbackMessageInterface;
import com.neophob.sematrix.core.glue.FileUtils;
import com.neophob.sematrix.core.glue.impl.FileUtilsRemoteImpl;
import com.neophob.sematrix.core.jmx.PixelControllerStatusMBean;
import com.neophob.sematrix.core.osc.PixelControllerOscServer;
import com.neophob.sematrix.core.osc.remotemodel.ImageBuffer;
import com.neophob.sematrix.core.output.IOutput;
import com.neophob.sematrix.core.preset.PresetSettings;
import com.neophob.sematrix.core.properties.Configuration;
import com.neophob.sematrix.core.properties.Command;
import com.neophob.sematrix.core.properties.ValidCommand;
import com.neophob.sematrix.core.rmi.RmiApi;
import com.neophob.sematrix.core.rmi.RmiApi.Protocol;
import com.neophob.sematrix.core.rmi.impl.RmiFactory;
import com.neophob.sematrix.core.sound.ISound;
import com.neophob.sematrix.core.sound.SoundDummy;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.OutputMapping;
import com.neophob.sematrix.core.visual.color.IColorSet;
import com.neophob.sematrix.gui.service.PixConServer;
import com.neophob.sematrix.osc.client.OscClientException;
import com.neophob.sematrix.osc.model.OscMessage;
import com.neophob.sematrix.osc.server.OscMessageHandler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * communicate service with remote pixelcontroller instance send and recieve
 * data via osc
 * 
 * @author michu
 * 
 */
public class RemoteOscServer extends OscMessageHandler implements PixConServer, Runnable {

    private static final Logger LOG = Logger.getLogger(RemoteOscServer.class.getName());
    private static final String CLIENT_CONFIG_FILENAME = "clientRemote.properties";

    private static final Protocol SERVER_PROTOCOL = Protocol.TCP;
    private static final Protocol CLIENT_PROTOCOL = Protocol.UDP;

    private float steps;

    private Set<ValidCommand> recievedMessages;
    private RemoteOscObservable remoteObserver;
    private CallbackMessageInterface<String> setupFeedback;

    private boolean initialized;
    private String version;
    private Configuration config;
    private MatrixData matrix;
    private List<IColorSet> colorSets;
    private ISound sound;
    private List<OutputMapping> outputMapping;
    private IOutput output;
    private ImageBuffer imageBuffer;
    private PresetSettings presetSettings;
    private FileUtils fileUtilsRemote;
    private PixelControllerStatusMBean jmxStatistics;

    private RmiApi remoteServer;

    public RemoteOscServer(CallbackMessageInterface<String> msgHandler) {
        this.setupFeedback = msgHandler;
        this.remoteServer = RmiFactory.getRmiApi(true,
                PixelControllerOscServer.REPLY_PACKET_BUFFERSIZE);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (initialized) {
                    LOG.log(Level.INFO, "Shutdown: Unregister Observer");
                    try {
                        remoteServer.sendPayload(
                                new Command(ValidCommand.UNREGISTER_VISUALOBSERVER), null);
                    } catch (OscClientException e) {
                        // ignored
                    }
                    LOG.log(Level.INFO, "Shutdown: Stop OSC Server");
                    remoteServer.shutdown();
                }
            }
        });
    }

    @Override
    public void start() {
        this.sound = new SoundDummy();
        this.steps = 1 / 12f;

        Thread startThread = new Thread(this);
        startThread.setName("GUI Poller");
        startThread.setDaemon(true);
        startThread.start();
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Configuration getConfig() {
        return config;
    }

    @Override
    public List<IColorSet> getColorSets() {
        return colorSets;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public int[] getOutputBuffer(int nr) {
        return imageBuffer.getOutputBuffer()[nr];
    }

    @Override
    public IOutput getOutput() {
        return output;
    }

    @Override
    public List<OutputMapping> getAllOutputMappings() {
        return outputMapping;
    }

    @Override
    public float getCurrentFps() {
        return jmxStatistics.getCurrentFps();
    }

    @Override
    public long getFrameCount() {
        return jmxStatistics.getFrameCount();
    }

    @Override
    public long getServerStartTime() {
        return jmxStatistics.getStartTime();
    }

    @Override
    public long getRecievedOscPackets() {
        return jmxStatistics.getRecievedOscPakets();
    }

    @Override
    public long getRecievedOscBytes() {
        return jmxStatistics.getRecievedOscBytes();
    }

    @Override
    public ISound getSoundImplementation() {
        return sound;
    }

    @Override
    public MatrixData getMatrixData() {
        return matrix;
    }

    @Override
    public int getNrOfVisuals() {
        return this.config.getNrOfScreens() + 1 + this.config.getNrOfAdditionalVisuals();
    }

    @Override
    public PresetSettings getCurrentPresetSettings() {
        return presetSettings;
    }

    @Override
    public List<PresetSettings> getAllPresetSettings() {
        throw new NotImplementedException();
    }

    @Override
    public void updateNeededTimeForMatrixEmulator(long t) {
        // ignored, as not relevant
    }

    @Override
    public void updateNeededTimeForInternalWindow(long t) {
        // ignored, as not relevant
    }

    @Override
    public void sendMessage(String[] msg) {
        try {
            remoteServer.sendPayload(new Command(msg), null);
        } catch (OscClientException e) {
            LOG.log(Level.WARNING, "Failed to parse Message!", e);
        }
    }

    @Override
    public void refreshGuiState() {
        // ignored, the gui is refreshed async
    }

    @Override
    public void observeVisualState(Observer o) {
        remoteObserver.addObserver(o);
    }

    @Override
    public int[] getVisualBuffer(int nr) {
        return imageBuffer.getVisualBuffer()[nr];
    }

    @Override
    public FileUtils getFileUtils() {
        return fileUtilsRemote;
    }

    /**
     * Incoming OSC Commands
     */
    @Override
    public void handleOscMessage(OscMessage oscIn) {
        if (StringUtils.isBlank(oscIn.getPattern())) {
            LOG.log(Level.INFO, "Ignore empty OSC message...");
            return;
        }

        String pattern = oscIn.getPattern();
        ValidCommand command;
        try {
            command = ValidCommand.valueOf(pattern);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unknown message: " + pattern, e);
            return;
        }

        recievedMessages.add(command);
        processMessage(command, oscIn.getBlob());
    }

    /**
     * reassemble object
     * 
     * @param command
     * @param data
     */
    private void processMessage(ValidCommand command, byte[] data) {
        try {
            switch (command) {
                case GET_VERSION:
                    this.version = remoteServer.reassembleObject(data, String.class);
                    setupFeedback.handleMessage("Found PixelController Version " + this.version);
                    break;

                case GET_CONFIGURATION:
                    config = remoteServer.reassembleObject(data,
                            Configuration.class);
                    setupFeedback.handleMessage("Recieved Configuration");
                    break;

                case GET_MATRIXDATA:
                    matrix = remoteServer.reassembleObject(data, MatrixData.class);
                    setupFeedback.handleMessage("Recieved Matrixdata");
                    break;

                case GET_COLORSETS:
                    colorSets = remoteServer.reassembleObject(data, ArrayList.class);
                    setupFeedback.handleMessage("Recieved Colorsets");
                    break;

                case GET_OUTPUTMAPPING:
                    outputMapping = remoteServer.reassembleObject(data, ArrayList.class);
                    setupFeedback.handleMessage("Recieved Output mapping");
                    break;

                case GET_OUTPUT:
                    output = remoteServer.reassembleObject(data, IOutput.class);
                    break;

                case GET_GUISTATE:
                    remoteObserver.notifyGuiUpdate(remoteServer.reassembleObject(data,
                            ArrayList.class));
                    break;

                case GET_PRESETSETTINGS:
                    presetSettings = remoteServer.reassembleObject(data, PresetSettings.class);
                    break;

                case GET_JMXSTATISTICS:
                    jmxStatistics = remoteServer.reassembleObject(data,
                            PixelControllerStatusMBean.class);
                    break;

                case GET_FILELOCATION:
                    fileUtilsRemote = remoteServer
                            .reassembleObject(data, FileUtilsRemoteImpl.class);
                    break;

                case GET_IMAGEBUFFER:
                    imageBuffer = remoteServer.reassembleObject(data, ImageBuffer.class);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to convert input data!", e);
            setupFeedback.handleMessage("Error converting " + command);
        }
    }

    @Override
    public void run() {
        LOG.log(Level.INFO, "Start PixelController Client thread");
        try {
            setupFeedback.handleMessage("Detect PixelController OSC Server");
            Properties p = loadLocalProperties();
            RemoteClientName remoteHost = new RemoteClientName(setupFeedback, p);
            remoteHost.queryRemoteName();
            LOG.log(Level.INFO, "Remote target, IP: " + remoteHost.getTargetHost() + ", port: "
                    + remoteHost.getTargetPort());
            setupFeedback.handleMessage(" ... use " + remoteHost.getTargetHost());

            setupFeedback.handleMessage("Start OSC Server");
            // create a tcp server, expected large messages (> then MTU)
            remoteServer.startServer(SERVER_PROTOCOL, this, remoteHost.getTargetPort() - 1);
            setupFeedback.handleMessage(" ... started");

            setupFeedback.handleMessage("Connect to PixelController OSC Server");
            // use udp to communicate with the pixelcontroller OSC server
            int clientPort = remoteHost.getTargetPort() - 1;
            remoteServer.startClient(CLIENT_PROTOCOL, remoteHost.getTargetHost(),
                    remoteHost.getTargetPort(), clientPort);
            setupFeedback.handleMessage(" ... done");

            this.remoteObserver = new RemoteOscObservable();
            this.initialized = false;
            this.recievedMessages = new HashSet<ValidCommand>();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to start Remote OSC Server!", e);
            return;
        }

        // first step, get static values
        Set<ValidCommand> initCommands = new HashSet<ValidCommand>();
        initCommands.add(ValidCommand.GET_VERSION);
        initCommands.add(ValidCommand.GET_CONFIGURATION);
        initCommands.add(ValidCommand.GET_MATRIXDATA);
        initCommands.add(ValidCommand.GET_COLORSETS);
        initCommands.add(ValidCommand.GET_OUTPUT);
        initCommands.add(ValidCommand.GET_GUISTATE);
        initCommands.add(ValidCommand.GET_OUTPUTMAPPING);
        initCommands.add(ValidCommand.GET_PRESETSETTINGS);
        initCommands.add(ValidCommand.GET_JMXSTATISTICS);
        initCommands.add(ValidCommand.GET_FILELOCATION);
        initCommands.add(ValidCommand.GET_IMAGEBUFFER);

        int waitLoop = 0;
        while (!recievedMessages.containsAll(initCommands)) {
            for (ValidCommand cmd : initCommands) {
                if (!recievedMessages.contains(cmd)) {
                    LOG.log(Level.INFO, "Request " + cmd + " from OSC Server");
                    try {
                        remoteServer.sendPayload(new Command(cmd), null);
                    } catch (OscClientException e) {
                        LOG.log(Level.SEVERE, "failed to send osc message, " + cmd, e);
                    }
                }
            }
            try {
                waitLoop++;
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // ignored
            }

            if (waitLoop > 4) {
                LOG.log(Level.SEVERE, "Failed to get answer from PixelController Server!");
                setupFeedback.handleMessage("");
                setupFeedback.handleMessage("ERROR: No answer from PixelController received!");
                setupFeedback
                        .handleMessage("Start aborted, make sure PixelController is running and restart client");
                return;
            }
        }
        initialized = true;
        // now register remote observer
        try {
            remoteServer.sendPayload(new Command(ValidCommand.REGISTER_VISUALOBSERVER), null);
        } catch (OscClientException e) {
            LOG.log(Level.SEVERE, "failed to send osc message, "
                    + ValidCommand.REGISTER_VISUALOBSERVER, e);
        }
    }

    @Override
    public float getSetupSteps() {
        return steps;
    }

    private Properties loadLocalProperties() {
        Properties config = new Properties();
        InputStream is = null;

        String fileToLoad = System.getProperty("user.dir") + File.separator
                + CLIENT_CONFIG_FILENAME;
        try {
            is = new FileInputStream(fileToLoad);
            config.load(is);
            LOG.log(Level.INFO, "Config loaded, {0} entries", config.size());
            return config;
        } catch (Exception e) {
            String error = "Failed to open the configfile " + fileToLoad;
            LOG.log(Level.SEVERE, error, e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                // ignored
            }
        }
        return null;
    }

}
