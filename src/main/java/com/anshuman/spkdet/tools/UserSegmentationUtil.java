package com.anshuman.spkdet.tools;

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
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * User: Anshuman
 * Date: 10/09/14
 * Time: 11:23 AM
 */
public class UserSegmentationUtil {

    public static final String fDesc = "audio8kHz2sphinx,1:1:0:0:0:0,13,0:0:0";

    public static final String outDesc = "sphinx,1:1:0:0:0:0,13,0:0:0";
    public static File UBM_SEG = new File("/tmp/ubm.seg");
    public static File WLD_SEG = new File("/tmp/wld.seg");
    static String mfccDir;
    static String labelDir;

    public static void main(String[] args) throws IOException {

//        String dir = args[0];
        String dir = "/Users/Apple/java-projs/voxforge/extracted";
        mfccDir = new File(dir).getParent() + File.separator + "mfcc_ivr";
        labelDir = new File(dir).getParent() + File.separator + "lbl_ivr";

        if (!new File(mfccDir).exists()) {
            new File(mfccDir).mkdir();
        }
        if (!new File(labelDir).exists()) {
            new File(labelDir).mkdir();
        }


        System.out.println("Reading dir = " + dir);
        IOFileFilter fileFilter = FileFilterUtils.nameFileFilter("README");
        IOFileFilter dirFilter = FileFilterUtils.directoryFileFilter();

        Collection<File> files = FileUtils.listFiles(new File(dir), fileFilter, dirFilter);
        for (File rMe : files) {
            readMe(rMe);
        }


        System.out.println("files = " + files.size());

    }

    private static void readMe(File rMe) throws IOException {

        String parentDir = rMe.getParentFile().getParent();
        String baseName = FilenameUtils.getBaseName(parentDir);
//        System.out.println("parentDir = " + baseName);
        if (!baseName.startsWith("voxforgeivr")) {
            return;
        }


        LineIterator lineIterator = FileUtils.lineIterator(rMe);
        String user = "", gender = "", micType = "";
        while (lineIterator.hasNext()) {
            String s = lineIterator.nextLine();
            if (s.startsWith("User Name")) {
                user = s.split(":")[1].trim();
            } else if (s.startsWith("Gender")) {
                gender = s.split(":")[1].trim().toLowerCase();
            } else if (s.startsWith("Microphone type")) {
                micType = s.split(":")[1].trim();
            } else if (s.startsWith("Sampling Rate")) {
                micType += s.split(":")[1].trim();
            }

        }

        lineIterator.close();
        Collection<File> wavFile = FileUtils.listFiles(new File(parentDir), new String[]{"wav"}, true);
        for (File w : wavFile) {
            genSegmentAndMfcc(w, mfccDir, labelDir, gender);
        }


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


    private static void genSegmentAndMfcc(File wavFile, String mfccDir, String labelDir, String gender) {
        File speechFile = wavFile;//AudioUtils.getSpeechWav(wavFile);
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
            if (clusterSet.getLength() == 0) {
                System.out.println("Check this file  " + wavFile);
                return;
            }
            String g = clusterSet.getFirstCluster().getGender();
            boolean male = gender.startsWith("male");
            System.out.println("g = " + g + " original " + gender);
            if (male) {
                clusterSet.getFirstCluster().setGender("M");

            } else {
                clusterSet.getFirstCluster().setGender("F");
            }
            //MainTools.writeClusterSet(parameter, clusterSet);


            int i = 0;
            for (Segment segment : clusterSet.getSegments()) {
                String data = String.format("%s %s %d %d %s %s %s %s\n", mfccFile, segment.getChannel(), segment.getStart(),
                        segment.getLength(), male ? "M" : "F", segment.getBandwidth(), segment.getEnvironement(), male ? "MS" : "FS");


                String ordate = String.format("%s %s %d %d %s %s %s %s\n", mfccFile, segment.getChannel(), segment.getStart(),
                        segment.getLength(), male ? "M" : "F", segment.getBandwidth(), segment.getEnvironement(), userName + "S" + i);


                FileUtils.writeStringToFile(WLD_SEG, ordate, true);
                FileUtils.writeStringToFile(UBM_SEG, data, true);
                i++;
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            if (null != speechFile) {
//                speechFile.delete();
//            }
        }
    }
}
