package com.anshuman.spkdet.dsp.signal;

import com.anshuman.spkdet.dsp.util.IntArray;

public class ActivityDetector {

    private float thresholdLevel;
    private int minActivityLen;
    private int minSilenceLen;

    private double[] signalEnvelope;
    private int pos;
    private IntArray activeZones;


    public ActivityDetector(float thresholdLevel, int minActivityLen, int minSilenceLen) {
        this.thresholdLevel = thresholdLevel;
        this.minActivityLen = minActivityLen;
        this.minSilenceLen = minSilenceLen;
    }


    public int[] process(double[] signalEnvelope) {
        this.signalEnvelope = signalEnvelope;
        pos = 0;
        activeZones = new IntArray(32);
        int activeStartPos = -1;
        int undefStartPos = -1;
        while (pos < signalEnvelope.length) {
            int segmentStartPos = pos;
            SegmentType segmentType = scanSegment();
            switch (segmentType) {
                case silence: {
                    if (activeStartPos != -1) {
                        addActiveZone(activeStartPos, segmentStartPos);
                        activeStartPos = -1;
                    }
                    undefStartPos = -1;
                    break;
                }
                case active: {
                    if (activeStartPos == -1) {
                        activeStartPos = (undefStartPos != -1) ? undefStartPos : segmentStartPos;
                    }
                    break;
                }
                case undef: {
                    if (undefStartPos == -1) {
                        undefStartPos = segmentStartPos;
                    }
                    break;
                }
                default:
                    throw new AssertionError();
            }
        }
        if (activeStartPos != -1) {
            addActiveZone(activeStartPos, pos);
        }
        return activeZones.toArray();
    }

    private enum SegmentType {active, silence, undef}

    private SegmentType scanSegment() {
        int startPos = pos;
        if (pos >= signalEnvelope.length) {
            throw new AssertionError();
        }
        boolean active = signalEnvelope[pos++] >= thresholdLevel;
        while (pos < signalEnvelope.length && (signalEnvelope[pos] >= thresholdLevel) == active) {
            pos++;
        }
        int minLen = active ? minActivityLen : minSilenceLen;
        if (pos - startPos < minLen) {
            return SegmentType.undef;
        }
        return active ? SegmentType.active : SegmentType.silence;
    }

    private void addActiveZone(int startPos, int endPos) {
        activeZones.add(startPos);
        activeZones.add(endPos);
    }

}
