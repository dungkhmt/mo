package com.socolabs.mo.vrplib.core;

import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;

import java.util.ArrayList;

public interface IVRPChecker {
    boolean checkInsertPointMove(VRPPoint x, VRPPoint y);

    // move customer x to from route of x to route of y; insert x Pointo the
    // position between y and next[y]
    // x and y are not the depot
    boolean checkOnePointMove(VRPPoint x, VRPPoint y);

    // remove (prev[x],x) and (x,next[x]) and (prev[y], y) and (y, next(y)
    // insert (x,prev[y]) and (next[y],x) and (next[x],y) and (y, prev[x])
    boolean checkTwoPointsMove(VRPPoint x, VRPPoint y);

    // move of type c [Groer et al., 2010]
    // x and y are on different routes and are not depots
    // remove (x,next[x]) and (y,next[y])
    // insert (x,y) and (next[x],next(y))
    boolean checkTwoOptMove1(VRPPoint x, VRPPoint y);

    // move of type c [Groer et al., 2010]
    // x and y are on different routes and are not depots
    // remove (x,next[x]) and (y,next[y])
    // insert (y,x) and (next[x],next(y))
    boolean checkTwoOptMove2(VRPPoint x, VRPPoint y);

    // move of type c [Groer et al., 2010]
    // x and y are on different routes and are not depots
    // remove (x,next[x]) and (y,next[y])
    // insert (x,y) and (next[y],next(x))
    boolean checkTwoOptMove3(VRPPoint x, VRPPoint y);

    // move of type c [Groer et al., 2010]
    // x and y are on different routes and are not depots
    // remove (x,next[x]) and (y,next[y])
    // insert (y,x) and (next[y],next(x))
    boolean checkTwoOptMove4(VRPPoint x, VRPPoint y);

    // move of type c [Groer et al., 2010]
    // x and y are on different routes and are not depots
    // remove (x,next[x]) and (y,next[y])
    // insert (x,next[y]) and (y,next[x])
    boolean checkTwoOptMove5(VRPPoint x, VRPPoint y);

    // move of type c [Groer et al., 2010]
    // x and y are on different routes and are not depots
    // remove (x,next[x]) and (y,next[y])
    // insert (next[y],x) and (y,next[x])
    boolean checkTwoOptMove6(VRPPoint x, VRPPoint y);

    // move of type c [Groer et al., 2010]
    // x and y are on different routes and are not depots
    // remove (x,next[x]) and (y,next[y])
    // insert (x,next[y]) and (next[x],y)
    boolean checkTwoOptMove7(VRPPoint x, VRPPoint y);

    // move of type c [Groer et al., 2010]
    // x and y are on different routes and are not depots
    // remove (x,next[x]) and (y,next[y])
    // insert (next[y],x) and (next[x],y)
    boolean checkTwoOptMove8(VRPPoint x, VRPPoint y);

    // x is before y on the same route
    // remove (x, next[x]) and (y,next[y])
    // add (x,y) and (next[x],next[y])
    boolean checkTwoOptMoveOneRoute(VRPPoint x, VRPPoint y);

    // move of type d [Groer et al., 2010]
    // move the sequence <x1,next[x1],..., prev[x2], x2> of length len to the
    // route containing y
    // remove (prev[x1],x1) and (x2,next[x2]), and (y,next[y])
    // add (y, x1) and (x2, next[y]) and (prev[x1], next[x2])
    boolean checkOrOptMove1(VRPPoint x1, VRPPoint x2, VRPPoint y);

    // move of type d [Groer et al., 2010]
    // move the sequence <x1,next[x1],..., prev[x2], x2> of length len to the
    // route containing y
    // remove (prev[x1],x1) and (x2,next[x2]), and (y,next[y])
    // add (y, x2) and (x1, next[y]) and (prev[x1], next[x2])
    boolean checkOrOptMove2(VRPPoint x1, VRPPoint x2, VRPPoint y);

    // move of type e [Groer et al., 2010]
    // x, y, z are on the same route in that order (x is before y, y is before
    // z)
    // remove (x, next[x]), (y, next[y]), and (z, next[z])
    // insert (x,z) and (next[y], next[x]) and(y, next[z])
    // s --> x -> nx --> y -> ny --> z -> nz --> e
    // s --> x -> z --> ny -> nx --> y -> nz --> e
    boolean checkThreeOptMove1(VRPPoint x, VRPPoint y, VRPPoint z);

    // move of type e [Groer et al., 2010]
    // x, y, z are on the same route in that order (x is before y, y is before
    // z)
    // remove (x, next[x]), (y, next[y]), and (z, next[z])
    // insert (z,x) and (next[x], next[y]) and(next[z],y)
    // ny --> z -> x --> s -> 
    boolean checkThreeOptMove2(VRPPoint x, VRPPoint y, VRPPoint z);

    // move of type e [Groer et al., 2010]
    // x, y, z are on the same route in that order (x is before y, y is before
    // z)
    // remove (x, next[x]), (y, next[y]), and (z, next[z])
    // insert (x,y) and (next[x], z) and(next[y], next[z])
    boolean checkThreeOptMove3(VRPPoint x, VRPPoint y, VRPPoint z);

    // move of type e [Groer et al., 2010]
    // x, y, z are on the same route in that order (x is before y, y is before
    // z)
    // remove (x, next[x]), (y, next[y]), and (z, next[z])
    // insert (y,x) and (z,next[x]) and(next[z], next[y])
    boolean checkThreeOptMove4(VRPPoint x, VRPPoint y, VRPPoint z);

    // move of type e [Groer et al., 2010]
    // x, y, z are on the same route in that order (x is before y, y is before
    // z)
    // remove (x, next[x]), (y, next[y]), and (z, next[z])
    // insert (x,next[y]) and (z,next[x]) and(y, next[z])
    boolean checkThreeOptMove5(VRPPoint x, VRPPoint y, VRPPoint z);

    // move of type e [Groer et al., 2010]
    // x, y, z are on the same route in that order (x is before y, y is before
    // z)
    // remove (x, next[x]), (y, next[y]), and (z, next[z])
    // insert (next[y],x) and (next[x],z) and(next[z],y)
    boolean checkThreeOptMove6(VRPPoint x, VRPPoint y, VRPPoint z);

    // move of type e [Groer et al., 2010]
    // x, y, z are on the same route in that order (x is before y, y is before
    // z)
    // remove (x, next[x]), (y, next[y]), and (z, next[z])
    // insert (x,next[y]) and (z,y) and(next[x], next[z])
    boolean checkThreeOptMove7(VRPPoint x, VRPPoint y, VRPPoint z);

    // move of type e [Groer et al., 2010]
    // x, y, z are on the same route in that order (x is before y, y is before
    // z)
    // remove (x, next[x]), (y, next[y]), and (z, next[z])
    // insert (next[y],x) and (y,z) and(next[z], next[x])
    boolean checkThreeOptMove8(VRPPoint x, VRPPoint y, VRPPoint z);

    // move of type g [Groer et al., 2010]
    // x1 and y1 are on the same route, x1 is before y1
    // x2 and y2 are on the same route, x2 is before y2
    // remove (x1,next[x1]) and (y1, next[y1])
    // remove (x2, next[x2]) and (y2, next[y2])
    // insert (x1, next[x2]) and (y2, next[y1])
    // insert (x2, next[x1]) and (y1, next[y2])
    boolean checkCrossExchangeMove1(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2);

    // move of type g [Groer et al., 2010]
    // x1 and y1 are on the same route, x1 is before y1
    // x2 and y2 are on the same route, x2 is before y2
    // remove (x1,next[x1]) and (y1, next[y1])
    // remove (x2, next[x2]) and (y2, next[y2])
    // insert (x1, y2) and (next[x2], next[y1])
    // insert (x2, next[x1]) and (y1, next[y2])
    boolean checkCrossExchangeMove2(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2);

    // move of type g [Groer et al., 2010]
    // x1 and y1 are on the same route, x1 is before y1
    // x2 and y2 are on the same route, x2 is before y2
    // remove (x1,next[x1]) and (y1, next[y1])
    // remove (x2, next[x2]) and (y2, next[y2])
    // insert (x1, next[x2]) and (y2, next[y1])
    // insert (x2, y1) and (next[x1], next[y2])
    boolean checkCrossExchangeMove3(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2);

    // move of type g [Groer et al., 2010]
    // x1 and y1 are on the same route, x1 is before y1
    // x2 and y2 are on the same route, x2 is before y2
    // remove (x1,next[x1]) and (y1, next[y1])
    // remove (x2, next[x2]) and (y2, next[y2])
    // insert (x1, y2) and (next[x2], next[y1])
    // insert (x2, y1) and (next[x1], next[y2])
    boolean checkCrossExchangeMove4(VRPPoint x1, VRPPoint y1, VRPPoint x2, VRPPoint y2);

    boolean checkKPointsMove(ArrayList<VRPPoint> x, ArrayList<VRPPoint> y);
}
