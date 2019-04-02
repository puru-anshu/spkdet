
package com.anshuman.spkdet.voiceid.db;

import java.io.File;
import java.io.IOException;


public abstract class AbstractFileVoiceModel extends File implements VoiceModel {

	private static final long serialVersionUID = -1025215276685470064L;
	protected Identifier identifier;
	private File model;



	public AbstractFileVoiceModel(String path, Identifier id) throws Exception {
		super(path);
		if (!this.exists())
			throw new IOException("No such file " + path);
		if (!this.isFile())
			throw new IOException(path + " is not a regular file");
		setModel(new File(path));
		identifier = id;
	}

	public Identifier getIdentifier() {
		return identifier;
	}


	public File getModel() {
		return model;
	}


	private void setModel(File model) {
		this.model = model;
	}

}
