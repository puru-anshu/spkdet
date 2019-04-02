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
public class Mistral {

    private static final Logger logger = LoggerFactory.getLogger(Mistral.class);
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
     * Trains a user in mistral
     *
     * @param user
     */
    public void trainUser(User user) {
        File idFile = new File(Option.ndxPath + "demo.ndx");
        try {
            FileWriter fw = new FileWriter(idFile);
            fw.append(user.getCallerId() + "\t\t" + user.getCallerId());
            fw.flush();
            fw.close();
        } catch (IOException ioe) {
            logger.warn("Exception", ioe);
        }

        String command = Option.mistralPath + "TrainTarget " +
                "--config " + Option.mistralConfPath + "TrainTarget.cfg " +
                "--featureFilesPath " + Option.prmPath + " " +
                "--mixtureFilesPath " + Option.gmmPath + " " +
                "--targetIdList " + idFile + " " +
                "--inputWorldFilename " + WORLD_FILE_NAME + " ";
        logger.debug(command);

        try {
            runCommand(command);
        } catch (IOException e) {
            logger.warn("Exception", e);
        } catch (InterruptedException e) {
            logger.warn("Exception", e);
        }
    }


    public void trainWorld(List<User> users) {
        try {
            FileWriter fw = new FileWriter(Option.lstPath + WORLD_FILE_NAME + "list.lst");

            for (User user : users) {
                logger.debug(user.getCallerId());
                fw.append(user.getCallerId() + "\r\n");
            }
            fw.flush();
            fw.close();

            File worldList = new File(Option.lstPath + WORLD_FILE_NAME + "list.lst");

            String command = Option.mistralPath + "TrainWorld " +
                    "--config " + Option.mistralConfPath + "TrainWorld.cfg " +
                    "--inputFeatureFilename " + worldList.getAbsolutePath() + " " +
                    "--featureFilesPath " + Option.prmPath + " " +
                    "--mixtureFilesPath " + Option.gmmPath + " " +
                    "--labelFilesPath " + Option.lblPath + " " +
                    "--outputWorldFilename " + WORLD_FILE_NAME;
            logger.info(command);
            runCommand(command);
        } catch (IOException e) {
            logger.warn("Exception", e);
        } catch (InterruptedException e) {
            logger.warn("Exception", e);
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

        try {
            runCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
            runCommand(command);
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
        try {
            runCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void detectEnergy(User user) {
        File lbl = new File(Option.lblPath + user + ".lbl");
        if (lbl.exists())
            lbl.delete();

        String command = Option.mistralPath + "EnergyDetector " +
                "--config " + Option.mistralConfPath + "EnergyDetector.cfg " +
                "--inputFeatureFilename " + user.getCallerId() + " " +
                "--labelFilesPath " + Option.lblPath + " " +
                "--featureFilesPath " + Option.prmPath + " ";

        logger.info(command);
        try {
            runCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void totalVariabilityMatrixEstimation(User user) {

        File idFile = new File(Option.ndxPath + "demo.ndx");
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
        try {
            runCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void generateIVector(User user) {
        File idFile = new File(Option.ndxPath + "demo.ndx");
        try {
            FileWriter fw = new FileWriter(idFile);
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
        try {
            runCommand(command);
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

        File idFile = new File(Option.ndxPath + "users.ndx");
        try {
            FileWriter fw = new FileWriter(idFile);
            for (User user : users) {
                fw.write("test" + "\t");
                fw.append(user.getCallerId()).append("\n");
                fw.flush();

            }
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


        try {


            String command = Option.mistralPath + "ComputeTest " +
                    "--config " + Option.mistralConfPath + "ComputeTestTNorm.cfg " +
                    "--featureFilesPath " + Option.prmPath + " " +
                    "--mixtureFilesPath " + Option.gmmPath + " " +
                    "--labelFilesPath " + Option.lblPath + " " +
                    "--ndxFilename " + idFile.getAbsolutePath() + "  " +  // Impostor ndx
                    "--inputWorldFilename " + WORLD_FILE_NAME + " " +
                    "--outputFilename " + result.getAbsolutePath();

            runCommand(command);


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

    private void runCommand(String command) throws IOException, InterruptedException {
        Process child = Runtime.getRuntime().exec(command);
        StreamGobbler errorGobbler = new StreamGobbler(child.getErrorStream(), "ERROR");
        StreamGobbler outputGobbler = new StreamGobbler(child.getInputStream(), "OUTPUT");
        errorGobbler.start();
        outputGobbler.start();
        child.waitFor();
    }


    public void computeNorm(File testFile, File impostorFile) {

    }


}
