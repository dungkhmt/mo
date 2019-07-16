package com.dailyopt.mo.controller;

import com.dailyopt.mo.model.AddModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {
    @PostMapping("/add")
    public Integer home(@RequestBody AddModel addModel) {
        return addModel.getA()+addModel.getB();
    }
}
