package com.anshuman.spkdet.lium;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libMatrix.MatrixIO;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.FullGaussian;
import fr.lium.spkDiarization.libModel.ivector.IVector;
import fr.lium.spkDiarization.libModel.ivector.IVectorArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.*;

/**
 * User: Anshuman
 * Date: 02/09/14
 * Time: 11:20 AM
 */
public class IVSpeakerVerifier {
    public static final String EFR_FILE = "/Users/Apple/java-projs/voxforge/out/ubm.gmm.efr.iv";
    public static final String MAH_FILE = "/Users/Apple/java-projs/voxforge/mat/ubm.mahanalobis.mat";

    private static IVSpeakerVerifier ourInstance = null;
    private static final String DIR = "./iv/";
    private MatrixSymmetric covariance;


    public static IVSpeakerVerifier getInstance() {
        if (ourInstance == null)
            ourInstance = new IVSpeakerVerifier();

        return ourInstance;
    }

    private IVSpeakerVerifier() {


        try {

            if (new File(MAH_FILE).exists()) {
                covariance = MatrixIO.readMatrixSymmetric(MAH_FILE, false);

            } else {
                IVectorArrayList speakerList = IVectorArrayList.loadIVector(EFR_FILE);
                covariance = makeMeanOfSpeakerCovariance(speakerList);
                MatrixIO.writeMatrix(covariance, MAH_FILE, false);

            }


            covariance = covariance.invert();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MatrixSymmetric makeMeanOfSpeakerCovariance(IVectorArrayList iVectorList) throws DiarizationException, FileNotFoundException {
        if (iVectorList.size() == 0) {
            return null;
        }

        int dimmension = iVectorList.get(0).getDimension();
        MatrixSymmetric meanCov = new MatrixSymmetric(dimmension);
        meanCov.fill(0.0);

        Set<String> speakerIDList = iVectorList.getSpeakerIDList();
        System.out.println("speakerIDList.size() = " + speakerIDList.size());

        FullGaussian fg = new FullGaussian(dimmension);
        fg.statistic_initialize();
        fg.setName("wld");

        int ct = 0;
        for (IVector iVector : iVectorList) {
            if (ct % 3 == 0)
                fg.statistic_addFeature(iVector.getData(), 1.0);
            ct++;

        }


        int count = 0;
        fg.setModel();
        System.out.println("fg.statistic_getCount() = " + fg.statistic_getCount());
        if (fg.statistic_getCount() >= 3) {
            count++;
            fg.statistic_setMeanAndCovariance();
            for (int i = 0; i < dimmension; i++) {
                for (int j = i; j < dimmension; j++) {
                    meanCov.set(i, j, meanCov.get(i, j) + fg.getCovariance(i, j));
                }
            }
            System.out.println("add " + fg.getName() + ", to few data, count:" + fg.getCount());
        } else {
            System.err.println("reject " + fg.getName() + ", to few data, count:" + fg.getCount());
        }
        fg.statistic_reset();
        System.out.println("make mean of covariance");
        for (int i = 0; i < dimmension; i++) {
            for (int j = i; j < dimmension; j++) {
                meanCov.set(i, j, meanCov.get(i, j) / count);
            }
        }

        return meanCov;
    }

    public void trainUser(String wavPath, String userId) throws Exception {
        Audio audio = new Audio(wavPath);
        audio.analyze();
        IVectorArrayList iVectors = audio.getIVectors();
        String outFile = DIR + userId + ".iv";
        IVectorArrayList.writeIVector(outFile, iVectors);


    }

    public Map<Double, String> scoreUser(String wavPath, List<String> userList) throws Exception {
        Map<Double, String> resultMap = new TreeMap<Double, String>();
        Audio audio = new Audio(wavPath);
        audio.analyze();
        IVectorArrayList testVector = audio.getIVectors();
        for (String userId : userList) {
            String inFile = DIR + userId + ".iv";
            if (new File(inFile).exists()) {
                IVectorArrayList baseVectors = IVectorArrayList.loadIVector(inFile);
                double dis = Distance.iVectorMahalanobis(testVector.get(0), baseVectors.get(0), covariance);
                resultMap.put(dis, userId);
            }

        }
        return resultMap;
    }

    public static void main(String[] args) {

        IVSpeakerVerifier verifier = IVSpeakerVerifier.getInstance();
        boolean build = false;
        if (build) {
            File baseDir = new File("/Users/Apple/wave-samples/tel/base/");
            for (File file : baseDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.endsWith("wav");
                }
            })) {
                try {
                    String userId = file.getName().replace(".wav", "");
                    verifier.trainUser(file.getPath(), userId);

                } catch (Exception e) {
                    System.err.println(file.getName() + " " + e.getMessage());
                }
            }

        }
        String[] users = {"7259421709", "9164742218", "9591716591",
                "9611552333", "9740199766", "9845205601", "9845404807",
                "9916329079", "9916796519",
                "7795196151", "9590884170", "9611123425", "9620710549",
                "9845153342", "9845394354", "9886810661", "9916745027", "9986656219",};

        List<String> userList = Arrays.asList(users);


        int sCount = 0;
        File testDir = new File("/Users/Apple/wave-samples/tel/2014-07-13/done");
        for (File file : testDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith("wav");
            }
        })) {
            try {


                Map<Double, String> res = verifier.scoreUser(file.getPath(), userList);

                if (res.size() > 0) {
                    Double val = res.keySet().iterator().next();
                    String key = res.get(val);
                    double d = val;
                    if (file.getName().startsWith(key)) {
                        sCount++;
                    }
                }
                System.out.println(file.getName() + " :: " + res);

            } catch (Exception e) {
                System.err.println(file.getName() + " " + e.getMessage());
            }
        }

        System.out.println("sCount = " + sCount);


    }
}
