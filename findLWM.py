#!/usr/bin/python

# A python script to calculate the mapping coefficients used for 
# Goshtasby's local weighted mean method of image registration 
# (1988) from the given tab column tables of paired control points.
#
# The program requires two arguments –– names of the input and 
# output files. The input file is a tab-separated four-column 
# table listing the paired control points.
#
from numpy import *
from scipy import *
from scipy import optimize
import sys

def findLWM(cp):

    M = size(cp, axis=0)
    N = int16(M/2+1)    
    if N<6:
        print "Too few control points! Require 12 points at least!"
    x = cp[:,0]
    y = cp[:,1]
    u = cp[:,2]
    v = cp[:,3]

    T = zeros((M,16))
    
    radii = zeros((M,1))

    fitfunc = lambda p, x: dot(x,p)
    errfunc = lambda p, x, y: fitfunc(p, x) - y

    
    for icp in r_[:M]:
        ### find N closest points
        distcp = sqrt( (x-x[icp])**2 + (y-y[icp])**2 )
        indx = distcp.argsort()
        dist_sorted = distcp[indx]
        radii[icp] = dist_sorted[N-1]
        neighbors = indx[:N]
        nindx = neighbors.argsort()
        neighbors = neighbors[nindx]
        xcp = x[neighbors]
        ycp = y[neighbors]    
        ucp = u[neighbors]    
        vcp = v[neighbors]
    
        ### set up matrix eqn for polynomial of order=2
        X = c_[ones((N,1)),  xcp,  ycp,  xcp*ycp,  xcp**2,  ycp**2]

        # Part I: Calculating Left to Right transformation coefficients      
        # The function chosen is a third order polynomial function

        X=X
        p0=zeros(6)
        coeffA,success = optimize.leastsq(errfunc, p0[:], args=(X,ucp))
        coeffB,success = optimize.leastsq(errfunc, p0[:], args=(X,vcp))
        
        T[icp,:] = r_[icp,x[icp],y[icp],radii[icp],coeffA,coeffB]

    return T

def main():
    if len(sys.argv) != 3:
        print "Usage: python %s (input file) (output file)" % sys.argv[0]
        print "The input file is a tab-separated four-column table listing the control points.\n" 
        quit()
        
    cp=genfromtxt(sys.argv[1])
    Ts = findLWM(cp)

    savetxt(sys.argv[2],Ts, fmt='%i\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f')
    
if __name__ == '__main__':
    main()

