
package com.anshuman.spkdet.voiceid.fm;


import fr.lium.spkDiarization.libClusteringData.Cluster;

import java.io.File;
import java.util.ArrayList;


public interface Diarizator {


	public ArrayList<Cluster> extractClusters(File input);

}
