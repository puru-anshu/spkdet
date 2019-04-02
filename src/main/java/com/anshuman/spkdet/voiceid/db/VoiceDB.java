package com.anshuman.spkdet.voiceid.db;


import com.anshuman.spkdet.voiceid.utils.Scores;

import java.util.List;


public interface VoiceDB {





	public  boolean readDb() throws Exception;


	public  boolean addModel(Sample sample, Identifier identifier);


	public  boolean removeModel();


	public Scores matchVoice(Sample sample, Identifier identifier);


	public  Scores voiceLookup(Sample sample);

    public Scores voiceLookup(Sample sample, List<String> list);






}
