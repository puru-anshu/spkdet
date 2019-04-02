
package com.anshuman.spkdet.voiceid.utils;


import com.anshuman.spkdet.voiceid.db.Identifier;

public class ThresholdStrategy implements Strategy {
	final private double threshold;
	final private double tolerance;


	public ThresholdStrategy(double threshold, double tolerance) {
		super();
		this.threshold = threshold;
		this.tolerance = tolerance;
	}


	public ThresholdStrategy(double threshold) {
		this(threshold, 0.0);
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ubona.voiceid.utils.Strategy#filter(com.ubona
	 * .voiceid.utils.Scores)
	 */
	@Override
	public Scores filter(Scores score) {
		Scores filteredScore = new Scores();
		double val;
		for (Identifier id : score.keySet()) {
			val = score.get(id);
			if (val > threshold - tolerance)
				filteredScore.put(id, val);
		}
		return filteredScore;
	}

}
