package com.mmgg.formework.context;

import com.mmgg.formework.annotation.Autowried;
import com.mmgg.formework.annotation.Controller;
import com.mmgg.formework.annotation.Service;
import com.mmgg.formework.beans.BeanDefinition;
import com.mmgg.formework.beans.BeanPostProcessor;
import com.mmgg.formework.beans.BeanWrapper;
import com.mmgg.formework.context.support.BeanDefinitionReader;
import com.mmgg.formework.core.BeanFactory;
import com.mmgg.formework.webmvc.controller.TestController;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 161196
 */
public class ApplicationContext implements BeanFactory {

    private String[] configLocations;

    private BeanDefinitionReader reader;

    /**
     * beanDefinittionMap用来保存配置信息
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 用来存储所有被代理的过的对象
     */
    private Map<String,BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();

    /**
     * 用来保证注册是单利的容器
     */
    private Map<String,Object> beanCaceMap = new HashMap<>();

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
        doAutowrited();
        TestController testController = (TestController) this.getBean("testController");
        testController.query(null,null,"wangxinjie");
        System.out.println();
    }

    /**
     * 开始执行自动化的依赖注入
     */
    private void doAutowrited() {
        for(Map.Entry<String,BeanDefinition> beanDefinitionEntry:this.beanDefinitionMap.entrySet()){
            String beanName = beanDefinitionEntry.getKey();
            if(!beanDefinitionEntry.getValue().isLazyInit()){
                getBean(beanName);
            }
        }

        for(Map.Entry<String,BeanWrapper> beanWrapperEntry: this.beanWrapperMap.entrySet()){
             populateBean(beanWrapperEntry.getKey(),beanWrapperEntry.getValue().getWrappedInstance());
        }
    }

    public void populateBean(String beanName,Object instance){
        Class clazz = instance.getClass();
        if(!clazz.isAnnotationPresent(Controller.class)||
             clazz.isAnnotationPresent(Service.class)){
            return;
        }

        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields){
            if(!field.isAnnotationPresent(Autowried.class)){continue;}
            Autowried autowried = field.getAnnotation(Autowried.class);

            String autowiredBeanName = autowried.value().trim();

            if("".equals(autowiredBeanName)){
                autowiredBeanName = field.getType().getName();
            }
            field.setAccessible(true);

            try {
                field.set(instance,this.beanWrapperMap.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
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
     * @param beanName
     * @return
     */
    @Override
    public Object getBean(String beanName) {

        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        String className = beanDefinition.getBeanClassName();

        try{

            //生成通知事件
            BeanPostProcessor beanPostProcessor = new BeanPostProcessor();

            Object instance = instantionBean(beanDefinition);
            if(null == instance){
                return null;
            }

            //在实例初始化以前调用一次
            beanPostProcessor.postProcessBefornInitialization(instance,beanName);

            BeanWrapper beanWrapper = new BeanWrapper(instance);
            beanWrapper.setPostProcessor(beanPostProcessor);
            this.beanWrapperMap.put(beanName,beanWrapper);

            //在实例初始化之后调用一次
            beanPostProcessor.postProcessAfterInitialization(instance,beanName);

            // populateBean(beanName,instance);

            //通过这样一调用,相当于给我们自己留有可操作的空间
            return this.beanWrapperMap.get(beanName).getWrappedInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 传一个BeanDefinition，就返回一个实例Bean
     * @param beanDefinition
     * @return
     */
    private Object instantionBean(BeanDefinition beanDefinition){
        Object instance;
        String className = beanDefinition.getBeanClassName();
        try {
            //因为根据Class才能确定一个类是否有实例
            if(this.beanCaceMap.containsKey(className)){
                instance = this.beanCaceMap.get(className);
            }else{
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.beanCaceMap.put(className,instance);
            }
            return instance;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
