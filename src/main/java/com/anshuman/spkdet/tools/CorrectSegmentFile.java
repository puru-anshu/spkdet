package com.anshuman.spkdet.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * User: Anshuman
 * Date: 10/09/14
 * Time: 8:16 PM
 */
public class CorrectSegmentFile {


    public static void main(String[] args) {
        try {
            LineIterator lineIterator = FileUtils.lineIterator(new File("/Users/Apple/java-projs/voxforge/wld.seg"));
            while (lineIterator.hasNext()) {
                String next = lineIterator.next();
                String[] split = next.split(" ");
                int len = split.length;
                String last = split[len - 1];
                String lastR = last.split("-")[0];
                split[len - 1] = lastR;
                String s = Arrays.toString(split);
                System.out.println(s);


            }
        } catch (IOException e) {

        }
    }
}
