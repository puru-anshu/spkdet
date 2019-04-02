
package com.anshuman.spkdet.voiceid.db.gmm;

import com.anshuman.spkdet.voiceid.db.Identifier;
import com.anshuman.spkdet.voiceid.db.Sample;
import com.anshuman.spkdet.voiceid.db.VoiceDB;
import com.anshuman.spkdet.voiceid.fm.LIUMScore;
import com.anshuman.spkdet.voiceid.fm.VoiceScorer;
import com.anshuman.spkdet.voiceid.fm.WavSample;
import com.anshuman.spkdet.voiceid.ruby.Audio;
import com.anshuman.spkdet.voiceid.utils.Scores;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GMMVoiceDB implements VoiceDB {


    private static Logger logger = LoggerFactory.getLogger(GMMVoiceDB.class);

    private File path;

    private ArrayList<GMMFileVoiceModel> models;

    private static UBMModel ubmmodel = null;


    public GMMVoiceDB(String path, UBMModel ubmmodel) throws Exception {
        if (GMMVoiceDB.getUbmmodel() == null
                || !ubmmodel.equals(GMMVoiceDB.getUbmmodel())) {
            GMMVoiceDB.setUbmmodel(ubmmodel);
        }
        Audio.UBM_FILE = getUbmmodel().getAbsoluteFile();
        this.path = new File(path);
        if (!this.path.exists())
            throw new IOException("GMMVoiceDB: No such file " + path);
        if (!this.path.isDirectory())
            throw new IOException("GMMVoiceDB: " + path + " is not a directory");
        this.readDb();

    }


    public GMMVoiceDB(String path) throws Exception {
        this(path, new UBMModel("./store/ubm.gmm"));
    }

    public boolean readDb() throws Exception {
        models = new ArrayList<GMMFileVoiceModel>();
        File[] files = this.path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".gmm");
            }
        });
        for (File f : files) {
            String basename = f.getName().split("\\.(?=[^\\.]+$)")[0];
            models.add(new GMMFileVoiceModel(f.getAbsolutePath(), new Identifier(basename)));
        }

        return true;
    }

    public boolean updateDB() {

        return false;
    }

    private GMMFileVoiceModel getModelById(Identifier identifier) {

        for (GMMFileVoiceModel gmm : this.models) {
            if (gmm.getIdentifier().toString().equals(identifier.toString())) {
                return gmm;
            }
        }
        return null;
    }


    @Override
    public boolean addModel(Sample sample, Identifier identifier) {
        WavSample wavsample;
        try {
            wavsample = new WavSample(sample.getResource());
            String gmmPath = path.getAbsolutePath() + File.separator + identifier.toString();
            buildModel(wavsample, ubmmodel, identifier.toString(), gmmPath);

        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (UnsupportedAudioFileException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        return true;
    }


    @Override
    public Scores matchVoice(Sample sample, Identifier identifier) {
        try {
            return matchVoice(sample, identifier, new LIUMScore(
                    GMMVoiceDB.ubmmodel));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public Scores matchVoice(Sample sample, Identifier identifier,
                             VoiceScorer scorer) throws Exception {
        GMMFileVoiceModel voicemodel = this.getModelById(identifier);
        return scorer.score(sample, voicemodel);
    }

    private ArrayList<GMMFileVoiceModel> geModels() {
        return models;
    }


    @Override
    public Scores voiceLookup(Sample sample) {
        try {
            return voiceLookup(sample, new LIUMScore(ubmmodel));
        } catch (Exception e) {
            logger.error("voiceLookup: error");
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    public Scores voiceLookup(Sample sample, VoiceScorer scorer)
            throws InterruptedException {


        Scores score = new Scores();

        GMMArrayList allList = new GMMArrayList();

        for (GMMFileVoiceModel gmm : geModels()) {
            String identifer = gmm.getIdentifier().toString();
            for (GMM g : gmm.getGMMList()) {
                g.setName(identifer);
                allList.add(g);
            }

        }

        try {
            score = scorer.score(new WavSample(sample), new MemoryModel(allList));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return score;
    }

    @Override
    public Scores voiceLookup(Sample sample, List<String> list) {

        VoiceScorer scorer = new LIUMScore(ubmmodel);
        Scores score = new Scores();
        GMMArrayList allList = new GMMArrayList();
        for (GMMFileVoiceModel gmm : geModels()) {
            String identifer = gmm.getIdentifier().toString();
            if (list.contains(identifer)) {
                for (GMM g : gmm.getGMMList()) {
                    g.setName(identifer);
                    allList.add(g);
                }
            }

        }
        try {
            score = scorer.score(new WavSample(sample), new MemoryModel(allList));
        } catch (Exception e) {
            e.printStackTrace();
        }


        return score;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ubona.voiceid.db.VoiceDB#removeModel()
     */
    @Override
    public boolean removeModel() {
        return false;
    }


    public static UBMModel getUbmmodel() {
        return ubmmodel;
    }


    public static void setUbmmodel(UBMModel ubmmodel) {
        GMMVoiceDB.ubmmodel = ubmmodel;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.ubona.voiceid.db.VoiceDB#getGenders()
     */
    public void mergeModels(GMMFileVoiceModel origmodel,
                            ArrayList<GMMFileVoiceModel> list, boolean deleteOnMerge)
            throws IOException, DiarizationException {

        for (GMMFileVoiceModel model : list) {
            origmodel.merge(model);
            if (deleteOnMerge) {
                model.delete();
            }
        }
        updateDB();

    }


    private void buildModel(WavSample wavSample, UBMModel ubmmodel,
                            String name, String gmmPath) throws Exception {

        File trimmed = wavSample.toWav();
        try {
            Audio.UBM_FILE = ubmmodel.getAbsoluteFile();
            Audio audio = new Audio(trimmed.getAbsolutePath());
            audio.analyze(true);
            GMMArrayList speakers = audio.getGmmList();
            for (GMM gmm : speakers)
                gmm.setName(name);
            Parameter parameter = new Parameter();
            gmmPath = gmmPath + ".gmm";
            logger.warn("Saving gmm to path " + gmmPath);
            parameter.getParameterModelSetOutputFile().setMask(gmmPath);
            MainTools.writeGMMContainer(parameter, speakers);
            models.add(new GMMFileVoiceModel(gmmPath, new Identifier(name)));
            logger.debug("Number of models " + models.size());

        } catch (DiarizationException e) {
            logger.error("error \t Exception : " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }


}
