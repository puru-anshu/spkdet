package com.anshuman.spkdet.dsp.swing;


import com.anshuman.spkdet.dsp.sound.AudioIo;

import javax.swing.*;
import java.awt.*;

/**
 * 
 * User: Anshuman
 * Date: 12/07/14
 * Time: 7:19 AM
 * 
 */
public class SignalFrame extends JFrame {

    private AudioIo.AudioSignal signal ;

    public SignalFrame(AudioIo.AudioSignal signal)
    {
        this.signal=signal;
        setLocationByPlatform(true);
        setSize(new Dimension(1200, 300));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        SignalPlot signalPlot = new SignalPlot(signal.data[0], -1, 1);
        signalPlot.setZoomModeHorizontal(true);
        setContentPane(signalPlot);

    }




}
