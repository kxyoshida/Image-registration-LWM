import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

import ij.measure.*;
import ij.text.*;
import ij.io.*;
import java.text.DecimalFormat;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

class realPoint {
    double x;
    double y;
    double intensity;
}

public class Polish_Spot_Location_COM implements PlugInFilter {
    ImagePlus imp;
    static int hw = 2; // half of window size in pixels 
    static String imet = "BILINEAR";
    String[] items={"BILINEAR", "BICUBIC"};

    static int iter_max = 3;
    static double dr2_lower = 0.1;
    static double dr2_upper = 1.0;
    static boolean rwRTs = false;
    static String RTSource = "";
    static String RTDestination = "";
    static boolean showLog = false;

    public int setup(String arg, ImagePlus imp) {
	this.imp = imp;
	return DOES_8G + DOES_16 + DOES_32;
    }

    FloatProcessor getInterpolatedRegion(ImageProcessor ip, realPoint centre, int X, int Y, int hw) {
	FloatProcessor fip = new FloatProcessor(2*hw+1, 2*hw+1);
	double cx = X - hw + centre.x;
	double cy = Y - hw + centre.y;
	for (int i = 0; i< 2*hw+1; i++) {
	    for (int j = 0; j< 2*hw+1; j++) {
		double ipip = ip.getInterpolatedPixel(cx-hw+j, cy-hw+i);
		fip.setf(j, i, (float)ipip);
	    }
	}
	return fip;
    }


    boolean withinCircle(double x, double y) {
	//	if ( (x-hw+1)*(x-hw+1)+(y-hw+1)*(y-hw+1)<=hw*hw ) 
	if ( (x-hw)*(x-hw)+(y-hw)*(y-hw)<=hw*hw ) 
	    return true; 
	else 
	    return false;
    }

    public realPoint calcCentreOfMass(ImageProcessor ip) 
    {
	int width = ip.getWidth();
	int height = ip.getHeight();
	realPoint centre = new realPoint();

	float[] pixels = (float[])ip.getPixels();
	int count = 0;

	double sumx = 0, sumy = 0, sumg = 0, nm = 0;
	for (int i = 0; i<height; i++) {
	    for (int j = 0; j<width; j++) {
		if (withinCircle(j, i)) {
		    int l=i*width+j;
		    sumx += j*pixels[l];
		    sumy += i*pixels[l];
		    sumg += pixels[l];
		    nm += 1;
		}
	    }
	}
	//      Modified on 25082014 again
	//      We do not add 0.5 because the coordinates should be consistent with 
	//      the interpolation methods of ImageProcessor, not with the centre of the mass.
	centre.x = sumx/sumg;
	centre.y = sumy/sumg;
	centre.intensity = sumg/nm;
	return centre;
    }

    private void fixResultsTable(ResultsTable rt, int tr, double cx, double cy, double intensity) {
	if (tr >= 0 && tr < rt.getCounter()) {
	    rt.setValue("cx", tr, cx);
	    rt.setValue("cy", tr, cy);
	    rt.setValue("intensity", tr, intensity); 
	} else {
	    IJ.log("No corresponding rows in Results Tabe.");
	}
	return;
    }

    public void run(ImageProcessor ip) {

	int width = ip.getWidth();
	int height = ip.getHeight();
	realPoint centre = new realPoint();


	ImageStack stack = imp.getStack();
	int nSlices = stack.getSize();

	GenericDialog gd = new GenericDialog("Find Centre of Mass");
	gd.addNumericField("Half window size", hw, 0);
	gd.addRadioButtonGroup("Method of Iteration",items, 1, 2, imet);	
	gd.addNumericField("Maximal Iteration (>=1)", iter_max, 0);
	gd.addNumericField("Lower limit of convergence", dr2_lower, 5);
	gd.addNumericField("Upper limit of convergence", dr2_upper, 1);
	gd.addCheckbox("Read and write RT files: ",rwRTs);
	gd.addStringField("Input_Folder: ",RTSource);
	gd.addStringField("Output_Folder: ",RTDestination);
	gd.addCheckbox("Logging: ",showLog);
	gd.showDialog();
	if (gd.wasCanceled()) return;
	hw = (int) gd.getNextNumber();
	imet=gd.getNextRadioButton();
	iter_max = (int) gd.getNextNumber();
	dr2_lower = (double) gd.getNextNumber();
	dr2_upper = (double) gd.getNextNumber();

	rwRTs = gd.getNextBoolean();
	RTSource = gd.getNextString();
	RTDestination = gd.getNextString();
	showLog = gd.getNextBoolean();

	if (imet == "BILINEAR")
	    ip.setInterpolationMethod(ImageProcessor.BILINEAR);
	else if (imet == "BICUBIC")
	    ip.setInterpolationMethod(ImageProcessor.BICUBIC);

	for (int n = 1 ; n <= nSlices; n++) {
	    int n_lower = 0;
	    int n_upper = 0;

	    ip = stack.getProcessor(n).convertToFloat();

	    float[] pixels = (float[]) ip.getPixels();
	    DescriptiveStatistics ds = new DescriptiveStatistics();

	    for (int i = 0; i < pixels.length; i++)
		ds.addValue((double)pixels[i]);

	    if (rwRTs) {
		try {
		    Opener.openResultsTable(RTSource+"SpA"+String.format("%04d", n)+".txt");
		}
		catch (Exception e) {
		    IJ.error("Can't open Results Table.");
		    return;
		}
	    }

	    ResultsTable rt = ResultsTable.getResultsTable();
	    if (rt == null) {
		IJ.error("Can't open Results Table.");
		return;
	    }
	    int nResults = rt.getCounter();

	    for (int i = 0; i < nResults; i++) {
		int X = (int)rt.getValue("X", i);
		int Y = (int)rt.getValue("Y", i);
		double cx = -1;
		double cy = -1;
		FloatProcessor fip = new FloatProcessor(2*hw+1, 2*hw+1);

		if ( X>=hw+1 && Y>=hw+1 && X+hw+2<width && Y+hw+2<height ) {
		    ip.setRoi(X-hw, Y-hw, 2*hw+1, 2*hw+1);
		    //	2014/07/10 modified from ip.setRoi(X-hw, Y-hw, 2*hw, 2*hw);
		    centre = calcCentreOfMass(ip.crop());

		    for (int iter = 1; iter < iter_max; iter++) {
			realPoint centre0 = centre;
			fip = getInterpolatedRegion(ip, centre, X, Y, hw);
			centre = calcCentreOfMass(fip);
			double dr2 = (centre.x-centre0.x)*(centre.x-centre0.x) + (centre.y-centre0.y)*(centre.y-centre0.y);
			if (dr2 > dr2_upper) {
			    if (showLog)
				IJ.log("Convergence failed for (X,Y)=("+X+","+Y+") at iter="+iter+".");
			    n_upper++;
			    break;
			}
			if (dr2 < dr2_lower) {
			    if (showLog)
				IJ.log("Convergence reached for (X,Y)=("+X+","+Y+") at iter="+iter+".");
			    n_lower++;
			    break;
			}
		    }
		    cx = X - hw + centre.x;
		    cy = Y - hw + centre.y;
		}

		fixResultsTable(rt, i, cx, cy, centre.intensity);
	    }
	    rt.show("Results");
	    IJ.log("Convergence reached = "+(100*n_lower/nResults)+"%, failed = "+(100*n_upper/nResults)+"%");
	    if (rwRTs) {
		try {
		    rt.saveAs(RTDestination+"COMSpA"+String.format("%04d", n)+".txt");
		} 
		catch (Exception e) {
		    IJ.error("Can't save Results Table.");
		    return;
		}
	    }
	}
    }
}
