
package com.anshuman.spkdet.mistral;

import biz.ubona.dsp.signal.ActivityDetector;
import biz.ubona.dsp.signal.EnvelopeDetector;
import biz.ubona.dsp.sound.AudioIo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class UVoiceActivityDetector {

    private static final Logger logger = LoggerFactory.getLogger(UVoiceActivityDetector.class);
    public static final double thresholdLevel = 0.005F;
    private static final float minActivityTime = 0.25F;
    private static final float minSilenceTime = 0.20F;
    private static final float leadTime = 0.05F;
    private static final float trailTime = 0.05F;



    public double[] removeSilence(double[] voiceSample, double sampleRate) {
        AudioIo.AudioSignal signal = new AudioIo.AudioSignal();
        signal.samplingRate=(int)sampleRate;
        signal.data=new double[1][];
        signal.data[0]=voiceSample;
        int[] zones = findSoundZones(signal);
        if (zones.length == 0) {
            return voiceSample;
        }
        ArrayList<Double> doubles = new ArrayList<Double>();
        for (int zoneNo = 1; zoneNo <= zones.length/2; zoneNo++) {
            int startPos =zones[2*(zoneNo-1)];
            int endPos=zones[2*(zoneNo-1)+1];
            startPos = Math.max(0, startPos - Math.round(leadTime * signal.samplingRate));
            endPos = Math.min(signal.data[0].length, endPos + Math.round(trailTime * signal.samplingRate));
            for(int j=startPos;j < endPos ; j++ )
            {
                doubles.add(signal.data[0][j]);
            }
        }
        double[] speechArr = new double[doubles.size()];
        for(int i =0;i<speechArr.length;i++)
            speechArr[i]=doubles.get(i);

        return speechArr;

    }


    public static  int[] findSoundZones(AudioIo.AudioSignal signal) {
        EnvelopeDetector envelopeDetector = new EnvelopeDetector(signal.samplingRate);
        double[] envelope = envelopeDetector.process(signal.data[0]);

        int minActivityLen = Math.round(minActivityTime * signal.samplingRate);
        int minSilenceLen = Math.round(minSilenceTime * signal.samplingRate);
        ActivityDetector activityDetector = new ActivityDetector((float)thresholdLevel, minActivityLen, minSilenceLen);

        return activityDetector.process(envelope);
    }


    public int getMinimumVoiceActivityLength(double sampleRate) {
        return (int)(minActivityTime *  sampleRate / 1000);
    }


    public double[] removeSilence(File voiceSampleFile, int sampleRate) throws IOException,
            UnsupportedAudioFileException {

        AudioIo.AudioSignal signal = null;

        signal = AudioIo.loadWavFile(voiceSampleFile.getAbsolutePath());


        int[] zones = findSoundZones(signal);
        if (zones.length == 0) {
            throw new IOException("Silence File");
        }
        ArrayList<Double> doubles = new ArrayList<Double>();
        for (int zoneNo = 1; zoneNo <= zones.length/2; zoneNo++) {
            int startPos =zones[2*(zoneNo-1)];
            int endPos=zones[2*(zoneNo-1)+1];
            startPos = Math.max(0, startPos - Math.round(leadTime * signal.samplingRate));
            endPos = Math.min(signal.data[0].length, endPos + Math.round(trailTime * signal.samplingRate));
            for(int j=startPos;j < endPos ; j++ )
            {
                doubles.add(signal.data[0][j]);
            }
        }
        double[] speechArr = new double[doubles.size()];
        for(int i =0;i<speechArr.length;i++)
            speechArr[i]=doubles.get(i);

        double spTime = 1.0 * speechArr.length / sampleRate;
        logger.info(voiceSampleFile.getName() + " speech duration " + spTime);


        return speechArr;
    }

    public static void main(String[] args) throws Exception {
        String file="/Users/Apple/wave-samples/tel/today/9845404807_sptest73_17_07_2014_11_05_54.wav";
        AudioIo.AudioSignal signal = AudioIo.loadWavFile(file);
        double[] data =signal.data[0];
        double[] spData = new UVoiceActivityDetector().removeSilence(data, signal.samplingRate);
        signal.data=new double[][] {spData};
        AudioIo.saveWavFile("/tmp/xx.wav",signal);




    }
}
