package com.neophob.sematrix.core.sound;
import ddf.minim.signals.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.effects.*;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a port of the beat detection algorithm by kctess5.
 * @see https://github.com/kctess5/Processing-Beat-Detection
 *
 * This thread runs at 60 fps doing a fft each frame.
 * I am not sure if the 60 fps are neccessary but it is a port of processing code
 * with lots of manual tuned constants and processing runs at 60 fps. Therfore it is
 * reasonable to assume that it will work best at 60 fps.
 */
public class SoundMinimKctess5 extends TimerTask implements ISound {

    private static final transient Logger LOG = Logger.getLogger(SoundMinim.class.getName());
    private AudioInput in;
    private FFT fft;
    private int k;

    private final int longTermAverageSamples = 60;    //gets average volume over a period of time
    private final int shortTermAverageSamples = 1;    //average volume over a shorter "instantanious" time
    private final int deltaArraySamples = 300;        //number of energy deltas between long & short average to sum together
    private final int beatAverageSamples = 100;
    private final int beatCounterArraySamples = 400;
    private final int maxTime = 200;
    private final float predictiveInfluenceConstant = .1f;
    private int cyclePerBeatIntensity;

    private final int beatBands = 30;                  //Number of bands to monitor, higher for more accuracy, lower for speed
    private final float lowFreqCutoff = 30;
    private final double TWO_PI = Math.PI * 2.0;

    private float[][] deltaArray = new float[deltaArraySamples][beatBands];
    private float[][] shortAverageArray = new float[shortTermAverageSamples][beatBands];
    private float[][] longAverageArray = new float[longTermAverageSamples/shortTermAverageSamples][beatBands];
    private float[] globalAverageArray = new float[longTermAverageSamples];
    private int[] beatCounterArray = new int[beatCounterArraySamples];
    private int[] beatSpread = new int[maxTime];
    private int beatCounterPosition = 0;
    private int cyclesPerBeat;

    private int longPosition = 0;
    private int shortPosition = 0;
    private int deltaPosition = 0;

    private int[] count = new int[beatBands];
    private float[] totalLong = new float[beatBands];
    private float[] totalShort = new float[beatBands];
    private float[] delta = new float[beatBands];
    private float[] c = new float[beatBands];             //multiplier used to determain threshold

    private int beatCounter = 0;
    private float[] beatAverage = new float[beatAverageSamples];
    private float totalBeat = 0;
    private int beatPosition = 0;

    private float standardDeviation;
    private boolean running = true;

    private Timer timer = new Timer();
    private final int fps = 60;
    /**True if a beat has been detected since the last call to reset() */
    private AtomicBoolean beatDetected;


    public SoundMinimKctess5(){
        for (int i = 0; i < beatBands; i += 1) {
            count[i] = 0;
            totalLong[i] = 0;
            totalShort[i] = 0;
            delta[i] = 0;
            c[i] = 1.5f;
        }
        beatDetected = new AtomicBoolean(false);
        Minim minim = new Minim(this);

        //in = minim.getLineIn(Minim.STEREO, 1024);
        in = minim.getLineIn(Minim.STEREO, 2048);                     //Gets values from mic (and soundcard?)
        fft = new FFT(in.bufferSize(), in.sampleRate());              //Sets up the FFT
        fft.logAverages(30, 5);                                       //Creates a 5 band/oct FFT starting at 40Hz
    }

    /**Minim requires this method to be present when using it independently of processing */
    public String sketchPath(String fileName) {
        LOG.log(Level.INFO, "Not implemented, not used, sketchPath: " + fileName);
        return "";
    }

    /**Minim requires this method to be present when using it independently of processing */
    public InputStream createInput(String fileName) {
        LOG.log(Level.INFO, "Not implemented, not used, createInput: " + fileName);
        return null;
    }

    @Override
    public String getImplementationName() {
        return "SoundMinimKctess5";
    }

    @Override
    public float getVolume() {
        return 0;
    }

    @Override
    public float getVolumeNormalized() {
        return 0;
    }

    @Override
    public boolean isKick() {
        return isPang();
    }

    @Override
    public boolean isSnare() {
        return isPang();
    }

    @Override
    public boolean isHat() {
        return isPang();
    }

    @Override
    public boolean isPang() {
        return beatDetected.get();
    }

    @Override
    public void shutdown() {
        timer.cancel();
    }

    @Override
    public int getFftAvg() {
        return 0;
    }

    @Override
    public float getFftAvg(int i) {
        return fft.getAvg(i);
    }
    
    public void run() {
        if(beatDetect()) {
            beatDetected.set(true);
        }
    }

    @Override
    public void start() {
        timer.scheduleAtFixedRate(this, 0, 1000/fps);
    }

    @Override
    public void reset() {
        beatDetected.set(false);
    }

    /**This is the main beat detection loop extracted from processing */
    public boolean beatDetect() {
        if (shortPosition >= shortTermAverageSamples) shortPosition = 0;    //Resets incremental variables
        if (longPosition >= longTermAverageSamples/shortTermAverageSamples) longPosition = 0;
        if (deltaPosition >= deltaArraySamples) deltaPosition = 0;
        if (beatPosition >= beatAverageSamples) beatPosition = 0;

        fft.forward(in.mix);                                          //Performs the FFT

        /////////////////////////////////////Calculate short and long term array averages///////////////////////////////////////////////////////////////////////////////////////////////////////////

        for (int i = 0; i <beatBands; i += 1) {
            shortAverageArray[shortPosition][i] = fft.getBand(i);   //stores the average intensity between the freq. bounds to the short term array
            totalLong[i] = 0;
            totalShort[i] = 0;

            for (int j = 0; j < longTermAverageSamples/shortTermAverageSamples; j += 1) totalLong[i]+= longAverageArray[j][i];  //adds up all the values in both of these arrays, for averaging
            for (int j = 0; j < shortTermAverageSamples; j +=1) totalShort[i] += shortAverageArray[j][i];
        }

        ///////////////////////////////////////////Find wideband frequency average intensity/////////////////////////////////////////////////////////////////////////////////////////////////////

        float totalGlobal = 0;
        globalAverageArray[longPosition] = fft.calcAvg(30, 2000);
        for (int j = 0; j < longTermAverageSamples; j +=1) totalGlobal += globalAverageArray[j];
        totalGlobal = totalGlobal /longTermAverageSamples;

        //////////////////////////////////Populate long term average array//////////////////////////////////////////////////////////////////////////////////////////////////////////////

        if (shortPosition%shortTermAverageSamples == 0) {   //every time the short array is completely new it is added to long array
            System.arraycopy(totalShort, 0, longAverageArray[longPosition], 0, beatBands);
            longPosition += 1;
        }

        /////////////////////////////////////////Find index of variation for each band///////////////////////////////////////////////////////////////////////////////////////////////////////

        for (int i = 0; i < beatBands; i += 1) {
            totalLong[i] = totalLong[i]/(longTermAverageSamples/shortTermAverageSamples);

            delta[i] = 0;
            deltaArray[deltaPosition][i] = (float)Math.pow(Math.abs(totalLong[i] - totalShort[i]), 2);
            for (int j = 0; j < deltaArraySamples; j += 1) delta[i] += deltaArray[j][i];
            delta[i] = delta[i]/deltaArraySamples;


            ///////////////////////////////////////////Find local beats/////////////////////////////////////////////////////////////////////////////////////////////////////

            c[i] = 1.3f + constrain(map(delta[i], 0, 3000, 0, 0.4f), 0, .4f) + //delta is usually bellow 2000
                    map(constrain((float)Math.pow(totalLong[i], 0.5f), 0, 6), 0, 20, .3f, 0) +    //possibly comment this out, adds weight to the lower end
                    map(constrain(count[i], 0, 15), 0, 15, 1, 0) -
                    map(constrain(count[i], 30, 200), 30, 200, 0, 0.75f);


            if (cyclePerBeatIntensity/standardDeviation > 3.5){
                float predictiveInfluence = predictiveInfluenceConstant * (1 - (float) Math.cos((((double) beatCounter) * TWO_PI) / ((double) cyclesPerBeat)));
                predictiveInfluence *= map(constrain(cyclePerBeatIntensity/standardDeviation, 3.5f, 20), 3.5f, 15, 1, 6);
                if (cyclesPerBeat > 10) c[i] = c[i] + predictiveInfluence;
            }
        }

        int beat = 0;
        for (int i = 0; i < beatBands; i += 1) {
            if (totalShort[i] > totalLong[i]*c[i] & count[i] > 7) {                  //If beat is detected

                if (count[i] > 12 & count[i] < 200) {
                    beatCounterArray[beatCounterPosition%beatCounterArraySamples] = count[i];
                    beatCounterPosition +=1;
                }
                count[i] = 0;                                                 //resets counter
            }
        }

        /////////////////////////////////////////Figure out # of beats, and average///////////////////////////////////////////////////////////////////////////////////////////////////////

        for (int i = 0; i < beatBands; i +=1) if (count[i] < 2) beat += 1;   //If there has been a recent beat in a band add to the global beat value

        beatAverage[beatPosition] = beat;
        for (int j = 0; j < beatAverageSamples; j +=1) totalBeat += beatAverage[j];
        totalBeat = totalBeat/beatAverageSamples;

        /////////////////////////////////////////////////find global beat///////////////////////////////////////////////////////////////////////////////////////////////
        c[0] = 3.25f + map(constrain(beatCounter, 0, 5), 0, 5, 5, 0);
        if (cyclesPerBeat > 10) c[0] = c[0] + 0.75f*(1 - (float)Math.cos((beatCounter * TWO_PI)/((double)cyclesPerBeat)));

        final float threshold = constrain(c[0] * totalBeat + map(constrain(totalGlobal, 0, 2), 0, 2, 4, 0), 5, 1000);

        boolean detected = false;
        if (beat > threshold & beatCounter > 5) {
            detected = true;
            beatCounter = 0;
        }
        /////////////////////////////////////////////////////Calculate beat spreads///////////////////////////////////////////////////////////////////////////////////////////

        for (int i = 0; i < maxTime; i++) beatSpread[i] = 0;
        for (int i = 0; i < beatCounterArraySamples; i++) {
            beatSpread[beatCounterArray[i]] +=1;
        }

        cyclesPerBeat = mode(beatCounterArray);
        if (cyclesPerBeat < 20) cyclesPerBeat *= 2;

        cyclePerBeatIntensity = max(beatSpread);

        standardDeviation = 0;
        for (int i = 0; i < maxTime; i++) standardDeviation += Math.pow(beatCounterArraySamples/maxTime-beatSpread[i], 2);
        standardDeviation = (float)Math.pow(standardDeviation/maxTime, .5);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        shortPosition += 1;
        deltaPosition += 1;
        for (int i = 0; i < beatBands; i += 1) count[i] += 1;
        beatCounter += 1;
        beatPosition += 1;

        return detected;
    }

    /** PApplet.constraint() copied from processing
     * @see http://github.com/processing/processing/blob/master/core/src/processing/core/PApplet.java */
    static public float constrain(float amt, float low, float high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    /**PApplet.map() copied from processing
     * @see http://github.com/processing/processing/blob/master/core/src/processing/core/PApplet.java */
    static public float map(float value,
                                  float start1, float stop1,
                                  float start2, float stop2) {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }

    int mode(int[] array) {
        int[] modeMap = new int [array.length];
        int maxEl = array[0];
        int maxCount = 1;

        for (int el : array) {
            if (modeMap[el] == 0) {
                modeMap[el] = 1;
            } else {
                modeMap[el]++;
            }

            if (modeMap[el] > maxCount) {
                maxEl = el;
                maxCount = modeMap[el];
            }
        }
        return maxEl;
    }

    public static int max(int[] values) {
        int max = Integer.MIN_VALUE;
        for(int value : values) {
            if(value > max)
                max = value;
        }
        return max;
    }

}
