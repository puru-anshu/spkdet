package com.anshuman.spkdet.speakerid;



import com.anshuman.spkdet.voiceid.db.Identifier;
import com.anshuman.spkdet.voiceid.db.iv.IVVoiceDB;
import com.anshuman.spkdet.voiceid.fm.WavSample;
import com.anshuman.spkdet.voiceid.utils.Scores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * User: Anshuman
 * Date: 02/07/14
 * Time: 2:39 PM
 */
public class ISpeakerRecog {

    private static final Logger logger = LoggerFactory.getLogger(ISpeakerRecog.class);


    private static ISpeakerRecog ourInstance = new ISpeakerRecog();
    private final String storeDir = "./iv/speakers.ivv";
    private final IVVoiceDB gmmRecognito = new IVVoiceDB(new File(storeDir));


    public static ISpeakerRecog getInstance() {
        return ourInstance;
    }

    private ISpeakerRecog() {


    }

    public void train(String user, String filePath) {
        try {

            gmmRecognito.addModel(new WavSample(new File(filePath)), new Identifier(user));
        } catch (Exception e) {
            logger.warn("Could not train " + filePath, e);
        }
    }

    public void merge(String user, String filePath) {


    }


    public List<MatchResult<String>> recognize(String filePath) {
        List<MatchResult<String>> toR = new ArrayList<MatchResult<String>>();
        File voiceSampleFile = null;
        try {

            voiceSampleFile = new File(filePath);
            Scores scores = gmmRecognito.voiceLookup(new WavSample(voiceSampleFile));
            for (Identifier user : scores.keySet()) {
                double score = scores.get(user);
                toR.add(new MatchResult<String>(user.toString(), 50, score));
            }

            Collections.sort(toR, new Comparator<MatchResult<String>>() {
                @Override
                public int compare(MatchResult<String> m1, MatchResult<String> m2) {
                    return Double.compare(m1.getDistance(), m2.getDistance());
                }
            });

            logger.info("RESULT " + filePath + " " + toR.toString());

        } catch (Exception e) {
            logger.warn(filePath + " " + e.getMessage());
        }

        return toR;

    }


    public List<MatchResult<String>> recognize(String filePath, List<String> users) {
        List<MatchResult<String>> toR = new ArrayList<MatchResult<String>>();
        File voiceSampleFile = null;
        try {

            voiceSampleFile = new File(filePath);
            Scores scores = gmmRecognito.voiceLookup(new WavSample(voiceSampleFile), users);

            logger.warn("Score : " + voiceSampleFile.getName() + " " + scores.toString());

            for (Identifier user : scores.keySet()) {
                double score = scores.get(user);
                toR.add(new MatchResult<String>(user.toString(), 50, score));
            }
            Collections.sort(toR, new Comparator<MatchResult<String>>() {
                @Override
                public int compare(MatchResult<String> m1, MatchResult<String> m2) {
                    return Double.compare(m1.getDistance(), m2.getDistance());
                }
            });

//            Collections.reverse(toR);
            logger.info("RESULT " + filePath + " " + toR.toString());

        } catch (Exception e) {
            logger.warn(filePath + " " + e.getMessage());
        }

        return toR;

    }


    public static void main(String[] args) {

        ISpeakerRecog verifier = ISpeakerRecog.getInstance();
        boolean build = false;
        if (build) {
            File baseDir = new File("/Users/Apple/wave-samples/tel/manas/");
            for (File file : baseDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.endsWith("wav");
                }
            })) {
                try {
                    String userId = file.getName().replace(".wav", "");
                    verifier.train(userId, file.getPath());

                } catch (Exception e) {
                    System.err.println(file.getName() + " " + e.getMessage());
                }
            }

        }
        String[] users = {"7259421709", "9164742218", "9591716591", "9611552333", "9740199766", "9845205601", "9845404807", "9916329079", "9916796519",
                "7795196151", "9590884170", "9611123425", "9620710549", "9845153342", "9845394354", "9886810661", "9916745027", "9986656219",};

        List<String> userList = Arrays.asList(users);


        int sCount = 0;
        File testDir = new File("/Users/Apple/wave-samples/tel/2014-07-13/done");
        for (File file : testDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith("wav");
            }
        })) {
            try {

                List<MatchResult<String>> res = verifier.recognize(file.getAbsolutePath());
                System.out.println("res = " + res);
                if (res.size() > 0) {
                    String key = res.get(0).getKey();
                    double d = res.get(0).getDistance();

                    if (file.getName().startsWith(key)) {
                        sCount++;
                    }
                }
                System.out.println(file.getName() + " :: " + res);

            } catch (Exception e) {
                System.err.println(file.getName() + " " + e.getMessage());
            }
        }

        System.out.println("sCount = " + sCount);


    }

    public List<MatchResult<String>> verify(String wavFile, String result) throws Exception {

        Scores scores = gmmRecognito.matchVoice(new WavSample(new File(wavFile)), new Identifier(result));
        List<MatchResult<String>> toR = new ArrayList<MatchResult<String>>();
        HashMap<Identifier, Double> bestFive = scores.getBestFive();
        for (Identifier user : bestFive.keySet()) {
            double score = bestFive.get(user);
            toR.add(new MatchResult<String>(user.toString(), 50, score));
        }
        Collections.sort(toR, new Comparator<MatchResult<String>>() {
            @Override
            public int compare(MatchResult<String> m1, MatchResult<String> m2) {
                return Double.compare(m1.getDistance(), m2.getDistance());
            }
        });

        Collections.reverse(toR);

        return toR;

    }
}


