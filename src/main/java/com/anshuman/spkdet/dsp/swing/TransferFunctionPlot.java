package com.anshuman.spkdet.dsp.swing;


import com.anshuman.spkdet.dsp.math.Complex;
import com.anshuman.spkdet.dsp.math.PolynomialUtils;

/**
* A Swing component for plotting the transfer curve of a signal filter.
*
* <p>
* The current implementation can display linear gain or phase.
* Logarithmic gain is not yet implemented.
*/
public class TransferFunctionPlot extends FunctionPlot {

private static final long    serialVersionUID = 1;

private static final double  borderFactor = 0.05;

/**
/* Constructs a plot component.
*
* @param tf
*    Coefficients of the z-plane transfer function.
* @param gainOrPhase
*    true = plot gain, false = plot phase
*/
public TransferFunctionPlot (PolynomialUtils.RationalFraction tf, boolean gainOrPhase) {
   super(new TransferFunctionPlotFunction(tf, gainOrPhase), 0, 0.5,
      gainOrPhase ? -borderFactor     : -Math.PI * (1+borderFactor*2),
      gainOrPhase ?  1 + borderFactor :  Math.PI * (1+borderFactor*2)); }

//--- Plot function ------------------------------------------------------------

private static class TransferFunctionPlotFunction extends SimplePlotFunction {

private PolynomialUtils.RationalFraction tf;
private boolean              gainOrPhase;

public TransferFunctionPlotFunction (PolynomialUtils.RationalFraction tf, boolean gainOrPhase) {
   super(10);
   this.tf = tf;
   this.gainOrPhase = gainOrPhase; }

@Override
public double getY (double x) {
   if (x < 0 || x > 0.5) {
      return Double.NaN; }
   Complex w = Complex.expj(2 * Math.PI * x);
   Complex t = PolynomialUtils.evaluate(tf, w);
   return gainOrPhase ? t.abs() : t.arg(); }

} // end class TransferFunctionPlotFunction
} // end class TransferFunctionPlot
