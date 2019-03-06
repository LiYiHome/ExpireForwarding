package com.liyi.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by liyi.
 */
public class LoadConf {

    private Properties p = new Properties();

    public LoadConf() {
        try {
            p.load(this.getClass().getClassLoader().getResourceAsStream("ef.properties"));
        } catch (IOException e) {
            System.out.println("加载配置文件出现异常!");
            e.printStackTrace();
        }
    }

    /**
     * 获取属性值
     *
     * @param name 属性名称
     * @return 属性值
     */
    public String getProperties(String name) {
        String result = p.getProperty(name);
        return result;
    }

}
