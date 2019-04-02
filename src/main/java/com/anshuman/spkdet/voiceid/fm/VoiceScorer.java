
package com.anshuman.spkdet.voiceid.fm;


import com.anshuman.spkdet.voiceid.db.Sample;
import com.anshuman.spkdet.voiceid.db.VoiceModel;
import com.anshuman.spkdet.voiceid.utils.Scores;

public interface VoiceScorer {


	public Scores score(Sample sample, VoiceModel voicemodel) throws Exception;

}
