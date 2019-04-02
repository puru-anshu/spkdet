package com.anshuman.spkdet.speakerid;


public class MatchResult<K> {
    
    private final K key;
    private final int likelihoodRatio;
    private final double distance;
    
    /**
     * Default constructor
     * @param key the user defined key for the corresponding VoicePrint
     * @param likelihoodRatio the likelihood ratio expressed as a percentage
     */
    public MatchResult(K key, int likelihoodRatio, double distance) {
        super();
        this.key = key;
        this.likelihoodRatio = likelihoodRatio;
        this.distance = distance;
    }

    /**
     * Get the matched key
     * @return the key
     */
    public K getKey() {
        return key;
    }

    /**
     * Get the likelihoodRatio level
     * @return the likelihoodRatio ratio expressed as a percentage
     */
    public int getLikelihoodRatio() {
        return likelihoodRatio;
    }
    
    /**
     * Get the raw distance between the <code>VoicePrint</code> idenntified by K 
     * and the given voice sample
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    public String toString()
    {
        return String.format("%s(distance=%2.2f )",key.toString(),distance);
    }
}
