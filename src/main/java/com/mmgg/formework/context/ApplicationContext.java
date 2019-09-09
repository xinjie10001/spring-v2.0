package com.mmgg.formework.context;

import com.mmgg.formework.beans.BeanDefinition;
import com.mmgg.formework.context.support.BeanDefinitionReader;
import com.mmgg.formework.core.BeanFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext implements BeanFactory {

    private String[] configLocations;

    private BeanDefinitionReader reader;

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    public ApplicationContext(String ... configLocations){
        this.configLocations=configLocations;
        this.refresh();
    }

    public void refresh(){
        //定位
        this.reader = new BeanDefinitionReader(configLocations);

        //加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();

        //注册
        doRegisty(beanDefinitions);

        //依赖注入(lazy-init=false),要是执行依赖注入的方法，在这里自动调用getBean的方法
    }

    /**
     * 真正将BeanDefinitions注册到beanDefinitionMap中
     */
    private void doRegisty(List<String> beanDefinitions) {
        //beanName有三种情况
        //1.默认是类名首字母小写
        //2.自定义名称
        //3.接口注入
        try {
            for(String className: beanDefinitions){
                Class<?> beanClass = Class.forName(className);

                /*如果是一个借口,是不能实例化的
                用它实现类实例化*/
                if(beanClass.isInterface()){
                    continue;
                }

                BeanDefinition beanDefinition = reader.registerBean(className);
                if(beanDefinition != null){
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
                }

                Class<?>[] interfaces = beanClass.getInterfaces();
                for(Class<?> i: interfaces){
                    //如果是多个实现类,只能覆盖
                    //为什么？因为Spring没有那么智能,就是这么傻
                    //这个时候,可以自定义名字
                    this.beanDefinitionMap.put(i.getName(),beanDefinition);
                }

                //到这一步,容器初始化完毕
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 依赖注入从这里开始
     * 通过读取DeanDefinition中的信息
     * 然后通过反射机制创建一个实例并返回
     * Spring的做法,不会把最原始的对象放出去,会用一个BeanWrapper来进行一次包装
     * 装饰器模式：
     * 1.保留原来的OOP关系
     * 2.我需要对它进行扩展,增强(为了以后AOP打基础)
     * @param name
     * @return
     */
    @Override
    public Object getBean(String name) {
        return null;
    }
}