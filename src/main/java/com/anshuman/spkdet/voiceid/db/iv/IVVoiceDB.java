package com.anshuman.spkdet.voiceid.db.iv;



import com.anshuman.spkdet.voiceid.db.Identifier;
import com.anshuman.spkdet.voiceid.db.Sample;
import com.anshuman.spkdet.voiceid.db.VoiceDB;
import com.anshuman.spkdet.voiceid.utils.Scores;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.libMatrix.MatrixIO;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.ModelIO;
import fr.lium.spkDiarization.libModel.ivector.EigenFactorRadialList;
import fr.lium.spkDiarization.libModel.ivector.IVector;
import fr.lium.spkDiarization.libModel.ivector.IVectorArrayList;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * User: Anshuman
 * Date: 04/09/14
 * Time: 10:38 AM
 */
public class IVVoiceDB implements VoiceDB, IVConfiguration {

    private File path;
    private IVectorArrayList models;
    private String ubmPath;


    private EigenFactorRadialList normalization;
    private GMM ubm;
    private MatrixSymmetric covariance;

    public IVVoiceDB(File path) {
        this.path = path;
        models = new IVectorArrayList();
        this.ubmPath = UBM_PATH;
        try {
            this.ubm = readGMM(UBM_PATH);

            this.normalization = EigenFactorRadialList.readXML(UBM_EFN_XML);
            covariance = MatrixIO.readMatrixSymmetric(MAH_FILE, false);


            covariance = covariance.invert();
            readDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GMM readGMM(String ubmPath) throws IOException, DiarizationException {
        GMMArrayList gmms = new GMMArrayList();
        IOFile file = new IOFile(ubmPath, "rb");
        file.open();
        ModelIO.readerGMMContainer(file, gmms);
        file.close();
        return gmms.get(0);
    }


    @Override
    public boolean readDb() throws Exception {
        if (path.exists()) {
            this.models = IVectorArrayList.loadIVector(path.getAbsolutePath());

        }

        return true;
    }

    @Override
    public boolean addModel(Sample sample, Identifier identifier) {

        IVectorArrayList iVectors = getiVectors(sample, identifier);
        for (IVector v : iVectors) {
            v.setSpeakerID(identifier.toString());
            v.setName(identifier.toString());
            this.models.add(v);
        }

        try {
            IVectorArrayList.writeIVector(path.getAbsolutePath(), models);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private IVectorArrayList getiVectors(Sample sample, Identifier identifier) {
        String id = identifier.toString();
        IvUtil util = new IvUtil(sample.getResource().getPath(), ubm, normalization, id);
        return util.getiVectors();
    }

    @Override
    public boolean removeModel() {

        return path.delete();
    }

    @Override
    public Scores matchVoice(Sample sample, Identifier identifier) {
        IVectorArrayList testV = getiVectors(sample, identifier);
        IVector testVector = testV.get(0);
        Scores scores = new Scores();

        for (IVector bVector : this.models) {
            if (bVector.getSpeakerID().equals(identifier.toString())) {
                try {
                    double v = Distance.iVectorMahalanobis(bVector, testVector, covariance);
                    scores.put(new Identifier(bVector.getSpeakerID()), v);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return scores;
    }

    @Override
    public Scores voiceLookup(Sample sample) {
        IVectorArrayList testV = getiVectors(sample, new Identifier("test"));
        IVector testVector = testV.get(0);
        Scores scores = new Scores();


        for (IVector bVector : this.models) {


            try {
                double v = Distance.iVectorMahalanobis(bVector, testVector, covariance);
                scores.put(new Identifier(bVector.getSpeakerID()), v);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return scores;
    }


    @Override
    public Scores voiceLookup(Sample sample, List<String> list) {
        IVectorArrayList testV = getiVectors(sample, new Identifier("test"));

        IVector testVector = testV.get(0);
        Scores scores = new Scores();

        for (IVector bVector : this.models) {
            if (list.contains(bVector.getSpeakerID())) {
                try {
                    double v = Distance.iVectorMahalanobis(bVector, testVector, covariance);
                    scores.put(new Identifier(bVector.getSpeakerID()), v);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return scores;
    }
}
