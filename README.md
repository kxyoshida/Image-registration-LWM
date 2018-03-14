# Image-registration-LWM
A set of programs to map images according to Goshtasby's local weighted mean method.


"ImageRegistrationLWM.ijm"

 - A collection of ImageJ macro functions to be used in the process of image registration.
 

"Polish_Spot_Location_COM.java"

 - An ImageJ plugin to be used to refine the locations of the peak points in subpixel accuracy.
 
 - The plugin simply estimate the centre of mass within circle ROI specified by the radius. 
 
 - Requires apache.commons library.


"findLWM.py"

 - A python script to calculate the mapping coefficients used for 
Goshtasby's local weighted mean method of image registration 
(1988) from the given tab column tables of paired control points.

 - The program requires two arguments –– names of the input and 
output files. The input file is a tab-separated four-column 
table listing the paired control points.

 - Requires numpy and scipy.



"Interpolate_LMW.java"

 - An ImageJ plugin filter for image registration based on Goshtasby's local
weighted mean method (1988).

 - The plugin, when run against the active image, transforms the target image to the
original coordinates.

 - The program reads the Results Table which is the list of coefficients calculated
by an associated python script "findLWM.py" for the given control points.

Documentation –– https://github.com/kxyoshida/Image-registration-LWM/wiki
