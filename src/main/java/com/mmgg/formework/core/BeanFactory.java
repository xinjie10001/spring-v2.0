package com.mmgg.formework.core;

/**
 * @author 161196
 */
public interface BeanFactory {

    /**
     * 根据BeanName从IOC容器中获取一个实例Bean
     * @param beanName
     * @return
     */
    Object getBean(String beanName);
}
