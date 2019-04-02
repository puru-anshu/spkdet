
package com.anshuman.spkdet.voiceid.fm;


import com.anshuman.spkdet.voiceid.db.Sample;
import com.anshuman.spkdet.voiceid.utils.Utils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;


public class WavSample extends Sample {


	public WavSample(File resource) throws IOException,
			UnsupportedAudioFileException {
		super(resource);
		Utils.isGoodWave(resource);

    }


	public WavSample(Sample sample) throws IOException,
			UnsupportedAudioFileException {
		this(sample.getResource());
	}


	public File toWav() {

		return this.resource;
	}




	public double getDuration() throws UnsupportedAudioFileException,
			IOException, LineUnavailableException {
		AudioInputStream stream;
		stream = AudioSystem.getAudioInputStream(this.resource);
		AudioFileFormat fileFormat = AudioSystem
				.getAudioFileFormat(this.resource);
		AudioFormat format = fileFormat.getFormat();
		DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat(),
				((int) stream.getFrameLength() * format.getFrameSize()));
		Clip clip = (Clip) AudioSystem.getLine(info);
		clip.close();
		return clip.getBufferSize()
				/ (clip.getFormat().getFrameSize() * clip.getFormat()
						.getFrameRate());
	}

}
