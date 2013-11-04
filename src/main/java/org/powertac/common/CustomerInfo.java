/*
 * Copyright 2009-2013 the original author or authors.
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

package org.powertac.common;

//import org.powertac.common.enumerations.CustomerType;
import org.powertac.common.enumerations.PowerType;
import org.powertac.common.state.Domain;
import org.powertac.common.state.StateChange;
import com.thoughtworks.xstream.annotations.*;

/**
 * A CustomerInfo instance represents a customer model (i.e. a consumer or a producer)
 * within a specific competition. The customer data stored is published to all brokers in
 * the Competition in order to provide them with an brief overview on what type
 * of customers participate in the specific competition. The collection of CustomerInfo
 * instances are serialized and sent to brokers at the beginning of a game, allowing brokers
 * to correlate tariff subscriptions and power consumption/production with individual customers.
 *
 * @author Carsten Block, John Collins
 */
@Domain
@XStreamAlias("cust-info")
public class CustomerInfo //implements Serializable 
{
  @XStreamAsAttribute
  private long id = IdGenerator.createId();

  /** Name of the customer model */
  @XStreamAsAttribute
  private String name;

  /** population represented by this model */
  @XStreamAsAttribute
  private int population;

  /** gives the available power classifications of the customer */
  @XStreamAsAttribute
  private PowerType powerType;

  /** Total controllable capacity in kWh per member. This is the maximum
   * possible response to curtailment request. For a consumption power type,
   * this results in up-regulation and the value is <= 0,
   * because it represents less energy being delivered to the customer. 
   * Brokers should assume that curtailment will result in shifting of load
   * (or supply, for a production power type) to a later timeslot. */
  @XStreamAsAttribute
  private double controllableKWh = 0.0;

  /** Maximum up-regulation rate in kW per member,
   * beyond the curtailment of its load.
   * Value is zero for thermal storage or no storage,
   * and negative for a battery
   * (because energy is flowing away from the customer). */
  @XStreamAsAttribute
  private double upRegulationKW = 0.0;

  /** Maximum down-regulation rate in kW per member. This is energy the
   * customer can absorb in a timeslot, beyond its normal usage.
   * A positive value represents charging a battery or dumping heat
   * (or cold) into some type of thermal storage system. */
  @XStreamAsAttribute
  private double downRegulationKW = 0.0;

  /** Maximum energy storage capacity.
   * If a 2 kWh battery can be charged at 1 kW, it's max down-regulation
   * would be 1 kW, and its capacity would be 2 kWh. */
  @XStreamAsAttribute
  private double storageCapacity = 0.0;

  /** True just in case this customer can engage in multiple contracts
   * at the same time. Defaults to false. */
  @XStreamAsAttribute
  private boolean multiContracting = false;

  /** True just in case this customer negotiates over contracts.
   * Defaults to false. */
  @XStreamAsAttribute
  private boolean canNegotiate = false;
  
  /**
   * Creates a new CustomerInfo, with default power type set to
   * CONSUMPTION. Chain a call
   * to withPowerType() to set the correct power types.
   */
  public CustomerInfo (String name, int population)
  {
    super();
    powerType = PowerType.CONSUMPTION;
    this.name = name;
    this.population = population;
  }
  
  public long getId ()
  {
    return id;
  }

  /**
   * Display name for this CustomerInfo instance.
   */
  public String getName ()
  {
    return name;
  }

  /**
   * Population of the model represented by the CustomerInfo. This is not
   * necessarily the number of people represented, but the number of potential
   * tariff subscribers.
   */
  public int getPopulation ()
  {
    return population;
  }

  /**
   * Updates the population for the underlying model. Depending on when this
   * method is called, it may or may not have any impact. There is also no
   * guarantee that the underlying model refers to this value in any useful
   * way.
   */
  @StateChange
  public void setPopulation (Integer population)
  {
    this.population = population;
  }

  /**
   * The types of power consumption and/or production modalities available in
   * the customer model.
   */
  public PowerType getPowerType ()
  {
    return powerType;
  }
  
  /**
   * Fluent setter to set PowerType for this CustomerInfo.
   * Default value is PowerType.CONSUMPTION.
   */
  @StateChange
  public CustomerInfo withPowerType (PowerType type)
  {
    powerType = type;
    return this;
  }

  /**
   * True if this customer can subscribe to multiple contracts. This is normally
   * true for population models, false for models of individual entities.
   */
  public boolean isMultiContracting ()
  {
    return multiContracting;
  }

  /**
   * Fluent setter for the multiContracting property.
   * Default is false - all entities in the model will subscribe to the
   * same tariff.
   */
  @StateChange
  public CustomerInfo withMultiContracting (boolean value)
  {
    multiContracting = value;
    return this;
  }

  /**
   * True just in case the underlying Customer model can negotiate individual
   * contracts.
   */
  public boolean isCanNegotiate ()
  {
    return canNegotiate;
  }
  
  /**
   * Fluent setter for the canNegotiate flag. Should be called only by the
   * model itself.
   */
  @StateChange
  public CustomerInfo withCanNegotiate (boolean value)
  {
    canNegotiate = value;
    return this;
  }

  @Override
  public String toString() {
    return "CustomerInfo(" + name + ")";
  }
}
