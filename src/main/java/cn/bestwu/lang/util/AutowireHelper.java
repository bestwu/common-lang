package cn.bestwu.lang.util;

import javax.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * bean 导入工具
 *
 * @author Peter Wu
 */
public class AutowireHelper implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  private static Environment environment;

  /**
   * getProperty
   *
   * @param key key
   * @return Property
   */
  public static String getProperty(String key) {
    return environment.getProperty(key);
  }

  /**
   * getProperty
   *
   * @param key key
   * @param defaultValue 默认值
   * @return Property
   */
  public static String getProperty(String key, String defaultValue) {
    return environment.getProperty(key, defaultValue);
  }

  /**
   * @param key key
   * @param targetType targetType
   * @param <T> target
   * @return target
   */
  public static <T> T getProperty(String key, Class<T> targetType) {
    return environment.getProperty(key, targetType);
  }

  /**
   * @param key key
   * @param targetType targetType
   * @param defaultValue 默认值
   * @param <T> target
   * @return target
   */
  public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
    return environment.getProperty(key, targetType, defaultValue);
  }

  /**
   * getProperty
   *
   * @param key key
   * @return Property
   */
  public static String getRequiredProperty(String key) throws IllegalStateException {
    return environment.getProperty(key);
  }

  /**
   * @param key key
   * @param targetType targetType
   * @param <T> target
   * @return target
   */
  public static <T> T getRequiredProperty(String key, Class<T> targetType)
      throws IllegalStateException {
    return environment.getProperty(key, targetType);
  }

  /**
   * @return Environment
   */
  public static Environment getEnvironment() {
    return environment;
  }

  /**
   * @param clazz clazz
   * @param <T> clazz 对应的类型
   * @return clazz 对应的Bean
   */
  public static <T> T getBean(Class<T> clazz) {
    return applicationContext.getBean(clazz);
  }

  /**
   * @return 如果当前线程是HttpServletRequest请求返回对应 request,否则返回:null
   */
  public static HttpServletRequest getRequest() {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    if (requestAttributes == null) {
      return null;
    }
    return requestAttributes.getRequest();
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) {
    AutowireHelper.applicationContext = applicationContext;
    environment = applicationContext.getEnvironment();
  }

}