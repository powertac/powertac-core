/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.powertac.samplebroker.core;

import java.io.File;
import java.util.Date;
import java.util.List;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Multi-session broker runner. The Spring context is re-built for each
 * session. By default, one session runs and the broker exits. Alternatively,
 * the broker will start a new session after the previous session finishes,
 * based on a specified count or time period in hours.  
 * 
 * @author John Collins, Pascal Brokmeier
 */
public class BrokerRunner
{
  private static Logger log = LogManager.getLogger();
  private AbstractApplicationContext context;
  private PowerTacBroker broker;

  public BrokerRunner ()
  {
    super();
  }

  public void processCmdLine (String[] args)
  {
    OptionParser parser = new OptionParser();
    OptionSpec<String> jmsUrlOption =
            parser.accepts("jms-url").withRequiredArg().ofType(String.class);
    OptionSpec<File> configOption = 
            parser.accepts("config").withRequiredArg().ofType(File.class);
    OptionSpec<Integer> repeatCountOption = 
            parser.accepts("repeat-count").withRequiredArg().ofType(Integer.class);
    OptionSpec<Integer> repeatHoursOption = 
            parser.accepts("repeat-hours").withRequiredArg().ofType(Integer.class);
    OptionSpec<String> queueNameOption =
            parser.accepts("queue-name").withRequiredArg().ofType(String.class);
    OptionSpec<String> serverQueueOption =
            parser.accepts("server-queue").withRequiredArg().ofType(String.class);
    parser.accepts("ipc-adapter-name").withRequiredArg().ofType(String.class);
    //parser.accepts("no-ntp");
    parser.accepts("interactive");
    OptionSpec<String> propOption =
            parser.accepts("prop").withRequiredArg().ofType(String.class);

    // do the parse
    OptionSet options = parser.parse(args);

    File configFile = null;
    //String jmsUrl = null;
    //boolean noNtp = false;
    //boolean interactive = false;
    //String queueName = null;
    //String serverQueue = null;
    Integer repeatCount = 1;
    Integer repeatHours = 0;
    long end = 0l;
    PropertiesConfiguration cliProps = new PropertiesConfiguration();

    try {
      // process broker options
      System.out.println("<options");
      if (options.has(configOption)) {
        configFile = options.valueOf(configOption);
        System.out.println(" config=\"" + configFile.getName() + "\"");
      }
      if (options.has(jmsUrlOption)) {
        cliProps.setProperty("samplebroker.core.powerTacBroker.jmsBrokerUrl",
                             options.valueOf(jmsUrlOption));
        System.out.print(" jms-url=\"" + options.valueOf(jmsUrlOption) + "\"");
      }
      //if (options.has("no-ntp")) {
      //  noNtp = true;
      //  System.out.println("  no ntp - estimate offset");
      //}
      if (options.has(repeatCountOption)) {
        repeatCount = options.valueOf(repeatCountOption);
        System.out.print(" repeat-count=\"" + repeatCount + "\"");
      }
      else if (options.has(repeatHoursOption)) {
        repeatHours = options.valueOf(repeatHoursOption);
        System.out.print(" repeat-hours=\"" + repeatHours + "\"");
        long now = new Date().getTime();
        end = now + 1000 * 3600 * repeatHours;
      }
      if (options.has(queueNameOption)) {
        cliProps.setProperty("samplebroker.core.powerTacBroker.brokerQueueName",
                             options.valueOf(queueNameOption));
        System.out.print(" queue-name=\"" + options.valueOf(queueNameOption) + "\"");
      }
      if (options.has(serverQueueOption)) {
        cliProps.setProperty("samplebroker.core.powerTacBroker.serverQueueName",
                             options.valueOf(serverQueueOption));
        System.out.print(" server-queue=\"" + options.valueOf(serverQueueOption) + "\"");
      }
      if (options.has("interactive")) {
        cliProps.setProperty("samplebroker.core.powerTacBroker.interactive",
                "true");
        System.out.print(" interactive=\"true\"");
      }
      if (options.has("ipc-adapter-name")){
        Object adapterName = options.valueOf("ipc-adapter-name");
        cliProps.setProperty("samplebroker.core.brokerMessageReceiver.ipcAdapterName", adapterName);
        System.out.println("Using " + adapterName + " to initialize an IpcAdapter as a listener. This is the Class name of the Bean you want to use as an Adapter.");
      }
      if (options.has(propOption)) {
        List<String> values = options.valuesOf(propOption);
        for (String value: values) {
          int colon = value.indexOf(":");
          if (colon > 0) {
            String name = value.substring(0, colon);
            String val = value.substring(colon + 1);
            cliProps.setProperty(name, val);
            System.out.print(" " + name + "=" + val);
          }
        }
      }
      System.out.println("/>");

      // at this point, we are either done, or we need to repeat
      int counter = 0;
      while ((null != repeatCount && repeatCount > 0) ||
              (new Date().getTime() < end)) {
        counter += 1;

        // initialize and run
        if (null == context) {
          context = new ClassPathXmlApplicationContext("broker.xml");
        }
        else {
          context.close();
          context.refresh();
        }

        // Re-open the logfiles
        reopenLogs(counter);

        // get the broker reference and delegate the rest
        context.registerShutdownHook();
        broker = (PowerTacBroker)context.getBeansOfType(PowerTacBroker.class).values().toArray()[0];
        System.out.println("Starting session " + counter);
        log.info("Starting session {}", counter);
        broker.startSession(cliProps, configFile, end);
        if (null != repeatCount)
          repeatCount -= 1;
      }
    }
    catch (OptionException e) {
      System.err.println("Bad command argument: " + e.toString());
    }
  }

  // reopen the logfiles for each session
  private void reopenLogs(int counter)
  {
    System.setProperty("logfile", "log/broker" + counter + ".trace");
    System.setProperty("statefile", "log/broker" + counter + ".state");
    
    ((LoggerContext) LogManager.getContext(false)).reconfigure();
  }
}
