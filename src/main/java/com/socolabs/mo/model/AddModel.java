package com.socolabs.mo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddModel {
    private int a;
    private int b;

    public AddModel(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public AddModel() {
    }
}
