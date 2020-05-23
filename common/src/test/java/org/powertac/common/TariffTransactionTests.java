/*
 * Copyright (c) 2016 by John Collins.
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
package org.powertac.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.powertac.common.enumerations.PowerType;
import org.powertac.common.repo.BrokerRepo;
import org.powertac.common.repo.CustomerRepo;
import org.powertac.common.repo.TariffRepo;
import org.powertac.common.repo.TimeslotRepo;
import org.powertac.common.spring.SpringApplicationContext;

import com.thoughtworks.xstream.XStream;

//@SpringJUnitConfig(locations = {"classpath:test-config.xml"})
//@DirtiesContext
//@TestExecutionListeners(listeners = {
//  DependencyInjectionTestExecutionListener.class,
//  DirtiesContextTestExecutionListener.class
//})
public class TariffTransactionTests
{
  //TestContextManager f;
  Broker b1;
  BrokerRepo brokerRepo;
  TariffSpecification spec;
  CustomerInfo customer;

  @BeforeEach
  public void setUp() 
  {
    brokerRepo = new BrokerRepo();
    b1 = new Broker("bob");
    brokerRepo.add(b1);
    spec = new TariffSpecification(b1, PowerType.CONSUMPTION);
    customer = new CustomerInfo("Podunk", 42);
    CustomerRepo customerRepo = new CustomerRepo();
    customerRepo.add(customer);
    TariffRepo tariffRepo = new TariffRepo();
    TimeslotRepo timeslotRepo = new TimeslotRepo();
    Map<String, Object> appServices =
            Map.of("brokerRepo", brokerRepo,
                   "customerRepo", customerRepo,
                   "tariffRepo", tariffRepo,
                   "timeslotRepo", timeslotRepo);
    SpringApplicationContext.setTestBeans(appServices);
  }

  @Test
  public void construction1() 
  {
    TariffTransaction ttx = new TariffTransaction(b1, 1, TariffTransaction.Type.PUBLISH,
                      spec, null, 0, 0.0, 0.0, false);
    assertNotNull(ttx, "created");
    assertEquals(TariffTransaction.Type.PUBLISH, ttx.getTxType(), "type");
    assertEquals(b1, ttx.getBroker(), "broker");
    assertFalse(ttx.isRegulation(), "not reg");
  }

  @Test
  public void xmlSerializationTest ()
  {
    TariffTransaction ttx = new TariffTransaction(b1, 2, TariffTransaction.Type.CONSUME,
                      spec, customer, 42, -420.0, 42.0, true);
    XStream xstream = XMLMessageConverter.getXStream();
    xstream.processAnnotations(TariffTransaction.class);
    StringWriter serialized = new StringWriter();
    serialized.write(xstream.toXML(ttx));
    System.out.println(serialized.toString());
    TariffTransaction xttx = (TariffTransaction) xstream.fromXML(serialized.toString());
    assertNotNull(xttx, "deserialized something");
    assertEquals(TariffTransaction.Type.CONSUME, xttx.getTxType(), "correct type");
    assertTrue(xttx.isRegulation(), "regulation");
    assertEquals(42.0, xttx.getCharge(), 1e-6, "correct value");
  }
}
