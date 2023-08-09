/*
 * Copyright (c) 2011 by the original author
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
package org.powertac.samplebroker.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.powertac.common.config.Configurator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Supports annotation-based configuration of broker components from 
 * properties or xml config files.
 * 
 * @author John Collins
 */
@Service
public class BrokerPropertiesService
implements ApplicationContextAware
{
  static private Logger log = LogManager.getLogger(BrokerPropertiesService.class);

  private ApplicationContext context;
  private CompositeConfiguration config;
  private Configurator configurator;
  
  private boolean initialized = false;
  
  /**
   * Default constructor
   */
  public BrokerPropertiesService ()
  {
    super();    
    recycle();
  }

  // test support
  void recycle ()
  {
    // set up the config instance
    config = new CompositeConfiguration();
    configurator = new Configurator();
    initialized = false;
  }
  
  /**
   * Loads the properties from classpath, default config file,
   * and user-specified config file, just in case it's not already been
   * loaded. This is done when properties are first requested, to ensure
   * that the logger has been initialized. Because the CompositeConfiguration
   * treats its config sources in FIFO order, this should be called <i>after</i>
   * any user-specified config is loaded.
   */
  void lazyInit ()
  {
    // only do this once
    if (initialized)
      return;
    initialized = true;
    log.debug("lazyInit");

    // find and load the default properties file
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> resources = null;
    try {
      resources = classloader.getResources("config/");
    } catch (IOException ioe) {
      log.error("Cannot fetch config resource", ioe.getMessage());
    }
    if (null == resources)
      return;
    URL first = resources.nextElement();
    FileFilter filter =
        file -> (file.exists() && !file.isDirectory() &&
                 !file.getName().equals("server.properties"));
    String configDir = first.getFile();
    File[] files =  new File(configDir).listFiles(filter);
    if (null == files)
      files = null;
    if (files != null) {
      for (File file : files) {
        try {
          if (file.toString().endsWith(".xml")) {
            log.debug("adding " + file.getName());
            config.addConfiguration(Configurator.readXML(file));
          }
          else if (file.toString().endsWith(".properties")) {
            log.debug("adding " + file.getName());
            config.addConfiguration(Configurator.readProperties(file));
          }
        }
        catch (Exception e) {
          log.warn("Unable to load properties file: " + file);
        }
      }
    }

    // find and load the default properties file
    try {
      File defaultProps = new File(configDir + "/broker.properties");
      if (defaultProps.canRead()) {
        log.debug("adding " + defaultProps.getName());
        config.addConfiguration(Configurator.readProperties(defaultProps));
      }
    }
    catch (Exception e1) {
      log.warn("config/broker.properties not found: " + e1.toString());
    }
    
    // set up the classpath props
    try {
      Resource[] xmlResources = context.getResources("classpath*:config/*.xml");
      for (Resource xml : xmlResources) {
        if (validXmlResource(xml)) {
          log.info("loading config from " + xml.getURI());
          config.addConfiguration(Configurator.readXML(xml.getURL()));
        }
      }
      Resource[] propResources = context.getResources("classpath*:config/*.properties");
      for (Resource prop : propResources) {
        if (validPropResource(prop)) {
          if (null == prop) {
            log.error("Null resource {}", prop.getFilename());
          }
          log.info("loading config from " + prop.getURI());
          config.addConfiguration(Configurator.readProperties(prop.getURL()));
        }
      }
    }
    catch (ConfigurationException e) {
      log.error("Problem loading configuration: " + e.toString());
    }
    catch (Exception e) {
      log.error("Error loading configuration: " + e.toString());
    }
    
    // set up the configurator
    configurator.setConfiguration(config);
  }

  public void setUserConfig (File userConfig)
          throws ConfigurationException, IOException
  {
    // then load the user-specified config
    config.addConfiguration(Configurator.readProperties(userConfig));
    log.debug("setUserConfig " + userConfig);
    lazyInit();
  }

//  public void setUserConfig (File userConfig)
//  {
//    // then load the user-specified config
//    try {
//      PropertiesConfiguration pconfig = new PropertiesConfiguration();
//      pconfig.read(new FileReader(userConfig));
//      config.addConfiguration(pconfig);
//      log.debug("setUserConfig " + userConfig.getName());
//    }
//    catch (ConfigurationException e) {
//      log.error("Config error loading " + userConfig + ": " + e.toString());
//    }
//    catch (Exception e) {
//      log.error("Error loading configuration: " + e.toString());
//    }
//    lazyInit();
//  }

  /**
   * Adds the Properties structure to the broker configuration. Earlier
   * configuration sources take precedence over later sources.
   */
  public void addProperties (Properties props)
  {
    lazyInit();
    for (Object key : props.keySet()) {
      Object value = props.get(key);
      if (value instanceof String && ((String) value).startsWith("[")) {
        // clean up list format
        String str = (String)value;
        value = str.substring(1, str.length() - 2);
      }
      config.setProperty((String)key, value);
    }
  }
  
//  public void addProperties (Properties props)
//  {
//    PropertiesConfiguration pconfig = new PropertiesConfiguration();
//    props.forEach((key, value) -> pconfig.addProperty((String) key, value));
//    addProperties(pconfig);
//  }

//  /**
//   * Adds the PropertiesConfiguration to the broker configuration. Earlier
//   * configuration sources take precedence over later sources.
//   */
//  public void addProperties (PropertiesConfiguration props)
//  {
//    config.addConfiguration(props);
//  }

  public void configureMe (Object target)
  {
    lazyInit();
    configurator.configureSingleton(target);
  }
 
  public Collection<?> configureInstances (Class<?> target)
  {
    lazyInit();
    Collection<?> result = configurator.configureInstances(target);
    return result;
  }
  
  public String getProperty (String name)
  {
    lazyInit();
    return config.getString(name);
  }

  public String getProperty (String name, String defaultValue)
  {
    lazyInit();
    return config.getString(name, defaultValue);
  }

  public Integer getIntegerProperty (String name, Integer defaultValue)
  {
    lazyInit();
    return config.getInteger(name, defaultValue);
  }

  public Double getDoubleProperty (String name, Double defaultValue)
  {
    lazyInit();
    return config.getDouble(name, defaultValue);
  }

  @Override
  public void setApplicationContext (ApplicationContext context)
      throws BeansException
  {
     this.context = context;
  }
  
  /**
   * Changes the value of a property (or adds a property).
   */
  public void setProperty (String key, Object value)
  {
    lazyInit();
    config.setProperty(key, value);
  }
  
  // -- valid configuration resources --
  private String[] excludedPaths =
    {".*/test-classes/.*", ".*/log4j.properties"};
  
  private boolean validXmlResource (Resource xml)
  {
    try {
      log.info("Validating resource " + xml.getURI());
      String path = xml.getURI().toString();
      for (String regex : excludedPaths) {
        if (path.matches(regex)) {
          return false;
        }
        if (!xml.exists()) {
          log.warn("Resource " + xml.getURI() + " does not exist");
          return false;
        }
        if (!xml.isReadable()) {
          log.warn("Resource " + xml.getURI() + " is not readable");
          return false;
        }
      }
      return true;
    }
    catch (IOException e) {
      log.error("Should not happen: " + e.toString());
      return false;
    }
    catch (Exception e) {
      log.error("Validation error " + e.toString());
      e.printStackTrace();
      return false;
    }
  }
  
  private boolean validPropResource (Resource prop)
  {
    return validXmlResource(prop);
  }
  
  // test support
  Configuration getConfig ()
  {
    return config;
  }
}
