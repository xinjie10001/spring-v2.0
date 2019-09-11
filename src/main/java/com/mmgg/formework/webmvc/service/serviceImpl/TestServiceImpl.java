package com.mmgg.formework.webmvc.service.serviceImpl;

import com.mmgg.formework.annotation.Service;
import com.mmgg.formework.webmvc.service.TestService;

@Service
public class TestServiceImpl implements TestService {

    @Override
    public String getTestService(String name) {
        return "My name is:"+name;
    }
}
