package com.socolabs.mo.components.movingobjects.taxi;

import com.socolabs.mo.components.movingobjects.MovingObject;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Taxi extends MovingObject {

    public final static int STATUS_AVAILABLE = 0;
    public final static int STATUS_RUNNING = 1;
    public final static int STATUS_BUZY = 2;

    private int status;

    public Taxi(String id, double lat, double lng) {
        super(id, lat, lng);
        status = STATUS_AVAILABLE;
    }
}
