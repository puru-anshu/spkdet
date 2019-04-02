package com.anshuman.spkdet.voiceid.db;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Identifier {

    private String id = null;
    private static final Logger logger = LoggerFactory.getLogger(Identifier.class);


    public Identifier(String id) {
        this.id = id;

    }


    public String toString() {
        return this.id;
    }

    public Identifier clone() {

        return new Identifier(this.id);

    }

    @Override
    public boolean equals(Object o) {
        return this.toString().equals(o.toString());
    }
}
