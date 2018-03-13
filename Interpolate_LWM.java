import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;
import java.lang.Math;

import ij.measure.*;
import ij.text.*;
import ij.io.*;
import java.text.DecimalFormat;


public class Interpolate_LWM implements PlugInFilter {
    // Transform the target image to the original coordinates
    // using local weighted mean method of Goshtasby (1988).
    // The program reads the Results Table which is the list 
    // of coefficients calculated by an associated python script
    // "findLWM.py" for the given control points.

    ImagePlus imp;
    public int setup(String arg, ImagePlus imp) {
	this.imp = imp;
	return DOES_8G + DOES_16 + DOES_32;
    }

    public void run(ImageProcessor ip) {
	int w = ip.getWidth();
	int h = ip.getHeight();

	String title = imp.getShortTitle();
	title = "tr-"+title;
	ImageStack stack = imp.getStack();
	int nSlices = stack.getSize();

	ResultsTable rt = ResultsTable.getResultsTable();
	if (rt == null) {
	    IJ.error("Can't open Results Table.");
	    return;
	}
	int nResults = rt.getCounter();
	double X[] = new double[nResults];
	double Y[] = new double[nResults];
	double radii[]= new double[nResults];
	double[] a0=new double[nResults];
	double[] ax=new double[nResults];
	double[] ay=new double[nResults];
	double[] axy=new double[nResults];
	double[] ax2=new double[nResults];
	double[] ay2=new double[nResults];
	double[] b0=new double[nResults];
	double[] bx=new double[nResults];
	double[] by=new double[nResults];
	double[] bxy=new double[nResults];
	double[] bx2=new double[nResults];
	double[] by2=new double[nResults];
	
	for (int i = 0; i < nResults; i++) {
	    int icp = (int)rt.getValue("C1",i);
	    X[i] = (double)rt.getValue("C2", i);
	    Y[i] = (double)rt.getValue("C3", i);
	    radii[i]= (double)rt.getValue("C4", i);
	    a0[i]= (double)rt.getValue("C5", i);
	    ax[i]= (double)rt.getValue("C6", i);
	    ay[i]= (double)rt.getValue("C7", i);
	    axy[i]= (double)rt.getValue("C8", i);
	    ax2[i]= (double)rt.getValue("C9", i);
	    ay2[i]= (double)rt.getValue("C10", i);
	    b0[i]= (double)rt.getValue("C11", i);
	    bx[i]= (double)rt.getValue("C12", i);
	    by[i]= (double)rt.getValue("C13", i);
	    bxy[i]= (double)rt.getValue("C14", i);
	    bx2[i]= (double)rt.getValue("C15", i);
	    by2[i]= (double)rt.getValue("C16", i);
	}

	FloatProcessor mip=new FloatProcessor(w,h);
	ImageStack mis=new ImageStack(w,h);
	
	for (int slice=1;slice<=nSlices;slice++) {
	    IJ.showProgress(slice,nSlices);
	    ip = stack.getProcessor(slice).convertToFloat();
	    
	    // Sweep the coordinates of the destination image after affine transformation.
	    for (int y=0;y<h;y++) {
		for (int x=0;x<w;x++) {
		    double sumw = 0;
		    double mpx = 0;
		    for (int i=0; i< nResults; i++) {
			double R = Math.sqrt(Math.pow(X[i]-x,2)+Math.pow(Y[i]-y,2))/radii[i];
			double weight=0;
			if (R > 1) 
			    weight=0;
			else if (R>=0) 
			    weight=1-3*Math.pow(R,2)+2*Math.pow(R,3);

			double mx = a0[i]+ax[i]*x+ay[i]*y+axy[i]*x*y+ax2[i]*Math.pow(x,2)+ay2[i]*Math.pow(y,2);
			double my = b0[i]+bx[i]*x+by[i]*y+bxy[1]*x*y+bx2[i]*Math.pow(x,2)+by2[i]*Math.pow(y,2);
			if (mx>=0 && mx<w && my>=0 && my<h) {
			    mpx+=weight*(ip.getBicubicInterpolatedPixel(mx,my,ip));
			    sumw+=weight;
			}

		    }
		    mpx = mpx/sumw;
		    mip.putPixelValue(x,y,mpx);
		}
	    }
	    mis.addSlice(title+"_fr"+slice, mip.duplicate());
	}
	ImagePlus mimp=new ImagePlus(title, mis);
	mimp.show();
    }
}