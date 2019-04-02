package com.anshuman.spkdet.mistral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Anshuman
 * Date: 01/08/14
 * Time: 3:15 PM
 */
public class IVMistral {

    private static final Logger logger = LoggerFactory.getLogger(IVMistral.class);
    public static final String WORLD_FILE_NAME = "world_demo";


    /**
     * StreamGobbler is a helperclass grabbed from http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4, which
     * prevents the thread getting stucked (catching stdout und stderr)
     */
    class StreamGobbler extends Thread {
        InputStream is;
        String type;

        StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    buff.append(line).append("\n");
                }
                if (buff.toString().contains("Exception")) {
                    logger.warn(buff.toString());
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }


    /**
     * Extract the features of the stored-before soundfile of a user
     * First thing to do after silence-removal
     *
     * @param user
     */
    public void extractFeatures(User user) {
        String command = Option.mistralPath + "sfbcep " +
                " -m -k 0.97 -p19 -n 24 -r 22 -e -D -A " +
                user.getWavFile() + "  " +
                Option.prmPath + user.getCallerId() + ".tmp.prm ";


        logger.info(command);

        runCommand(command);
    }

    public void normalizeFeatures(User user) {
        String command = Option.mistralPath + "NormFeat " +
                "--config " + Option.mistralConfPath + "NormFeat.cfg " +
                "--inputFeatureFilename " + user.getCallerId() + " " +
                "--featureFilesPath " + Option.prmPath + " " +
                "--labelFilesPath " + Option.lblPath + " " +
                "--debug false --verbose true";

        logger.info(command);
        try {
            Process child = Runtime.getRuntime().exec(command);
            StreamGobbler errorGobbler = new StreamGobbler(child.getErrorStream(), "ERROR");
            StreamGobbler outputGobbler = new StreamGobbler(child.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            child.waitFor();
        } catch (IOException e) {
            logger.warn("Exception", e);
        } catch (InterruptedException e) {
            logger.warn("Exception", e);
        }
    }


    public void normalizeEnergy(User user) {
        String command = Option.mistralPath + "NormFeat " +
                "--config " + Option.mistralConfPath + "NormFeat_energy.cfg " +
                "--inputFeatureFilename " + user.getCallerId() + " " +
                "--featureFilesPath " + Option.prmPath + " ";

        logger.info(command);
        runCommand(command);
    }


    public void detectEnergy(User user) {

        File lbf = new File(Option.lblPath + user.getCallerId() + ".lbl");
        if (lbf.exists())
            lbf.delete();

        String command = Option.mistralPath + "EnergyDetector " +
                "--config " + Option.mistralConfPath + "EnergyDetector.cfg " +
                "--inputFeatureFilename " + user.getCallerId() + " " +
                "--labelFilesPath " + Option.lblPath + " " +
                "--featureFilesPath " + Option.prmPath + " ";

        logger.info(command);
        runCommand(command);

    }


    public void totalVariabilityMatrixEstimation(User user) {

        File idFile = new File(Option.ndxPath + user.getCallerId() + ".ndx");
        try {
            FileWriter fw = new FileWriter(idFile);
            fw.append(user.getCallerId() + "\t\t" + user.getCallerId());
            fw.flush();
            fw.close();
        } catch (IOException ioe) {
            logger.warn("Exception", ioe);
        }

        String command = Option.mistralPath + "TotalVariability " +
                "--config " + Option.mistralConfPath + "TotalVariability_fast.cfg " +
                "--ndxFilename " + idFile.getAbsolutePath() + " " +
                "--featureFilesPath " + Option.prmPath + " " +
                "--labelFilesPath " + Option.lblPath + " " +
                "--mixtureFilesPath " + Option.gmmPath + " " +
                "--matrixFilesPath " + Option.matrixPath + " ";


        logger.info(command);
        runCommand(command);
    }


    public void normalizeIVector(User user) {
        File idFile = new File(Option.lstPath + user.getCallerId() + ".lst");
        try {
            FileWriter fw = new FileWriter(idFile);
            fw.append(user.getCallerId());
            fw.flush();
            fw.close();
        } catch (IOException ioe) {
            logger.warn("Exception", ioe);
        }

        String command = Option.mistralPath + "IvNorm " +
                "--ivNormLoadParam false --ivNormIterationNb 1 --ivNormEfrMode EFR " +
                "--LDA false  --ldaRank 3 --ldaMatrix LDA --loadVectorFilesExtension .y --saveVectorFilesExtension .y " +
                "--saveMatrixFilesExtension .matx  --loadMatrixFilesExtension .matx " +
                " --saveMatrixFormat DB --loadMatrixFormat DB  --ivNormEfrMatrixBaseName EfrMat --ivNormEfrMeanBaseName EfrMean " +
                "--inputVectorFilename " + idFile.getAbsolutePath() + " " +
                "--backgroundNdxFilename " + Option.ndxPath + "trainModel.ndx " + //@todo generate this plda
                "--saveVectorFilesPath " + Option.ivPath + "/lengthNorm/ " +
                "--loadVectorFilesPath " + Option.ivRAWPath + " " +
                "--matrixFilesPath " + Option.matrixPath + " ";

        runCommand(command);

    }


    public void pldaTrain() {
        String command = Option.mistralPath + "PLDA " +
                "--config " + Option.mistralConfPath + "Plda.cfg " +
                "--backgroundNdxFilename " + Option.ndxPath + "trainModel.ndx " + //@todo generate this plda
                "--saveVectorFilesPath " + Option.ivPath + "/lengthNorm/ " +
                "--testVectorFilesPath " + Option.ivPath + "/lengthNorm/ " +
                "--loadVectorFilesPath " + Option.ivRAWPath + " " +
                "--matrixFilesPath " + Option.matrixPath + " ";

        runCommand(command);

    }


    public void generateIVector(User user) {
        File idFile = new File(Option.ndxPath + user.getCallerId() + ".ndx");
        try {
            FileWriter fw = new FileWriter(idFile);
            //Name and File
            fw.append(user.getCallerId() + "\t\t" + user.getCallerId());
            fw.flush();
            fw.close();
        } catch (IOException ioe) {
            logger.warn("Exception", ioe);
        }


        String command = Option.mistralPath + "IvExtractor " +
                "--config " + Option.mistralConfPath + "ivExtractor_fast.cfg " +
                "--targetIdList " + idFile.getAbsolutePath() + " " +
                "--featureFilesPath " + Option.prmPath + " " +
                "--labelFilesPath " + Option.lblPath + " " +
                "--mixtureFilesPath " + Option.gmmPath + " " +
                "--matrixFilesPath " + Option.matrixPath + " " +
                "--saveVectorFilesPath " + Option.ivRAWPath + " ";


        logger.info(command);
        runCommand(command);
    }

    private void runCommand(String command) {
        logger.info(command);
        try {
            Process child = Runtime.getRuntime().exec(command);

            StreamGobbler errorGobbler = new StreamGobbler(child.getErrorStream(), "ERROR");
            StreamGobbler outputGobbler = new StreamGobbler(child.getInputStream(), "OUTPUT");

            errorGobbler.start();
            outputGobbler.start();
            child.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Test the given sound against the user
     *
     * @param users
     * @param sound
     * @return double the likelihood
     */
    public Map<String, Double> getScore(List<User> users, File sound) {
        User test = new User("test", sound.getAbsolutePath());

        this.extractFeatures(test);
        this.normalizeEnergy(test);
        this.detectEnergy(test);
        this.normalizeFeatures(test);
        this.totalVariabilityMatrixEstimation(test);
        this.generateIVector(test);

        File idFile = new File(Option.ndxPath + "users.ndx");
        try {
            FileWriter fw = new FileWriter(idFile);
            fw.write("test");
            for (User user : users) {
                fw.append("\t");
                fw.append(user.getCallerId());
            }
            fw.flush();
            fw.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        File result = null;
        try {
            result = File.createTempFile("userIdent", ".res");
        } catch (IOException e1) {

            e1.printStackTrace();
        }
        String command = Option.mistralPath + "IvTest " +
                "--config " + Option.mistralConfPath + "IvTest.cfg " +
                "--targetIdList " + Option.ndxPath + "trainModel.ndx " + " " +      // trained user space filename
                "--loadVectorFilesPath " + Option.ivRAWPath + " " +
                "--testVectorFilesPath " + Option.ivRAWPath + " " +
                "--matrixFilesPath " + Option.matrixPath + " " +
                "--ndxFilename " + idFile.getAbsolutePath() + " " +        // test file all users  seperated by tab
                "--backgroundNdxFilename " + Option.ndxPath + "Plda.ndx " +  //
                "--outputFilename " + result.getAbsolutePath();
        logger.info(command);
        try {
            Process child = Runtime.getRuntime().exec(command);
            StreamGobbler errorGobbler = new StreamGobbler(child.getErrorStream(), "ERROR");
            StreamGobbler outputGobbler = new StreamGobbler(child.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            child.waitFor();
        } catch (IOException e) {
            logger.warn("Exception", e);
        } catch (InterruptedException e) {
            logger.warn("Exception", e);
        }
        Map<String, Double> rsMap = new HashMap<String, Double>();

        try {
            FileReader resReader = new FileReader(result);
            BufferedReader br = new BufferedReader(resReader);

            String s;
            try {
                while ((s = br.readLine()) != null) {
                    System.out.println("s = " + s);
                    String[] split = s.split(" ");
                    if (split.length == 5) {
                        rsMap.put(split[1], Double.parseDouble(split[4]));
                    }
                }

            } catch (IOException e) {
                logger.warn("Exception", e);

            }
        } catch (FileNotFoundException e) {
            logger.warn("Exception", e);

        } finally {
            if (null != result) result.delete();
        }
        return rsMap;
    }


    public static void main(String[] args) {
        User user = new User("9845404807", "/Users/Apple/java-projs/LIA_RAL/program/pcm/base/9845404807.wav");

        IVMistral mistral = new IVMistral();
        mistral.extractFeatures(user);
        mistral.normalizeEnergy(user);
        mistral.detectEnergy(user);
        mistral.normalizeEnergy(user);
        mistral.totalVariabilityMatrixEstimation(user);
        mistral.generateIVector(user);
        mistral.normalizeIVector(user);
        mistral.pldaTrain();
    }


}
