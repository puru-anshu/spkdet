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
public class IVVerifier {

    private static final Logger logger = LoggerFactory.getLogger(IVVerifier.class);

    public User identifyUser(File soundFile, List<User> users) {
        IVMistral mist = new IVMistral();
        Map<String, Double> scoreMap = mist.getScore(users, soundFile);

        TreeMap<Double, User> results = new TreeMap<Double, User>();
        for (String user : scoreMap.keySet()) {
            double v = scoreMap.get(user);
            results.put(v, new User(user, ""));
        }
        logger.info(soundFile.getAbsolutePath() + " ==> " + results);
        if(results.size() > 0)
             return results.get(results.lastKey());
        else
            return new User("","");
    }


    public Map<String, Double> scoreUser(File soundFile, List<User> users) {
        IVMistral mist = new IVMistral();
        return mist.getScore(users, soundFile);
    }


    public void trainUser(User user) {
        IVMistral mist = new IVMistral();
        mist.extractFeatures(user);
        mist.normalizeEnergy(user);
        mist.detectEnergy(user);

        //IVector
        mist.totalVariabilityMatrixEstimation(user);
        mist.generateIVector(user);
    }

    public void normalizeAudio(User user) {
        IVMistral mist = new IVMistral();
        mist.extractFeatures(user);
        mist.normalizeEnergy(user);
        mist.detectEnergy(user);
        mist.normalizeFeatures(user);
    }



}
