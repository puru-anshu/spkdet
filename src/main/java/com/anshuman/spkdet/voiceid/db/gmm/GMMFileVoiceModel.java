
package com.anshuman.spkdet.voiceid.db.gmm;

import com.anshuman.spkdet.voiceid.db.AbstractFileVoiceModel;
import com.anshuman.spkdet.voiceid.db.Identifier;
import com.anshuman.spkdet.voiceid.db.Sample;
import com.anshuman.spkdet.voiceid.fm.VoiceScorer;
import com.anshuman.spkdet.voiceid.utils.Scores;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.ModelIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class GMMFileVoiceModel extends AbstractFileVoiceModel {

    private static Logger logger = LoggerFactory.getLogger(GMMFileVoiceModel.class);
    private static final long serialVersionUID = 7297011177725502307L;
    public GMMArrayList gmmlist;

    public GMMFileVoiceModel(String path, Identifier id) throws Exception {
        super(path, id);
        if (!this.verifyGMMFormat())
            throw new IOException(this.getName() + " is not in right format");
        try {
            gmmlist = new GMMArrayList(this.extractGMMList());

        } catch (DiarizationException e) {
            logger.warn(e.getMessage());
        }
    }

    private boolean verifyGMMFormat() throws Exception {
        try {
            GMMArrayList vect = new GMMArrayList();
            IOFile fi = new IOFile(this.getAbsolutePath(), "rb");
            fi.open();
            ModelIO.readerGMMContainer(fi, vect);
            fi.close();
            this.identifier = new Identifier(vect.get(0).getName());
        } catch (DiarizationException e) {
            logger.warn(e.getMessage());
            return false;
        } catch (IOException e) {
            logger.warn(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean merge(GMMFileVoiceModel other) throws DiarizationException {

        GMMArrayList vect = new GMMArrayList(other.getGMMList());

        gmmlist.addAll(vect);
        IOFile fo = new IOFile(this.getAbsolutePath(), "wb");
        try {
            fo.open();
            ModelIO.writerGMMContainer(fo, gmmlist);
            fo.close();
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }

        return true;

    }


    public void replace(GMMFileVoiceModel other) throws DiarizationException {
        gmmlist = new GMMArrayList(other.getGMMList());
        IOFile fo = new IOFile(this.getAbsolutePath(), "wb");
        try {
            fo.open();
            ModelIO.writerGMMContainer(fo, gmmlist);
            fo.close();
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
    }

    void replaceGMM(GMM gmm_in, GMM gmm_out) throws IOException {
        int gmm_in_index = gmmlist.indexOf(gmm_in);
        gmmlist.set(gmm_in_index, gmm_out);
        IOFile fo = new IOFile(this.getAbsolutePath(), "wb");
        try {
            fo.open();
            ModelIO.writerGMMContainer(fo, gmmlist);
            fo.close();
        } catch (DiarizationException e) {
            logger.warn(e.getMessage());
        }
    }

    void addGMM(GMM gmm) throws IOException {
        gmmlist.add(gmm);
        IOFile fo = new IOFile(this.getAbsolutePath(), "wb");
        try {
            fo.open();
            ModelIO.writerGMMContainer(fo, gmmlist);
            fo.close();
        } catch (DiarizationException e) {
            logger.warn(e.getMessage());
        }
    }

    public GMMArrayList extractGMMList() throws DiarizationException {
        GMMArrayList vect = new GMMArrayList();
        if (!this.exists()) {
            logger.warn("input model don't exist " + this.getName());
            return null;
        }
        if (this.equals("")) {
            logger.warn("warring[MainTools] \t input model empty " + this.getName());
            return null;
        }
        IOFile fi = new IOFile(this.getAbsolutePath(), "rb");
        for (int i = 0; i < vect.size(); i++) {
            vect.get(i).sortComponents();
        }
        try {
            fi.open();
            ModelIO.readerGMMContainer(fi, vect);
            fi.close();
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
        return vect;
    }

    public GMMArrayList getGMMList() {
        return gmmlist;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ubona.voiceid.db.VoiceModel#scoreSample(com.ubona
     * .voiceid.db.Sample)
     */
    public Scores scoreSample(Sample sample, VoiceScorer voicescorer) {
        try {
            return voicescorer.score(sample, this);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return null;
    }
}
