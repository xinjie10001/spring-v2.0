package com.mmgg.formework.beans;

/**
 * 用于做事件监听
 */
public class BeanPostProcessor {

    public Object postProcessBefornInitialization(Object bean,String beanName){
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean,String beanName){
        return bean;
    }
}
