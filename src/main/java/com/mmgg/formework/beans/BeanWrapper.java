package com.mmgg.formework.beans;

public class BeanWrapper {

    private Object wrapperInstance;
    private Object originalInstance;

    public BeanWrapper(Object instance){
        this.wrapperInstance = instance;
        this.originalInstance = instance;
    }

    public Object getWrappedInstance(){
        return this.wrapperInstance;
    }
}
