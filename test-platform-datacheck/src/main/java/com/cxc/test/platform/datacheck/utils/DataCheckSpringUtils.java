package com.cxc.test.platform.datacheck.utils;

import com.cxc.test.platform.datacheck.ext.fieldCheck.FieldCheckExt;
import com.cxc.test.platform.datacheck.ext.skip.SkipCheckHandlerChain;
import com.cxc.test.platform.datacheck.ext.sourceLocate.SourceLocateExt;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class DataCheckSpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 指定获取实现了FieldCheckExt接口的bean
     *
     * @param fcBeanName
     * @return
     */
    public static FieldCheckExt getFcBean(String fcBeanName) {
        return getBean(fcBeanName, FieldCheckExt.class);
    }

    /**
     * 指定获取实现了SourceLocateExt接口的bean
     *
     * @param locateBeanName
     * @return
     */
    public static SourceLocateExt getLocateBean(String locateBeanName) {
        return getBean(locateBeanName, SourceLocateExt.class);
    }

    /**
     * 指定获取SkipCheckHandlerChain这个bean
     * @return
     */
    public static SkipCheckHandlerChain getSkipCheckHandlerChain() {
        return getBean("skipCheckHandlerChain", SkipCheckHandlerChain.class);
    }

    private static <T> T getBean(String beanName, Class<T> clazz) {
        if (applicationContext.containsBean(beanName)) {
            return (T) applicationContext.getBean(beanName, clazz);
        } else {
            return null;
        }
    }
}
