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
package org.powertac.common.repo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.Instant;

import org.powertac.common.Competition;
import org.powertac.common.TimeService;
import org.powertac.common.Timeslot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Repository for Timeslots. 
 * 
 * Timeslots are created with makeTimeslot(). Several
 * query methods are supported, including currentTimeslot(), enabledTimeslots(),
 * and findBySerialNumber(). The implementation makes a strong assumption that
 * timeslots are created in sequence, and that each timeslot starts when the
 * previous timeslots ends.
 * @author John Collins
 */
@Repository
public class TimeslotRepo implements DomainRepo
{
  static private Logger log = Logger.getLogger(TimeslotRepo.class.getName());

  // local state
  private ArrayList<Timeslot> indexedTimeslots;

  @Autowired
  private TimeService timeService;
  
  /** standard constructor */
  public TimeslotRepo ()
  {
    super();
    indexedTimeslots = new ArrayList<Timeslot>();
  }

  /** 
   * Creates a timeslot with the given start time. The sequence number of the
   * timeslot is the number of timeslots since the simulation base time.
   */
  public Timeslot makeTimeslot (Instant startTime)
  {
    long duration = Competition.currentCompetition().getTimeslotDuration();
    Instant base = Competition.currentCompetition().getSimulationBaseTime();
    log.debug("makeTimeslot" + startTime.toString());
    int index = (int)((startTime.getMillis() - base.getMillis()) / duration);
    Instant realStart = getTimeForIndex(index);
    Timeslot result;
    if (index >= indexedTimeslots.size()) {
      // create new timeslot
      result = new Timeslot(index, realStart);
      add(result);
    }
    else {
      // already exists
      result = indexedTimeslots.get(index);
    }
    return result;
  }
  
  /**
   * Returns the timeslot for the current time.
   */
  public Timeslot currentTimeslot () 
  {
    return findByInstant(timeService.getCurrentTime());
  }
  
  /**
   * Returns the serial number of the current timeslot
   */
  public int currentSerialNumber ()
  {
    return currentTimeslot().getSerialNumber();
  }

  /**
   * Returns the timeslot with the given serial number.
   */
  public Timeslot findBySerialNumber (int serialNumber)
  {
    return makeTimeslot(getTimeForIndex(serialNumber));
  }

  /**
   * Returns the timeslot with the given serial number.
   */
  public Timeslot findOrCreateBySerialNumber (int serialNumber)
  {
    return findBySerialNumber(serialNumber);
  }

  /**
   * Creates timeslots to fill in the time from sim start to the current
   * time. This is needed to initialize brokers.
   */
  public void createInitialTimeslots()
  {
    findOrCreateBySerialNumber(getTimeslotIndex(timeService.getCurrentTime()));
  }
  
  /**
   * Returns the timeslot (if any) corresponding to a particular Instant.
   */
  public Timeslot findByInstant (Instant time)
  {
    log.debug("find " + time.toString());
    int index = getTimeslotIndex(time);
    return findBySerialNumber(index);
  }

  /**
   * Converts time to timeslot index without actually creating a timeslot
   */
  public int getTimeslotIndex (Instant time)
  {
    long offset = time.getMillis()
            - Competition.currentCompetition().getSimulationBaseTime().getMillis();
    long duration = Competition.currentCompetition().getTimeslotDuration();
    // truncate to timeslot boundary
    return (int)(offset / duration);
  }

  /**
   * Returns the list of enabled timeslots, starting with the first by serial number.
   * This code depends on the set of enabled timeslots being contiguous in the serial
   * number sequence, and on a disabled timeslot never being re-enabled.
   */
  public List<Timeslot> enabledTimeslots ()
  {
    int firstIndex = currentTimeslot().getSerialNumber()
            + Competition.currentCompetition().getDeactivateTimeslotsAhead();
    ArrayList<Timeslot> result = new ArrayList<Timeslot>(30);
    for (int index = 0;
         index < Competition.currentCompetition().getTimeslotsOpen();
         index++) {
      result.add(findOrCreateBySerialNumber(firstIndex + index));
    }
    return result;
  }
  
  /**
   * True just in case the specified timeslot is enabled.
   */
  public boolean isTimeslotEnabled (Timeslot ts)
  {
    return isTimeslotEnabled(ts.getSerialNumber());
  }
  
  /**
   * True just in case the timeslot with the given index is enabled.
   */
  public boolean isTimeslotEnabled (int index)
  {
    int firstIndex = currentTimeslot().getSerialNumber()
            + Competition.currentCompetition().getDeactivateTimeslotsAhead();
    int lastIndex = firstIndex + Competition.currentCompetition().getTimeslotsOpen();
    return (index >= firstIndex && index < lastIndex);
  }
  
  /**
   * Returns the number of timeslots that have been successfully created.
   */
  public int count ()
  {
    return indexedTimeslots.size();
  }
  
  /**
   * Adds a timeslot that already exists. Visibility is public to support
   * logfile analysis, should not be used in other contexts.
   */
  public void add (Timeslot timeslot)
  {
    int sn = timeslot.getSerialNumber();
    if (indexedTimeslots.size() > sn && indexedTimeslots.get(sn) != timeslot) {
      log.error("timeslot sn " + sn + " already exists");
    }
    // ensure capacity
    for (int index = indexedTimeslots.size(); index < sn; index++) {
      indexedTimeslots.add(new Timeslot(index, getTimeForIndex(index)));
    }
    indexedTimeslots.add(timeslot);
  }
  
  /**
   * Converts int timeslot index to time
   */
  public Instant getTimeForIndex (int index)
  {
    Competition comp = Competition.currentCompetition();
    return new Instant(comp.getSimulationBaseTime().getMillis()
                       + index * comp.getTimeslotDuration());    
  }

  @Override
  public void recycle ()
  {
    log.debug("recycle");
    indexedTimeslots.clear();
  }

}
