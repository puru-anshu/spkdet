
package com.anshuman.spkdet.voiceid.utils;



import com.anshuman.spkdet.voiceid.db.Identifier;

import java.util.ArrayList;
import java.util.Collections;


public class DistanceStrategy implements Strategy {
	private final double distance;


	public DistanceStrategy(double distance) {
		this.distance = distance;
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
		double second = v.get(1);
		if (Math.abs(best - second) >= this.distance) {
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
