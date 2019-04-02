
package com.anshuman.spkdet.voiceid.db;


import com.anshuman.spkdet.voiceid.fm.VoiceScorer;
import com.anshuman.spkdet.voiceid.utils.Scores;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;


public interface VoiceModel {
	public Scores scoreSample(Sample sample, VoiceScorer voicescorer);

    public GMMArrayList getGMMList();
}
