package com.anshuman.spkdet.voiceid.tests;

import com.ubona.voiceid.db.Identifier;
import com.ubona.voiceid.db.gmm.GMMVoiceDB;
import com.ubona.voiceid.db.gmm.UBMModel;
import com.ubona.voiceid.fm.WavSample;
import com.ubona.voiceid.utils.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

public class TestVoiceId {
    public static void main(String[] args) throws Exception {

        System.out.println("Arrays.toString(args) = " + Arrays.toString(args));
        String wavFile = args[0];


        GMMVoiceDB voiceDB = new GMMVoiceDB("gmmdb", new UBMModel("./store/ubm.gmm"));


        Strategy[] startegies = new Strategy[2];
        startegies[0] = new ThresholdStrategy(2, 0.2);
        startegies[1] = new DistanceStrategy(0.1);


//        List<File> segmentFiles = new AudioUtils(wavFile).getSegmentFiles();
//        for (File file : segmentFiles) {
            File file = new File(wavFile);
            WavSample sample = new WavSample(file);
            Scores scores = voiceDB.voiceLookup(sample);
            HashMap<Identifier, Double> best = scores.getBestFive();
            System.out.println(file.getName() + "  Best five " + best);
            HashMap<Identifier, Double> qualified = scores.getBest(startegies);
            System.out.println(file.getName() + " Qualified " + qualified);
//        }


    }
}
