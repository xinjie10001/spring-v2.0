package com.mmgg.formework.webmvc.controller;

import com.mmgg.formework.annotation.Autowried;
import com.mmgg.formework.annotation.Controller;
import com.mmgg.formework.annotation.RequestMapping;
import com.mmgg.formework.annotation.RequestParam;
import com.mmgg.formework.webmvc.service.TestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 161196
 */
@RequestMapping("test")
@Controller
public class TestController {

    @Autowried
    private TestService testService;

    @RequestMapping("get")
    public void query(HttpServletRequest request, HttpServletResponse response,
         @RequestParam("name")String name){
        String str = testService.getTestService(name);
        System.out.printf(str);
    }
}
