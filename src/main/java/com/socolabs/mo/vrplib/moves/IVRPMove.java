package com.socolabs.mo.vrplib.moves;

import com.socolabs.mo.vrplib.entities.LexMultiValues;

public interface IVRPMove {

    void move();
    LexMultiValues evaluation();

}
