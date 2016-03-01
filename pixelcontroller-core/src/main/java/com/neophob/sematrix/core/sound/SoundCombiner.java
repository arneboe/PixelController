package com.neophob.sematrix.core.sound;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by arne on 10.10.2015.
 */
public class SoundCombiner implements ISound
{
    private SoundMinimKctess5 audioSound = new SoundMinimKctess5();
    private SoundMinim minimSound = new SoundMinim(0.0005f);

    private ISound currentSound;

    public SoundCombiner()
    {
        currentSound = audioSound;
    }

    public enum SoundMode
    {
        AUDIO(0),
        AUDIO2(1);

        private int id;

        SoundMode(int id) {
            this.id = id;
        }
        public String guiText()
        {
            return WordUtils.capitalizeFully(StringUtils.replace(this.name(), "_", " "));
        }

        public int getId()
        {
            return id;
        }
    }
    public void selectSound(SoundMode source)
    {
        switch(source)
        {
            case AUDIO:
                currentSound = audioSound;
                break;
            case AUDIO2:
                currentSound = minimSound;
                break;
            default:
                throw new NotImplementedException();
        }
    }


    @Override
    public String getImplementationName() {
        return currentSound.getImplementationName();
    }

    @Override
    public float getVolume() {
        return currentSound.getVolume();
    }

    @Override
    public float getVolumeNormalized() {
        return currentSound.getVolumeNormalized();
    }

    @Override
    public boolean isKick() {
        return currentSound.isKick();
    }

    @Override
    public boolean isSnare() {
        return currentSound.isSnare();
    }

    @Override
    public boolean isHat() {
        return currentSound.isHat();
    }

    @Override
    public boolean isPang() {
        return currentSound.isPang();
    }

    @Override
    public boolean isBeat() {
        return currentSound.isBeat();
    }

    @Override
    public void shutdown() {
        audioSound.shutdown();
        minimSound.shutdown();
    }

    @Override
    public int getFftAvg() {
        return currentSound.getFftAvg();
    }

    @Override
    public float getFftAvg(int i) {
        return currentSound.getFftAvg(i);
    }

    @Override
    public void start() {
        minimSound.start();
        audioSound.start();
    }

    @Override
    public void reset() {
        minimSound.reset();
        audioSound.reset();
    }
}
