package com.anshuman.spkdet.mistral;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: Anshuman
 * Date: 01/08/14
 * Time: 3:34 PM
 */
public class Verifier {

    private static final Logger logger = LoggerFactory.getLogger(Verifier.class);

    public User identifyUser(File soundFile, List<User> users) {
        Mistral mist = new Mistral();
        Map<String, Double> scoreMap = mist.getScore(users, soundFile);

        TreeMap<Double, User> results = new TreeMap<Double, User>();
        for (String user : scoreMap.keySet()) {
            double v = scoreMap.get(user);
            results.put(v, new User(user, ""));
        }
        logger.info(soundFile.getAbsolutePath() + " ==> " + results);
        return results.get(results.lastKey());
    }


    public Map<String, Double> scoreUser(File soundFile, List<User> users) {
        Mistral mist = new Mistral();
        return mist.getScore(users, soundFile);
    }


    public void trainUser(User user) {
        Mistral mist = new Mistral();
        mist.extractFeatures(user);
        mist.normalizeEnergy(user);
        mist.detectEnergy(user);
        mist.normalizeFeatures(user);
        //MAP
        mist.trainUser(user);
    }

    public void normalizeAudio(User user) {
        Mistral mist = new Mistral();
        mist.extractFeatures(user);
        mist.normalizeEnergy(user);
        mist.detectEnergy(user);
        mist.normalizeFeatures(user);

    }

    public void prepareWorld(List<User> users) {
        Mistral mist = new Mistral();
        mist.trainWorld(users);
    }


}
