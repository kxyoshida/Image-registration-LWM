macro "Disassemble multi-point selection" {
 	getSelectionCoordinates(xPs, yPs);
	print(xPs.length);
      roiManager("reset");
 	for (jj=0; jj<xPs.length; jj++) {
		x=xPs[jj];
		y=yPs[jj];
		makePoint(x,y);
	  	roiManager("Add");
	}
	roiManager("Sort");
}

macro "A-trous decomposition filter" {
	run("Spot Extraction Filter");
}

macro "Peak Point Selection" {
// This is just for exemplification purpose.
// Better to run the built-in command "Process>Find Maxima..." 
// adjusting the noise tolerance with "preview".
      run("Find Maxima...", "noise=10 output=[Point Selection] exclude");
}

macro "ROIManager2ResultsTable"
{
	run("Set Measurements...", "  centroid redirect=None decimal=4");
	roiManager("Deselect");
	roiManager("Measure");
}

macro "Polish Spots against AD files" {
      run("Polish Spot Location COM", "half=7 method=BILINEAR maximal=1 lower=0.10000 upper=1 input_folder=[] output_folder=[]");
}

