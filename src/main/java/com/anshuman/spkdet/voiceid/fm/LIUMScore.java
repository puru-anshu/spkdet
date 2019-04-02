
package com.anshuman.spkdet.voiceid.fm;

import com.anshuman.spkdet.voiceid.db.Identifier;
import com.anshuman.spkdet.voiceid.db.Sample;
import com.anshuman.spkdet.voiceid.db.VoiceModel;
import com.anshuman.spkdet.voiceid.db.gmm.MemoryModel;
import com.anshuman.spkdet.voiceid.db.gmm.UBMModel;
import com.anshuman.spkdet.voiceid.ruby.Audio;
import com.anshuman.spkdet.voiceid.utils.Scores;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.programs.MScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;


public class LIUMScore implements VoiceScorer {


    private static Logger logger = LoggerFactory.getLogger(LIUMScore.class);
    private static UBMModel ubmmodel = null;


    public LIUMScore(UBMModel ubmmodel) {
        if (LIUMScore.getUbmmodel() == null
                || !ubmmodel.equals(LIUMScore.getUbmmodel())) {
            LIUMScore.setUbmmodel(ubmmodel);
        }
    }

    /*
     * The score method takes a {@link Sample} and {@link VoiceModel} and return
     * a {@link Scores} object.
     *
     * @see
     * com.ubona.voiceid.fm.VoiceScorer#score(com.ubona.
     * voiceid.db.Sample, com.ubona.voiceid.db.VoiceModel)
     */
    @Override
    public Scores score(Sample sample, VoiceModel voicemodel)
            throws IOException, UnsupportedAudioFileException {
        return score(new WavSample(sample), voicemodel);


    }


    public Scores score(WavSample sample, VoiceModel voicemodel) {

        File input = sample.toWav();

        try {
            Audio audio = new Audio(input.getAbsolutePath());
            audio.analyze(true);
            ClusterSet clusters = audio.getClusters();
            GMMArrayList topGaussian = audio.getTopGaussian();
            AudioFeatureSet audioFeatureSet = audio.getFeatureSet();
            GMMArrayList gmmList = voicemodel.getGMMList();
            Parameter parameter = new Parameter();
            parameter.getParameterScore().setByCluster(true);
            parameter.getParameterTopGaussian().setTopGaussian("8," + ubmmodel.getAbsolutePath());
            parameter.getParameterScore().setLabel("add");
            boolean tNorm = voicemodel instanceof MemoryModel;
            parameter.getParameterScore().setTNorm(false);
//            parameter.getParameterScore().setGender(true);
            ClusterSet result = MScore.make(audioFeatureSet, clusters, gmmList, topGaussian, parameter);
            return toScores(result);

        } catch (Exception e) {
            logger.error("Score error");
            logger.error(e.getMessage());
            return null;
        }
    }


    private Scores toScores(ClusterSet clusterResult) {
        Scores result = new Scores();
        ArrayList<Cluster> clusterVectorRepresentation = clusterResult.getClusterVectorRepresentation();
        for (Cluster c : clusterVectorRepresentation) {
            TreeMap<String, Object> information = c.getInformation();
            for (String key : information.keySet()) {
                String user = key.replace("score:", "").trim();
                if (user.equals("UBM") || user.equalsIgnoreCase("lenght") || user.equals("length")) continue;
                double score = Double.parseDouble(information.get(key).toString());
                try {
                    result.put(new Identifier(user.indexOf(":") > 1 ? user.split(":")[1] : user), score);
                } catch (Exception e) {
                    //
                }
            }

        }

        return result;
    }


    public static UBMModel getUbmmodel() {
        return ubmmodel;
    }


    public static void setUbmmodel(UBMModel ubmmodel) {
        LIUMScore.ubmmodel = ubmmodel;
    }


}
