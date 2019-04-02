package com.anshuman.spkdet.tools;

import com.ubona.mistral.AudioUtils;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.programs.*;
import fr.lium.spkDiarization.system.Telephone;
import fr.lium.spkDiarization.tools.SFilter;
import fr.lium.spkDiarization.tools.SSplitSeg;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * User: Anshuman
 * Date: 10/09/14
 * Time: 11:23 AM
 */
public class SegmentationUtil {

    public static final String fDesc = "audio8kHz2sphinx,1:1:0:0:0:0,13,0:0:0";

    public static final String outDesc = "sphinx,1:1:0:0:0:0,13,0:0:0";
    public static File UBM_SEG = new File("/tmp/ubm.seg");

    public static void main(String[] args) throws IOException {

        String dir = "/Users/Apple/java-projs/voxforge/extracted";
        String mfccDir = new File(dir).getParent() + File.separator + "mfcc";
        String labelDir = new File(dir).getParent() + File.separator + "lbl";

        if (!new File(mfccDir).exists()) {
            new File(mfccDir).mkdir();
        }
        if (!new File(labelDir).exists()) {
            new File(labelDir).mkdir();
        }


        System.out.println("Reading dir = " + dir);

        Collection<File> files = FileUtils.listFiles(new File(dir), new String[]{"wav"}, true);
        for (File wavFile : files) {
            genSegmentAndMfcc(wavFile, mfccDir, labelDir);

        }

        System.out.println("files = " + files.size());

    }

    private static AudioFeatureSet loadFeature(AudioFeatureSet features, Parameter param,
                                               ClusterSet clusters, String desc) throws IOException, DiarizationException {
        param.getParameterInputFeature().setFeaturesDescription(desc);
        return MainTools.readFeatureSet(param, clusters, features);
    }


    private static ClusterSet telephone(Parameter parameter) throws Exception {
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

        String FeatureFormat = "featureSetTransformation";
        AudioFeatureSet featureSet2 = loadFeature(features, parameter, clustersSegInit, FeatureFormat
                + ",3:1:0:0:0:0,13,0:0:0:0");

        ClusterSet clusterSNS = new ClusterSet();
        Cluster clusterS = clusterSNS.createANewCluster("f2");
        Cluster clusterNS = clusterSNS.createANewCluster("iT");

        for (Cluster cluster : clustersSegInit.clusterSetValue()) {
            double thr1 = Distance.getThreshold(cluster, features, 0.1, features.getIndexOfEnergy());
            double thr2 = Distance.getThreshold(cluster, features, 0.3, features.getIndexOfEnergy());

            for (Segment segment : cluster) {
                features.setCurrentShow(segment.getShowName());
                for (int i = segment.getStart(); i <= segment.getLast(); i++) {
                    Segment newSegment = (Segment) segment.clone();
                    newSegment.setStart(i);
                    newSegment.setLength(1);
                    if (features.getFeature(parameter.show, i)[features.getIndexOfEnergy()] > thr2) {
                        clusterS.addSegment(newSegment);
                    }
                    if (features.getFeature(parameter.show, i)[features.getIndexOfEnergy()] < thr1) {
                        clusterNS.addSegment(newSegment);
                    }

                }
            }
        }
        clusterSNS.collapse();

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

            nb++;
        }

        parameter.getParameterFilter().setSegmentPadding(25);
        parameter.getParameterFilter().setSilenceMinimumLength(10);
        parameter.getParameterFilter().setSpeechMinimumLength(100);
        parameter.getParameterSegmentationFilterFile().setClusterFilterName("iT");
        ClusterSet clustersFltClust = SFilter.make(clustersSegInit, current, parameter);
        // ** segments of more than 20s are split according of silence present in the pms or using a gmm silence detector
        parameter.getParameterSegmentationFilterFile().setClusterFilterName("iT");
        ClusterSet clustersSplitClust = SSplitSeg.make(features, clustersFltClust, gmmVect, current, parameter);

        AudioFeatureSet featureSet3 = loadFeature(features, parameter, clustersSplitClust, FeatureFormat
                + ",1:3:2:0:0:0,13,1:1:0:0");

        String dir = "media";
        InputStream genderInputStream = Telephone.class.getResourceAsStream(dir + "/gender.gmms");
        GMMArrayList genderVector = MainTools.readGMMContainer(genderInputStream, parameter.getParameterModel());
        parameter.getParameterScore().setByCluster(true);
        parameter.getParameterScore().setGender(true);
        ClusterSet clustersGender = MScore.make(featureSet3, clustersSplitClust, genderVector, null, parameter);

        featureSet3.getFeature(parameter.show, 0);
        MainTools.writeFeatureSetAs(parameter.show, parameter, featureSet3);

        return clustersGender;


    }


    private static void genSegmentAndMfcc(File wavFile, String mfccDir, String labelDir) {
        File speechFile = AudioUtils.getSpeechWav(wavFile);
        try {

            String baseName = FilenameUtils.getBaseName(wavFile.getName());
            String userName = FilenameUtils.getBaseName(wavFile.getParentFile().getParent());
            baseName = userName + "_" + baseName;

            Parameter parameter = new Parameter();
            parameter.show = baseName;
            parameter.getParameterInputFeature().setFeaturesDescription(fDesc);
            parameter.getParameterDiarization().setCEClustering(true);
            parameter.getParameterInputFeature().setFeatureMask(speechFile.getAbsolutePath());
            parameter.getParameterSegmentationOutputFile().setMask(labelDir +
                    File.separator + baseName + ".seg");
            String mfccFile = mfccDir + File.separator + baseName;
            parameter.getParameterOutputFeature().setFeatureMask(mfccFile + ".mfcc");
            parameter.getParameterOutputFeature().setFeaturesDescription(outDesc);
            ClusterSet clusterSet = telephone(parameter);



            MainTools.writeClusterSet(parameter, clusterSet);



            String gender = clusterSet.getFirstCluster().getGender();
            int i = 0;
            for (Segment segment : clusterSet.getSegments()) {
                String data = String.format("%s %s %d %d %s %s %s %s\n", mfccFile, segment.getChannel(), segment.getStart(),
                        segment.getLength(), gender, segment.getBandwidth(), segment.getEnvironement(), userName.split("-")[0]);

                FileUtils.writeStringToFile(UBM_SEG, data, true);
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != speechFile) {
                speechFile.delete();
            }
        }
    }
}
