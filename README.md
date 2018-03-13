# Image-registration-LWM
A set of programs to map images according to Goshtasby's local weighted mean method.

findLWM.py

 - A python script to calculate the mapping coefficients used for 
Goshtasby's local weighted mean method of image registration 
(1988) from the given tab column tables of paired control points.

 - The program requires two arguments –– names of the input and 
output files. The input file is a tab-separated four-column 
table listing the paired control points.

 - Requires numpy and scipy.



Interpolate_LMW.java

 - An ImageJ plugin filter for image registration based on Goshtasby's local
weighted mean method (1988).

 - The plugin, when run against the active image, transform the target image to the
original coordinates using local weighted mean method of Goshtasby (1988).

 - The program reads the Results Table which is the list of coefficients calculated
by an associated python script "findLWM.py" for the given control points.

