
package com.anshuman.spkdet.voiceid.utils;

import com.anshuman.spkdet.voiceid.db.Identifier;

import java.util.ArrayList;
import java.util.Collections;


public class MeanDistanceStrategy implements Strategy {
    private final double distance;


    public MeanDistanceStrategy(double distance) {
        this.distance = distance;
    }

    private double getMean(ArrayList<Double> list) {
        int size = list.size();
        double sum = 0d;
        for (double d : list) {
            sum += d;
        }
        return sum / size;
    }

    @Override
    public Scores filter(Scores score) {
        ArrayList<Double> v = new ArrayList<Double>();
        v.addAll(score.values());
        int size = v.size();
        if (size <= 1)
            return score;
        Collections.sort(v, Collections.reverseOrder());
        double best = v.get(0);
        //double second = v.get(1);
        double mean = getMean(v);
        if (Math.abs(best - mean) >= this.distance) {
            for (Identifier id : score.keySet()) {
                if (score.get(id) == best) {
                    Scores out = new Scores();
                    out.put(id, score.get(id));
                    return out;
                }
            }
        }
        return new Scores();
    }

}
