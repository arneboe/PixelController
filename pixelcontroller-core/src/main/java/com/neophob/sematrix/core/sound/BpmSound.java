package com.neophob.sematrix.core.sound;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by arne on 10/4/15.
 */
public class BpmSound implements ISound {

    private int bpm;
    private long lastBeatTime;//in milliseconds
    private int timeBetweenBeats; //in milliseconds
    private int timeTillNextBeat;
    private float beatVolume = 0.5f;
    private float nonBeatVolume = 0;
    private boolean beatDetected = false;

    public BpmSound() {
        lastBeatTime = System.currentTimeMillis();
        timeBetweenBeats = 200;
        timeTillNextBeat = 200;
    }

    /**Call this method to update the beat state */
    public void update() {
        final long currentTime = System.currentTimeMillis();
        final int timePassed = (int)(currentTime - lastBeatTime);
        beatDetected = false;
        if(timePassed > timeTillNextBeat)
        {
            beatDetected = true;
            //this is done to avoid drift due to overshooting
            if(timePassed <= timeBetweenBeats)
            {
                timeTillNextBeat = timeBetweenBeats;
            }
            else
            {
                timeTillNextBeat = timeBetweenBeats - (timePassed - timeBetweenBeats);
            }
            lastBeatTime = currentTime;
        }
    }

    @Override
    public String getImplementationName() {
        return "BpmSound";
    }

    @Override
    public float getVolume() {
        if(beatDetected)
        {
            return beatVolume;
        }
        return nonBeatVolume;
    }

    @Override
    public float getVolumeNormalized() {
        return getVolume();
    }

    @Override
    public boolean isKick() {
        return beatDetected;
    }

    @Override
    public boolean isSnare() {
        return beatDetected;
    }

    @Override
    public boolean isHat() {
        return beatDetected;
    }

    @Override
    public boolean isPang() {
        return beatDetected;
    }

    @Override
    public boolean isBeat() {
        return beatDetected;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public int getFftAvg() {
        throw new NotImplementedException();
    }

    @Override
    public float getFftAvg(int i) {
        throw new NotImplementedException();
    }

    @Override
    public void start() {

    }

    @Override
    public void reset() {
        update();
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
        timeBetweenBeats = 60000 / bpm;
        timeTillNextBeat = timeBetweenBeats;
    }

    public void setNoBeatVolume(float noBeatVolume) {
        this.nonBeatVolume = noBeatVolume;
    }
}
