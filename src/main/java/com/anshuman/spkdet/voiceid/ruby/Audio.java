package com.anshuman.spkdet.voiceid.ruby;


import com.ubona.mistral.AudioUtils;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.ModelIO;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.programs.MSegInit;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.programs.MTrainMAP;
import fr.lium.spkDiarization.system.Telephone;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * User: Anshuman
 * Date: 15/07/14
 * Time: 5:53 AM
 */
public class Audio {

    public static File UBM_FILE = new File("./store/ubm.gmm");


    private File path;
    private String identifier;
    private ClusterSet clusters;
    private AudioFeatureSet audioFeatureSet = null;
    private GMMArrayList gmmList = null;
    private Parameter parameter = null;


    public Audio(String path) throws Exception {

        this.path = new File(path);
        this.path = AudioUtils.getSpeechWav(new File(path));
        if (!this.path.exists()) {
            System.err.println("Something wrong");
        }
        this.identifier = path.split("\\.(?=[^\\.]+$)")[0];

//        System.out.println("path = " + identifier);

    }

    public void analyze(boolean trainSpeakerModel) throws Exception {
        try {
            parameter = new Parameter();
            parameter.show = identifier;
            parameter.getParameterInputFeature().setFeaturesDescription("audio8kHz2sphinx,1:1:0:0:0:0,13,0:0:0:0");
            parameter.getParameterDiarization().setCEClustering(true);
            parameter.getParameterInputFeature().setFeatureMask(path.getAbsolutePath());
            clusters = telephone(parameter);    // ester2 in the case of not telephone
//            printCluster(clusters);

            if (trainSpeakerModel)
                gmmList = trainSpeakerModel();
        } finally {
            if (this.path.getPath().startsWith("/tmp")) {
                this.path.delete();
            }
        }

    }


    public ClusterSet getClusters() {
        return clusters;
    }


    public AudioFeatureSet getFeatureSet() {
        return audioFeatureSet;
    }

    public GMMArrayList trainSpeakerModel() throws Exception {
        if (clusters == null) {
            System.err.println("Call analyze feature");
            return null;
        }
        Parameter parameter = new Parameter();
        parameter.getParameterInputFeature().setFeaturesDescription("audio8kHz2sphinx,1:3:2:0:0:0,13,1:1:300:4");
        parameter.getParameterInputFeature().setFeatureMask(path.getAbsolutePath());
        parameter.getParameterInitializationEM().setModelInitMethod("copy");
        parameter.getParameterModelSetInputFile().setMask(UBM_FILE.getAbsolutePath());
        audioFeatureSet = MainTools.readFeatureSet(parameter, clusters);
        GMMArrayList initList = new GMMArrayList();
        MTrainInit.make(audioFeatureSet, clusters, initList, parameter);

        parameter = new Parameter();
        parameter.getParameterInputFeature().setFeaturesDescription("audio8kHz2sphinx,1:3:2:0:0:0,13,1:1:300:4");
        parameter.getParameterInputFeature().setFeatureMask(path.getAbsolutePath());
        parameter.getParameterEM().setEMControl("1,20,0.01");
        parameter.getParameterVarianceControl().setVarianceControl("0.01,10.0");
        parameter.show = identifier;
        audioFeatureSet.setCurrentShow(parameter.show);
        GMMArrayList gmmVect = new GMMArrayList();
        MTrainMAP.make(audioFeatureSet, clusters, initList, gmmVect, parameter, true);


        return gmmVect;

    }

    public GMMArrayList getTopGaussian() throws Exception {

        GMMArrayList gmmList = new GMMArrayList();
        if (this.gmmList.size() > 0) {
            this.gmmList.get(0).sortComponents();
            gmmList.add(this.gmmList.get(0));
        }
        audioFeatureSet.setUbmList(gmmList);
        return gmmList;

    }


    private ClusterSet telephone(Parameter parameter) throws Exception {
        ClusterSet clusters = null;
        clusters = new ClusterSet();
        Cluster clusterInit = clusters.createANewCluster("init");
        Segment segmentInit = new Segment(parameter.show, 0, 1, clusterInit, 8000f);
        clusterInit.addSegment(segmentInit);
        AudioFeatureSet features = Telephone.loadFeature(parameter, clusters,
                parameter.getParameterInputFeature().getFeaturesDescriptorAsString());

        features.setCurrentShow(parameter.show);

        int nbFeatures = features.getNumberOfFeatures();
        if (!parameter.getParameterDiarization().isLoadInputSegmentation()) {
            clusters.getFirstCluster().firstSegment().setLength(nbFeatures);
        }

        // ** check the quality of the MFCC, remove similar consecutive MFCC of the featureSet
        ClusterSet clustersSegInit = new ClusterSet();
        MSegInit.make(features, clusters, clustersSegInit, parameter);
        clustersSegInit.collapse();

        return clustersSegInit;
//        String FeatureFormat = "featureSetTransformation";
//        AudioFeatureSet featureSet2 = loadFeature(features, parameter, clustersSegInit, FeatureFormat
//                + ",3:1:0:0:0:0,13,0:0:0:0");
//
//        ClusterSet clusterSNS = new ClusterSet();
//        Cluster clusterS = clusterSNS.createANewCluster("f2");
//        Cluster clusterNS = clusterSNS.createANewCluster("iT");
//
//        for (Cluster cluster : clustersSegInit.clusterSetValue()) {
//            double thr1 = Distance.getThreshold(cluster, features, 0.1, features.getIndexOfEnergy());
//            double thr2 = Distance.getThreshold(cluster, features, 0.3, features.getIndexOfEnergy());
//
//            for (Segment segment : cluster) {
//                features.setCurrentShow(segment.getShowName());
//                for (int i = segment.getStart(); i <= segment.getLast(); i++) {
//                    Segment newSegment = (Segment) segment.clone();
//                    newSegment.setStart(i);
//                    newSegment.setLength(1);
//                    if (features.getFeature(parameter.show, i)[features.getIndexOfEnergy()] > thr2) {
//                        clusterS.addSegment(newSegment);
//                    }
//                    if (features.getFeature(parameter.show, i)[features.getIndexOfEnergy()] < thr1) {
//                        clusterNS.addSegment(newSegment);
//                    }
//
//                }
//            }
//        }
//        clusterSNS.collapse();
//
//        ClusterSet previous = clustersSegInit;
//        ClusterSet current = clusterSNS;
//        int nb = 0;
//        GMMArrayList gmmVect = new GMMArrayList();
//        while (!current.equals(previous)) {
//            previous = current;
//            parameter.getParameterModel().setModelKind("DIAG");
//            parameter.getParameterModel().setNumberOfComponents(4);
//            GMMArrayList gmmInitVect = new GMMArrayList(clusterSNS.clusterGetSize());
//            MTrainInit.make(featureSet2, clusterSNS, gmmInitVect, parameter);
//            // ** EM training of the initialized GMM
//            gmmVect = new GMMArrayList(clusterSNS.clusterGetSize());
//            MTrainEM.make(featureSet2, clusterSNS, gmmInitVect, gmmVect, parameter);
//
//            // ** set the penalty to move from the state i to the state j, penalty to move from i to i is equal to 0
//            parameter.getParameterDecoder().setDecoderPenalty("10");
//            // ** make Viterbi decoding using the 8-GMM set
//            // ** one state = one GMM = one speaker = one cluster
//            current = MDecode.make(featureSet2, clustersSegInit, gmmVect, parameter);
//
//            nb++;
//        }
//
//        parameter.getParameterFilter().setSegmentPadding(25);
//        parameter.getParameterFilter().setSilenceMinimumLength(10);
//        parameter.getParameterFilter().setSpeechMinimumLength(100);
//        parameter.getParameterSegmentationFilterFile().setClusterFilterName("iT");
//        ClusterSet clustersFltClust = SFilter.make(clustersSegInit, current, parameter);
//        // ** segments of more than 20s are split according of silence present in the pms or using a gmm silence detector
//        parameter.getParameterSegmentationFilterFile().setClusterFilterName("iT");
//        ClusterSet clustersSplitClust = SSplitSeg.make(features, clustersFltClust, gmmVect, current, parameter);
//
//        AudioFeatureSet featureSet3 = loadFeature(features, parameter, clustersSplitClust, FeatureFormat
//                + ",1:3:2:0:0:0,13,1:1:0:0");
//
//        String dir = "media";
//        InputStream genderInputStream = Telephone.class.getResourceAsStream(dir + "/gender.gmms");
//        GMMArrayList genderVector = MainTools.readGMMContainer(genderInputStream, parameter.getParameterModel());
//        parameter.getParameterScore().setByCluster(true);
//        parameter.getParameterScore().setGender(true);
//        ClusterSet clustersGender = MScore.make(featureSet3, clustersSplitClust, genderVector, null, parameter);
//        return clustersGender;


    }


    private AudioFeatureSet loadFeature(AudioFeatureSet features, Parameter param,
                                        ClusterSet clusters, String desc) throws IOException, DiarizationException {
        param.getParameterInputFeature().setFeaturesDescription(desc);
        return MainTools.readFeatureSet(param, clusters, features);
    }

    public GMMArrayList getGmmList() {
        return gmmList;
    }

    public static void main(String[] args) {
        File testDir = new File("/Users/Apple/wave-samples/tel/2014-07-13/");
        for (File file : testDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith("wav");
            }
        })) {
            try {
                Audio audio = new Audio(file.getPath());
                audio.analyze(true);

            } catch (Exception e) {
                System.err.println(file.getName() + " " + e.getMessage());
            }

        }

    }

    private static GMM readModel(String modelPath) throws IOException, DiarizationException {
        GMMArrayList gmms = new GMMArrayList();
        IOFile file = new IOFile(modelPath, "rb");
        file.open();
        ModelIO.readerGMMContainer(file, gmms);
        file.close();


        GMM gaussians = gmms.get(0);
        System.out.println("gaussians = " + gaussians.getGender());
        System.out.println("gaussians = " + gaussians.getNbOfComponents());


        return gmms.get(0);
    }

    public static GMM getUBM() throws IOException, DiarizationException {
        return readModel(UBM_FILE.getAbsolutePath());
    }
}
