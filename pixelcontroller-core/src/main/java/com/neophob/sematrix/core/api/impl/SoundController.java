package com.neophob.sematrix.core.api.impl;

import com.neophob.sematrix.core.sound.SoundCombiner;

/**
 * Created by arne on 10.10.2015.
 */
public interface SoundController
{
    void setSoundMode(SoundCombiner.SoundMode mode);
    /**Set the bpm if the current mode supports it */
    void setBpm(final int bpm);

    void setNoBeatSpeed(float noBeatSpeed);
}
