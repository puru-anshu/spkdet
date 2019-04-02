package com.anshuman.spkdet.tools;

import fr.lium.spkDiarization.libMatrix.MatrixIO;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.libModel.ivector.IVector;
import fr.lium.spkDiarization.libModel.ivector.IVectorArrayList;
import fr.lium.spkDiarization.programs.ivector.ComputeMahanalobisCovariance;

/**
 * User: Anshuman
 * Date: 11/09/14
 * Time: 4:10 PM
 */
public class ComputeMahCovariance {

    public static void main(String[] args) throws Exception {


        String ivFile = "/Users/Apple/java-projs/voxforge/out/ubm.gmm.efr.iv";
        IVectorArrayList list = IVectorArrayList.loadIVector(ivFile);
        IVectorArrayList newList = new IVectorArrayList();
        int ct =0 ;
        for (IVector vt : list) {
            String name = vt.getName();
            String speakerID = vt.getSpeakerID();
            char c = speakerID.charAt(0);
//            if (Character.isUnicodeIdentifierStart(c)) {
                name = speakerID.split("-")[0];
                IVector v = new IVector(vt.getData(), name, vt.getGender());
                v.setSpeakerID(name);
                newList.add(v);
//            }

//           if(ct++ > 200)
//                break;

        }
        list.clear();


        System.out.println("newList = " + newList.getSpeakerIDList().size());
        MatrixSymmetric matrixSymmetric = ComputeMahanalobisCovariance.makeMeanOfSpeakerCovariance(newList);
        MatrixIO.writeMatrix(matrixSymmetric,"/tmp/cov",false);




    }
}
