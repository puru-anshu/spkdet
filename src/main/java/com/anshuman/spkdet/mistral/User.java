package com.anshuman.spkdet.mistral;/**
 * 
 * User: Anshuman
 * Date: 01/08/14
 * Time: 3:16 PM
 * 
 */
public class User {

    private String callerId ;
    private String wavFile ;
    public User(String callerId, String wavFile)
    {
        this.callerId=callerId;
        this.wavFile=wavFile;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public String getWavFile() {
        return wavFile;
    }

    public void setWavFile(String wavFile) {
        this.wavFile = wavFile;
    }

    @Override
    public String toString() {
        return callerId;
    }
}
