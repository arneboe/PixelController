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
package com.neophob.sematrix.core.api.impl;

import java.util.List;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.neophob.sematrix.core.api.CallbackMessageInterface;
import com.neophob.sematrix.core.glue.FileUtils;
import com.neophob.sematrix.core.glue.Shuffler;
import com.neophob.sematrix.core.glue.impl.FileUtilsLocalImpl;
import com.neophob.sematrix.core.jmx.PixelControllerStatus;
import com.neophob.sematrix.core.jmx.PixelControllerStatusMBean;
import com.neophob.sematrix.core.jmx.TimeMeasureItemGlobal;
import com.neophob.sematrix.core.listener.MessageProcessor;
import com.neophob.sematrix.core.osc.PixelControllerOscServer;
import com.neophob.sematrix.core.output.IOutput;
import com.neophob.sematrix.core.output.PixelControllerOutput;
import com.neophob.sematrix.core.preset.PresetFactory;
import com.neophob.sematrix.core.preset.PresetService;
import com.neophob.sematrix.core.preset.PresetServiceImpl;
import com.neophob.sematrix.core.preset.PresetSettings;
import com.neophob.sematrix.core.properties.Configuration;
import com.neophob.sematrix.core.properties.ValidCommand;
import com.neophob.sematrix.core.sound.ISound;
import com.neophob.sematrix.core.sound.SoundDummy;
import com.neophob.sematrix.core.sound.SoundMinim;
import com.neophob.sematrix.core.sound.SoundMinimKctess5;
import com.neophob.sematrix.core.visual.MatrixData;
import com.neophob.sematrix.core.visual.OutputMapping;
import com.neophob.sematrix.core.visual.VisualState;
import com.neophob.sematrix.core.visual.color.IColorSet;
import com.neophob.sematrix.mdns.server.PixMDnsServer;
import com.neophob.sematrix.mdns.server.impl.MDnsServerFactory;

/**
 * @author michu
 */
final class PixelControllerServerImpl extends PixelControllerServer implements Runnable {

    private static final Logger LOG = Logger.getLogger(PixelControllerServerImpl.class.getName());
    private static final long MAXIMAL_FRAME_DELAY_IN_MS = 250;

    private VisualState visualState;
    private PresetService presetService;

    private IOutput output;
    private ISound sound;

    private Configuration applicationConfig;
    private List<IColorSet> colorSets;
    private FileUtils fileUtils;
    private Framerate framerate;

    private Thread runner;
    private boolean initialized = false;
    private float lastFramerateValue = 1.0f;

    private PixelControllerOscServer oscServer;
    private PixelControllerStatusMBean pixConStat;
    private PixelControllerOutput pixelControllerOutput;

    /**
     * 
     * @param handler
     */
    public PixelControllerServerImpl(CallbackMessageInterface<String> handler) {
        super(handler);
        this.runner = new Thread(this);
        this.runner.setName("PixelController Core");
        this.runner.setDaemon(true);
    }

    @Override
    public void start() {
        LOG.log(Level.INFO, "Start PixelController v" + getVersion());
        this.runner.start();
    }

    @Override
    public void stop() {
        runner = null;
    }

    @Override
    public void run() {
        long cnt = 0;

        clientNotification("Load Configuration");
        fileUtils = new FileUtilsLocalImpl();
        applicationConfig = loadConfiguration(fileUtils.getDataDir());
        this.colorSets = loadColorPalettes(fileUtils.getDataDir());

        clientNotification("Create Collector");
        LOG.log(Level.INFO, "Create Collector");
        this.visualState = VisualState.getInstance();

        clientNotification("Initialize System");
        LOG.log(Level.INFO, "Initialize System");
        this.pixConStat = new PixelControllerStatus((int) applicationConfig.parseFps());
        this.initSound();

        List<PresetSettings> presetSettings = PresetFactory.loadPresetsFile(fileUtils.getDataDir());
        this.presetService = new PresetServiceImpl(presetSettings);
        MessageProcessor.INSTANCE.init(presetService, fileUtils);

        this.visualState.init(fileUtils, applicationConfig, sound, colorSets, presetService);
        framerate = new Framerate(applicationConfig.parseFps());

        clientNotification("Initialize OSC Server");
        LOG.log(Level.INFO, "Initialize OSC Server");

        int listeningOscPort = applicationConfig.getOscListeningPort();
        try {
            if (listeningOscPort > 0) {
                oscServer = new PixelControllerOscServer(this, listeningOscPort);
                oscServer.startServer();
                // register osc server in the statistic class
                this.pixConStat.setOscServerStatistics(oscServer);
            } else {
                LOG.log(Level.INFO, "OSC Server disabled, port: " + listeningOscPort);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "failed to start OSC Server", e);
        }

        try {
            if (listeningOscPort > 0) {
                PixMDnsServer bonjour = MDnsServerFactory.createServer(listeningOscPort, ZEROCONF_NAME);
                bonjour.startServerAsync();
            } else {
                LOG.log(Level.INFO, "MDNS Server disabled, OSC port: " + listeningOscPort);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "failed to start MDns Server", e);
        }

        clientNotification("Initialize Output device");
        LOG.log(Level.INFO, "Initialize Output device");
        this.output = PixelControllerOutput.getOutputDevice(this.visualState.getMatrix(),
                this.visualState.getPixelControllerResize(), applicationConfig);
        if (this.output == null) {
            throw new IllegalArgumentException("No output device found!");
        }
        pixelControllerOutput = new PixelControllerOutput(pixConStat);
        pixelControllerOutput.initAll();
        pixelControllerOutput.addOutput(this.output);

        this.setupInitialConfig();

        LOG.log(Level.INFO, "--- PixelController Setup END ---");
        LOG.log(Level.INFO, "---------------------------------");
        LOG.log(Level.INFO, "");

        initialized = true;

        LOG.log(Level.INFO, "Enter main loop");

        int c = 0;
        // Mainloop
        while (Thread.currentThread() == runner) {
            if (this.visualState.isInPauseMode()) {
                // no update here, we're in pause mode
                continue;
            }

            try {
                if(this.sound.isPang())
                {
                    System.out.println("gui " + c);
                    ++c;
                }
                this.visualState.updateSystem(pixConStat);
                this.sound.reset(); //reset sound after updating all sound aware modules
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "VisualState.getInstance().updateSystem() failed!", e);
            }

            long l = System.currentTimeMillis();

            // do not update output if a preset is loaded
            if (!visualState.isLoadingPresent()) {
                pixelControllerOutput.update();
                pixConStat.trackTime(TimeMeasureItemGlobal.OUTPUT_SCHEDULE,
                        System.currentTimeMillis() - l);

                pixConStat.setCurrentFps(framerate.getFps());
                pixConStat.setFrameCount(cnt++);

                if (cnt % 5000 == 0) {
                    LOG.log(Level.INFO, "Framerate: {0}, Framecount: {1}",
                            new Object[] { framerate.getFps(), cnt });
                }
            }
            handleFpsSleep();
        }
        LOG.log(Level.INFO, "Main loop finished...");
    }

    private void handleFpsSleep() {
        if (lastFramerateValue != visualState.getFpsSpeed()) {
            lastFramerateValue = visualState.getFpsSpeed();
            framerate.setFps(visualState.getFpsSpeed() * applicationConfig.parseFps());
        }

        long delay = framerate.getFrameDelay();
        while (delay > MAXIMAL_FRAME_DELAY_IN_MS) {
            sleep(MAXIMAL_FRAME_DELAY_IN_MS);
            delay -= MAXIMAL_FRAME_DELAY_IN_MS;
            // prevent that very low framerate (<1) result in a non
            // responsiveness
            if (lastFramerateValue != visualState.getFpsSpeed()) {
                lastFramerateValue = visualState.getFpsSpeed();
                framerate.setFps(visualState.getFpsSpeed() * applicationConfig.parseFps());
                delay = 1;
            }
        }
        sleep(delay);
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public PresetService getPresetService() {
        return presetService;
    }

    private void setupInitialConfig() {
        // start in random mode?
        if (applicationConfig.startRandommode()) {
            LOG.log(Level.INFO, "Random Mode enabled");
            Shuffler.manualShuffleStuff(visualState);
            visualState.setRandomMode(true);
        }

        if (applicationConfig.startRandomPresetmode()) {
            LOG.log(Level.INFO, "Random Preset Mode enabled");
            visualState.setRandomPresetMode(true);
        }

        // load saves presets
        int presetNr = applicationConfig.loadPresetOnStart();
        if (presetNr < 0 || presetNr >= PresetServiceImpl.NR_OF_PRESET_SLOTS) {
            presetNr = 0;
        }
        LOG.log(Level.INFO, "Load preset " + presetNr);
        presetService.setSelectedPreset(presetNr);
        MessageProcessor.INSTANCE.processMsg(new String[] { ValidCommand.LOAD_PRESET.toString() },
                false, null);
        visualState.notifyGuiUpdate();
    }

    private void initSound() {
        // choose sound implementation
        if (applicationConfig.isAudioAware()) {
            try {
                sound = new SoundMinimKctess5();
                sound.start();
                return;
            } catch (Exception e) {
                LOG.log(Level.WARNING, "FAILED TO INITIALIZE SOUND INSTANCE. Disable sound input.",
                        e);
            } catch (Error e) {
                LOG.log(Level.WARNING,
                        "FAILED TO INITIALIZE SOUND INSTANCE (Error). Disable sound input.", e);
            }
        }

        LOG.log(Level.INFO, "Initialize dummy sound.");
        sound = new SoundDummy();
    }

    @Override
    public String getVersion() {
        String version = this.getClass().getPackage().getImplementationVersion();
        if (version != null && !version.isEmpty()) {
            return "v" + version;
        }
        return "Developer Snapshot";
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public Configuration getConfig() {
        return applicationConfig;
    }

    @Override
    public IOutput getOutput() {
        return output;
    }

    @Override
    public float getFps() {
        return framerate.getFps();
    }

    @Override
    public PixelControllerStatusMBean getPixConStat() {
        return pixConStat;
    }

    @Override
    public MatrixData getMatrix() {
        // TODO inject matrix to visual state!
        return visualState.getMatrix();
    }

    @Override
    public VisualState getVisualState() {
        return visualState;
    }

    @Override
    public ISound getSoundImplementation() {
        return this.sound;
    }

    @Override
    public long getProcessedFrames() {
        return this.framerate.getFrameCount();
    }

    @Override
    public void refreshGuiState() {
        VisualState.getInstance().notifyGuiUpdate();
    }

    @Override
    public void observeVisualState(Observer o) {
        VisualState.getInstance().addObserver(o);
    }

    @Override
    public void stopObserveVisualState(Observer o) {
        VisualState.getInstance().deleteObserver(o);
    }

    @Override
    public List<IColorSet> getColorSets() {
        return colorSets;
    }

    @Override
    public List<OutputMapping> getAllOutputMappings() {
        return VisualState.getInstance().getAllOutputMappings();
    }

    @Override
    public List<String> getGuiState() {
        return VisualState.getInstance().getGuiState();
    }

    @Override
    public FileUtils getFileUtils() {
        return fileUtils;
    }

}
