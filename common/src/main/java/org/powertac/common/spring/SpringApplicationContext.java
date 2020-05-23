/*
 * Copyright (c) 2011 by the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.powertac.common.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Static methods to access the Spring application context. It is set up
 * as a service so Spring will create and initialize it.
 * 
 * NOTE: These methods should NOT be used in constructors. Doing so makes it
 * virtually impossible to test with mocks, and makes transitive dependencies
 * very difficult in a test environment. Better to use them in individual
 * "service getter" methods that do nothing for dependencies that have
 * already been satisfied by test code using ReflectionTestUtils.setField().
 * 
 * ALSO NOTE: Even when used outside a constructor, there are still potential
 * problems in a test environment, because if any test in a suite initializes
 * Spring, then later tests that do not initialize Spring can retrieve
 * instances from earlier tests, which may not be what the test expects.
 * This can lead to baffling problems. To avoid this trap, it is best to NOT
 * use Spring in a unit-test environment. Instead, at the top of a test that
 * needs this service (it's usually not the test but something called by the
 * test that needs it), add a call to setTestBeans with a map of bean-name
 * to implementation (which may be a stub). This works for getBean(), but
 * not for the other calls.
 * 
 * @author John Collins
 */
@Service
public class SpringApplicationContext implements ApplicationContextAware
{
  private static ApplicationContext context;
  
  private static Map testBeans;
  
  @Override
  public void setApplicationContext (ApplicationContext appContext)
  throws BeansException
  {
    context = appContext;
  }
  
  /**
   * Returns the Spring bean, if any, with the given name.
   */
  public static Object getBean (String beanName)
  {
    if (context != null)
      return context.getBean(beanName);
    else if (null != testBeans) {
      Object result = testBeans.get(beanName); 
      return result;
    }
    else
      return null;
  }

  /**
   * Sets up the testBeans mapping
   */
  public static void setTestBeans (Map beanMap)
  {
    testBeans = beanMap;
  }
  
  /**
   * Returns the first Spring bean found that is an instance of the 
   * given class.
   */
  public static <T> T getBeanByType (Class<T> type) {
    T bean = null;
    Collection<T> beans = context.getBeansOfType(type).values();
    if (beans.size() > 0) {
      bean = new ArrayList<T>(beans).get(0);
    }
      
    return bean;
  }

  /**
   * Returns all the Spring beans that are instances of the given type.
   */
  public static <T> List<T> listBeansOfType(Class<T> type)
  {
    return new ArrayList<T>(context.getBeansOfType(type).values());
  }

  /**
   * Returns a map of all the Spring beans that are instances of 
   * the given type.
   */
  public static <T> Map<String, T> mapBeansOfType(Class<T> type)
  {
    return context.getBeansOfType(type);
  }
  
  public static ApplicationContext getContext ()
  {
    return context;
  }
}
