package com.anshuman.spkdet.voiceid.tests;

import com.ubona.speakerid.MatchResult;
import com.ubona.speakerid.SpeakerRecog;

import java.util.Arrays;
import java.util.List;

public class TestSpeakerId {

    public static final double DIFF_THRESHOLD = 0.25;
    public static final double SCORE_THRESHOLD = 1.5;

    public static void main(String[] args) throws Exception {

        System.out.println("Arrays.toString(args) = " + Arrays.toString(args));
//        String wavFile = args[0];
        SpeakerRecog recog = SpeakerRecog.getInstance();
        for (String wavFile : args) {
            List<MatchResult<String>> list = recog.recognize(wavFile);
            String result = "UNKNOWN";

            if (list.size() > 0) {
                MatchResult<String> best = list.get(0);
                System.out.println(wavFile + " ::: " + list);
                double diff = 0.5;
                if (list.size() > 1) {
                    MatchResult<String> secondBest = list.get(1);
                    diff = best.getDistance() - secondBest.getDistance();
                }

                if (best.getDistance() > 2.0) {
                    result = best.getKey();
                } else {

                    if (best.getDistance() > SCORE_THRESHOLD && diff > DIFF_THRESHOLD) {

                        result = best.getKey();
                    }
                }
                System.out.println("Final " + wavFile + " diff= " + diff + " BEST=" + best);

//                if (!result.equals("UNKNOWN")) {
//
//                    List<MatchResult<String>> negativeList = recog.verify(wavFile, result);
//                    best = negativeList.get(0);
//                    System.out.println(wavFile  +  " Verfiy == " + best);
//
//
//                }


            }

            System.out.println("Final  " + wavFile + "  " + result);


        }


    }
}
