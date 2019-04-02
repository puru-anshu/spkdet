package com.anshuman.spkdet.voiceid.tests;



import com.anshuman.spkdet.voiceid.db.Identifier;
import com.anshuman.spkdet.voiceid.db.gmm.GMMVoiceDB;
import com.anshuman.spkdet.voiceid.db.gmm.UBMModel;
import com.anshuman.spkdet.voiceid.fm.WavSample;
import com.anshuman.spkdet.voiceid.utils.Scores;

import java.io.File;
import java.util.Arrays;

public class MatchVoiceId {
    public static void main(String[] args) throws Exception {

        System.out.println("Arrays.toString(args) = " + Arrays.toString(args));
        String wavFile = args[0];
        String user = args[1];


        GMMVoiceDB voiceDB = new GMMVoiceDB("gmmdb", new UBMModel("./store/ubm.gmm"));


        WavSample sample = new WavSample(new File(wavFile));
        Identifier id =  new Identifier(user);

        Scores scores = voiceDB.matchVoice(sample, id);
        System.out.println("scores = " + scores);




    }
}
