package com.anshuman.spkdet.voiceid.db.iv;

import com.ubona.mistral.AudioUtils;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libMatrix.MatrixIO;
import fr.lium.spkDiarization.libMatrix.MatrixRectangular;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.ModelIO;
import fr.lium.spkDiarization.libModel.ivector.EigenFactorRadialList;
import fr.lium.spkDiarization.libModel.ivector.EigenFactorRadialNormalizationFactory;
import fr.lium.spkDiarization.libModel.ivector.IVectorArrayList;
import fr.lium.spkDiarization.libModel.ivector.TotalVariability;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.programs.*;
import fr.lium.spkDiarization.system.Telephone;
import fr.lium.spkDiarization.tools.SFilter;
import fr.lium.spkDiarization.tools.SSplitSeg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: Anshuman
 * Date: 04/09/14
 * Time: 10:47 AM
 */
public class IvUtil implements IVConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(IvUtil.class);

    private String audioPath;


    public static final String fDesc = "audio8kHz2sphinx,1:1:0:0:0:0,13,0:0:0";
    public static final String fDescIV = "audio8kHz2sphinx,1:3:2:0:0:0,13,1:1:0:0";


    private ClusterSet clusters;
    private AudioFeatureSet audioFeatureSet;
    private IVectorArrayList iVectors;


    private EigenFactorRadialList normalization;
    private GMM ubm;
    private String identifier;

    public IvUtil(String audioPath, GMM ubm, EigenFactorRadialList efr, String id) {

        this.audioPath = audioPath;
        this.identifier = id;

        this.normalization = efr;
        this.ubm = ubm;
        try {
            analyze();
        } catch (Exception e) {
            logger.warn("Exception ", e);
        }
    }


    public void analyze() throws Exception {
        File speechWav = AudioUtils.getSpeechWav(new File(audioPath));
        try {
            Parameter parameter = new Parameter();
            parameter.show = identifier;
            parameter.getParameterInputFeature().setFeaturesDescription(fDesc);

            parameter.getParameterDiarization().setCEClustering(true);
            parameter.getParameterInputFeature().setFeatureMask(speechWav.getAbsolutePath());
            clusters = makeMedia(parameter);

            parameter = new Parameter();
            parameter.show = identifier;
            parameter.getParameterInputFeature().setFeaturesDescription(fDescIV);
            parameter.getParameterInputFeature().setFeatureMask(speechWav.getAbsolutePath());
            audioFeatureSet = MainTools.readFeatureSet(parameter, clusters);
            audioFeatureSet.setCurrentShow(parameter.show);
            iVectors = makeIVector();
            iVectors = normalize(iVectors, 5);

        } finally {
            if (speechWav.getPath().startsWith("/tmp")) {
                speechWav.delete();
            }
        }

    }

    private IVectorArrayList makeIVector() throws DiarizationException, IOException {
        //Get the UBM
        MatrixRectangular totalFactorMatrix = MatrixIO.readRectMatrix(UBM_TV_MAT, false);
        TotalVariability totalFactorFactory = new TotalVariability(ubm, totalFactorMatrix);
        totalFactorFactory.computeStatistics(clusters, audioFeatureSet, false);
        logger.info("Train i-vector");
        return totalFactorFactory.trainIVector();

    }


    private IVectorArrayList normalize(IVectorArrayList iVectorList, int nb) throws Exception {
        return EigenFactorRadialNormalizationFactory.applied(iVectorList, normalization);


    }


    private ClusterSet makeMedia(Parameter parameter) throws DiarizationException, Exception {
        parameter.help = true;
        String mask = parameter.getParameterSegmentationOutputFile().getMask();

        // ** get the first diarization
        ClusterSet clusters = null;
        if (parameter.getParameterDiarization().isLoadInputSegmentation()) {
            clusters = MainTools.readClusterSet(parameter);
        } else {
            clusters = new ClusterSet();
            Cluster cluster = clusters.createANewCluster("init");
            Segment segment = new Segment(parameter.show, 0, 1, cluster, parameter.getParameterSegmentationInputFile().getRate());
            cluster.addSegment(segment);
        }

        // ** load the features, sphinx format (13 MFCC with C0) or compute it form a wave file
        AudioFeatureSet features = Telephone.loadFeature(parameter, clusters, parameter.getParameterInputFeature().getFeaturesDescriptorAsString());

        features.setCurrentShow(parameter.show);
        int nbFeatures = features.getNumberOfFeatures();

        if (!parameter.getParameterDiarization().isLoadInputSegmentation()) {
            clusters.getFirstCluster().firstSegment().setLength(nbFeatures);
        }

        // ** check the quality of the MFCC, remove similar consecutive MFCC of the featureSet
        ClusterSet clustersSegInit = new ClusterSet();
        MSegInit.make(features, clusters, clustersSegInit, parameter);
        clustersSegInit.collapse();
        parameter.getParameterSegmentationOutputFile().setMask(mask + ".i.seg");
        if (parameter.getParameterDiarization().isSaveAllStep()) {
            MainTools.writeClusterSet(parameter, clustersSegInit, false);
        }
        String FeatureFormat = "featureSetTransformation";
        AudioFeatureSet featureSet2 = loadFeature(features, parameter, clustersSegInit, FeatureFormat
                + ",3:1:0:0:0:0,13,0:0:0:0");

        ClusterSet clusterSNS = new ClusterSet();
        Cluster clusterS = clusterSNS.createANewCluster("f2");
        Cluster clusterNS = clusterSNS.createANewCluster("iT");
        // param.getParameterSegmentation().setSilenceThreshold(0.1);
        for (Cluster cluster : clustersSegInit.clusterSetValue()) {
            double thr1 = Distance.getThreshold(cluster, features, 0.1, features.getIndexOfEnergy());
            double thr2 = Distance.getThreshold(cluster, features, 0.3, features.getIndexOfEnergy());

            for (Segment segment : cluster) {
                features.setCurrentShow(segment.getShowName());
                for (int i = segment.getStart(); i <= segment.getLast(); i++) {
                    Segment newSegment = segment.clone();
                    newSegment.setStart(i);
                    newSegment.setLength(1);
                    if (features.getFeatureUnsafe(i)[features.getIndexOfEnergy()] > thr2) {
                        clusterS.addSegment(newSegment);
                    }
                    if (features.getFeatureUnsafe(i)[features.getIndexOfEnergy()] < thr1) {
                        clusterNS.addSegment(newSegment);
                    }

                }
            }
        }
        clusterSNS.collapse();

        parameter.getParameterSegmentationOutputFile().setMask(mask + ".sns_base.seg");
        if (parameter.getParameterDiarization().isSaveAllStep()) {
            MainTools.writeClusterSet(parameter, clusterSNS, false);
        }

        ClusterSet previous = clustersSegInit;
        ClusterSet current = clusterSNS;
        int nb = 0;
        GMMArrayList gmmVect = new GMMArrayList();
        while (!current.equals(previous)) {
            previous = current;
            parameter.getParameterModel().setModelKind("DIAG");
            parameter.getParameterModel().setNumberOfComponents(4);
            GMMArrayList gmmInitVect = new GMMArrayList(clusterSNS.clusterGetSize());
            MTrainInit.make(featureSet2, clusterSNS, gmmInitVect, parameter);
            // ** EM training of the initialized GMM
            gmmVect = new GMMArrayList(clusterSNS.clusterGetSize());
            MTrainEM.make(featureSet2, clusterSNS, gmmInitVect, gmmVect, parameter);

            // ** set the penalty to move from the state i to the state j, penalty to move from i to i is equal to 0
            parameter.getParameterDecoder().setDecoderPenalty("10");
            // ** make Viterbi decoding using the 8-GMM set
            // ** one state = one GMM = one speaker = one cluster
            current = MDecode.make(featureSet2, clustersSegInit, gmmVect, parameter);
            parameter.getParameterSegmentationOutputFile().setMask(mask + ".sns_" + nb + ".seg");
            if (parameter.getParameterDiarization().isSaveAllStep()) {
                MainTools.writeClusterSet(parameter, current, true);
            }
            nb++;
        }
        parameter.getParameterFilter().setSegmentPadding(25);
        parameter.getParameterFilter().setSilenceMinimumLength(10);
        parameter.getParameterFilter().setSpeechMinimumLength(100);
        parameter.getParameterSegmentationFilterFile().setClusterFilterName("iT");
        ClusterSet clustersFltClust = SFilter.make(clustersSegInit, current, parameter);
        if (parameter.getParameterDiarization().isSaveAllStep()) {
            parameter.getParameterSegmentationOutputFile().setMask(mask + ".flt.seg");
            MainTools.writeClusterSet(parameter, clustersFltClust, false);
            parameter.getParameterSegmentationOutputFile().setMask(mask);
        }

        // ** segments of more than 20s are split according of silence present in the pms or using a gmm silence detector
        parameter.getParameterSegmentationFilterFile().setClusterFilterName("iT");
        ClusterSet clustersSplitClust = SSplitSeg.make(features, clustersFltClust, gmmVect, current, parameter);
        if (parameter.getParameterDiarization().isSaveAllStep()) {
            parameter.getParameterSegmentationOutputFile().setMask(mask + ".spl.seg");
            MainTools.writeClusterSet(parameter, clustersSplitClust, false);
            parameter.getParameterSegmentationOutputFile().setMask(mask);
        }

        AudioFeatureSet featureSet3 = loadFeature(features, parameter, clustersSplitClust, FeatureFormat
                + ",1:3:2:0:0:0,13,1:1:0:0");
//        mask = parameter.getParameterSegmentationOutputFile().getMask();
        String dir = "media";
        InputStream genderInputStream = Telephone.class.getResourceAsStream(dir + "/gender.gmms");
        GMMArrayList genderVector = MainTools.readGMMContainer(genderInputStream, parameter.getParameterModel());
        parameter.getParameterScore().setByCluster(true);
        parameter.getParameterScore().setGender(true);
        return MScore.make(featureSet3, clustersSplitClust, genderVector, null, parameter);
    }

    private AudioFeatureSet loadFeature(AudioFeatureSet features, Parameter param,
                                        ClusterSet clusters, String desc) throws IOException, DiarizationException {
        param.getParameterInputFeature().setFeaturesDescription(desc);
        return MainTools.readFeatureSet(param, clusters, features);
    }


    public IVectorArrayList getiVectors() {
        return iVectors;
    }

    private GMM readGMM(String ubmPath) throws IOException, DiarizationException {
        GMMArrayList gmms = new GMMArrayList();
        IOFile file = new IOFile(ubmPath, "rb");
        file.open();
        ModelIO.readerGMMContainer(file, gmms);
        file.close();
        return gmms.get(0);
    }


}

