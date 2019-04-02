package com.anshuman.spkdet.voiceid.db.gmm;

import com.anshuman.spkdet.voiceid.db.Sample;
import com.anshuman.spkdet.voiceid.db.VoiceModel;
import com.anshuman.spkdet.voiceid.fm.VoiceScorer;
import com.anshuman.spkdet.voiceid.fm.WavSample;
import com.anshuman.spkdet.voiceid.utils.Scores;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;

/**
 * 
 * User: Anshuman
 * Date: 06/08/14
 * Time: 12:23 PM
 * 
 */
public class MemoryModel  implements VoiceModel {
    private GMMArrayList gmms ;

    public MemoryModel(GMMArrayList gmms) {
        this.gmms = gmms;
    }



    @Override
    public Scores scoreSample(Sample sample, VoiceScorer voicescorer) {
        try {
            return voicescorer.score(new WavSample(sample),this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Scores();
    }

    @Override
    public GMMArrayList getGMMList() {
        return gmms;
    }
}
