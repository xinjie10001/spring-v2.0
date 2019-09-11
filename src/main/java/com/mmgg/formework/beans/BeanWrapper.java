package com.mmgg.formework.beans;

import com.mmgg.formework.core.FactoryBean;

public class BeanWrapper extends FactoryBean {

    /**
     * 还会用到 观察者模式
     * 支持事件响应,会有一个监听
     */
    private BeanPostProcessor postProcessor;

    private Object wrapperInstance;

    /**
     * 原始的通过反射new出来,要把包装起来,存下来
     */
    private Object originalInstance;

    public BeanWrapper(Object instance){
        this.wrapperInstance = instance;
        this.originalInstance = instance;
    }

    public Object getWrappedInstance(){
        return this.wrapperInstance;
    }

    /**
     * 返回代理以后的Class
     * 可能会是这个$Prox0
     * @return
     */
    public Class<?> getWrappedClass(){
        return this.wrapperInstance.getClass();
    }

    public BeanPostProcessor getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(BeanPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }
}
