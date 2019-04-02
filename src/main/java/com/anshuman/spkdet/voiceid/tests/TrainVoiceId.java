package com.anshuman.spkdet.voiceid.tests;

import com.ubona.voiceid.db.Identifier;
import com.ubona.voiceid.db.gmm.GMMVoiceDB;
import com.ubona.voiceid.db.gmm.UBMModel;
import com.ubona.voiceid.fm.WavSample;

import java.io.File;
import java.util.Arrays;

public class TrainVoiceId {
    public static void main(String[] args) throws Exception {

        System.out.println("Arrays.toString(args) = " + Arrays.toString(args));
        String wavFile = args[0];
        String user = args[1];


        GMMVoiceDB voiceDB = new GMMVoiceDB("gmmdb", new UBMModel("./store/ubm.gmm"));


        WavSample sample = new WavSample(new File(wavFile));
        voiceDB.addModel(sample, new Identifier(user));


    }
}
