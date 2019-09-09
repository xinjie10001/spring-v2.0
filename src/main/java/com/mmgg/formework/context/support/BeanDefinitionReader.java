package com.mmgg.formework.context.support;

import com.mmgg.formework.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author 161196
 * 用于对配置文件进行查找，读取，解析
 */
public class BeanDefinitionReader {

    private Properties config = new Properties();
    private List<String> registryBeanClasses = new ArrayList<>();
    //在配置文件中，用来获取自动扫描的包名key
    private final String SCAN_PACKAGE = "scanPackage";

    public BeanDefinitionReader(String ... locations){
        //在spring中是通过Reader去查找和定位
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:",""));
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null!=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    /**
     * 递归扫描所有相关的class，并保存到一个List中
     * @param packageName
     */
    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.","/"));
        File classDir= new File(url.getFile());

        for(File file : classDir.listFiles()){
            if(file.isDirectory()){
                doScanner(packageName+"."+file.getName());
            }else{
                registryBeanClasses.add(packageName+"."+file.getName().replaceAll(".class",""));
            }
        }
    }

    public List<String> loadBeanDefinitions(){
        return null;
    }

    /**
     * 每注册一个className，就返回一个BeanDefinition,我自己包装
     * 只是为了对配置信息进行一个包装
     * @return
     */
    public BeanDefinition registerBean(String className){
        if(this.registryBeanClasses.contains(className)){
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(lowerFisrtCase(className.substring(className.lastIndexOf(".")+1)));
            return beanDefinition;
        }
        return null;
    }

    private String lowerFisrtCase(String str){
        char[] chars = str.toCharArray();
        chars[0] +=32;
        return String.valueOf(chars);
    }

    public Properties getConfig(){
        return this.config;
    }
}
