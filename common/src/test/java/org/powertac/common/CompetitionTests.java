package org.powertac.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.thoughtworks.xstream.XStream;

//@SpringJUnitConfig(locations = {"classpath:test-config.xml"})
//@DirtiesContext
//@TestExecutionListeners(listeners = {
//  DependencyInjectionTestExecutionListener.class,
//  DirtiesContextTestExecutionListener.class
//})
public class CompetitionTests
{

  @BeforeEach
  public void setUp () throws Exception
  {
  }

  @Test
  public void testNewInstance ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertNotNull(c1, "c1 created");
    assertEquals(c1, Competition.currentCompetition(), "c1 retreival");
    assertEquals("c1", c1.getName(), "name property");
    Competition c2 = Competition.newInstance("c2");
    assertEquals(c2, Competition.currentCompetition(), "c2 retreival");
    assertEquals("c2", c2.getName(), "name property");
  }

  @Test
  public void testSetDescription ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertEquals("", c1.getDescription(), "empty description");
    Competition cx = c1.withDescription("test version");
    assertEquals(c1, cx, "correct return");
    assertEquals("test version", Competition.currentCompetition().getDescription(), "description");
  }

  @Test
  public void testSetTimeslotLength ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertEquals(60, c1.getTimeslotLength(), "default length");
    Competition cx = c1.withTimeslotLength(30);
    assertEquals(c1, cx, "correct return");
    assertEquals(30, c1.getTimeslotLength(), "new length");
  }

  @Test
  public void testSetMinimumTimeslotCount ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertEquals(480, c1.getMinimumTimeslotCount(), "default count");
    Competition cx = c1.withMinimumTimeslotCount(300);
    assertEquals(c1, cx, "correct return");
    assertEquals(300, c1.getMinimumTimeslotCount(), "new count");
  }

  @Test
  public void testSetExpectedTimeslotCount ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertEquals(600, c1.getExpectedTimeslotCount(), "default count");
    Competition cx = c1.withExpectedTimeslotCount(360);
    assertEquals(c1, cx, "correct return");
    assertEquals(360, c1.getExpectedTimeslotCount(), "new count");
  }

  @Test
  public void testSetTimeslotsOpen ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertEquals(24, c1.getTimeslotsOpen(), "default count");
    Competition cx = c1.withTimeslotsOpen(13);
    assertEquals(c1, cx, "correct return");
    assertEquals(13, c1.getTimeslotsOpen(), "new count");
  }

  @Test
  public void testSetDeactivateTimeslotsAhead ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertEquals(1, c1.getDeactivateTimeslotsAhead(), "default count");
    Competition cx = c1.withDeactivateTimeslotsAhead(3);
    assertEquals(c1, cx, "correct return");
    assertEquals(3, c1.getDeactivateTimeslotsAhead(), "new count");
  }
  @Test
  public void testSetSimulationBaseTime ()
  {
    Competition c1 = Competition.newInstance("c1");
    Instant base =
            ZonedDateTime.of(2010, 6, 21, 0, 0, 0, 0, TimeService.UTC).toInstant();
    assertEquals(base, c1.getSimulationBaseTime(), "default base");
    Instant newBase = base.plusMillis(TimeService.DAY);
    Competition cx = c1.withSimulationBaseTime(newBase);
    assertEquals(c1, cx, "correct return");
    assertEquals(newBase, c1.getSimulationBaseTime(), "new base");
  }

  @Test
  public void testSetSimulationBaseTimeLong ()
  {
    Competition c1 = Competition.newInstance("c1");
    Instant base =
            ZonedDateTime.of(2010, 6, 21, 0, 0, 0, 0, TimeService.UTC).toInstant();
    assertEquals(base, c1.getSimulationBaseTime(), "default base");
    long newBase = base.plusMillis(TimeService.DAY).toEpochMilli();
    Competition cx = c1.withSimulationBaseTime(newBase);
    assertEquals(c1, cx, "correct return");
    assertEquals(newBase, c1.getSimulationBaseTime().toEpochMilli(), "new base");
  }

  @Test
  public void testYMDParse ()
  {
    Instant bt = ZonedDateTime.of(2010, 6, 21, 0, 0, 0, 0, TimeService.UTC).toInstant();
    String day = "2010-06-22T00:00:00";
    try {
      DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
      LocalDateTime base = LocalDateTime.parse(day, fmt);
    } catch (DateTimeParseException dtp) {
      System.out.println("Cannot parse " + dtp.getParsedString()
                         + " at " + dtp.getErrorIndex());
      fail();
    }
  }

  @Test
  public void testSetSimulationBaseTimeYmd ()
  {
    Competition c1 = Competition.newInstance("c1");
    Instant base =
            ZonedDateTime.of(2010, 6, 21, 0, 0, 0, 0, TimeService.UTC).toInstant();
    assertEquals(base, c1.getSimulationBaseTime(), "default base");
    String newBase = "2010-06-22";
    Competition cx = c1.withSimulationBaseTime(newBase);
    assertEquals(c1, cx, "correct return");
    assertEquals(base.plusMillis(TimeService.DAY).toEpochMilli(),
                 c1.getSimulationBaseTime().toEpochMilli(), "new base");
  }

  @Test
  public void testSetSimulationBaseTimeYmdhms ()
  {
    Competition c1 = Competition.newInstance("c1");
    Instant base =
            ZonedDateTime.of(2010, 6, 21, 0, 0, 0, 0, TimeService.UTC).toInstant();
    assertEquals(base, c1.getSimulationBaseTime(), "default base");
    String newBase = "2010-06-22T00:00:00";
    Competition cx = c1.withSimulationBaseTime(newBase);
    assertEquals(c1, cx, "correct return");
    assertEquals(base.plusMillis(TimeService.DAY).toEpochMilli(),
                 c1.getSimulationBaseTime().toEpochMilli(), "new base");
  }

  @Test
  public void testSetSimulationBaseTimeString ()
  {
    Competition c1 = Competition.newInstance("c1");
    Instant base =
            ZonedDateTime.of(2010, 6, 21, 0, 0, 0, 0, TimeService.UTC).toInstant();
    assertEquals(base, c1.getSimulationBaseTime(), "default base");
    String newBase =
            Long.toString(base.plusMillis(TimeService.DAY).toEpochMilli());
    Competition cx = c1.withSimulationBaseTime(newBase);
    assertEquals(c1, cx, "correct return");
    assertEquals(base.plusMillis(TimeService.DAY).toEpochMilli(),
                 c1.getSimulationBaseTime().toEpochMilli(), "new base");
  }

  @Test
  public void testSetSimulationRate ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertEquals(720l, c1.getSimulationRate(), "default rate");
    Competition cx = c1.withSimulationRate(300l);
    assertEquals(c1, cx, "correct return");
    assertEquals(300l, c1.getSimulationRate(), "new rate");
  }

  @Test
  public void testSetSimulationModulo ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertEquals(60*60*1000, c1.getSimulationModulo(), "default mod");
    Competition cx = c1.withSimulationModulo(30*60000);
    assertEquals(c1, cx, "correct return");
    assertEquals(30*60000, c1.getSimulationModulo(), "new mod");
  }
  
  @Test
  public void testGetClockParams ()
  {
    long base =
            ZonedDateTime.of(2010, 6, 21, 0, 0, 0, 0, TimeService.UTC).toInstant().toEpochMilli();
    long rate = 300l;
    long modulo = 30*60000l;
    Competition c1 = Competition.newInstance("c1")
        .withSimulationBaseTime(base)
        .withSimulationRate(rate)
        .withSimulationModulo(modulo)
        .withBootstrapDiscardedTimeslots(24)
        .withBootstrapTimeslotCount(48)
        .withTimeslotLength(30);
    Map<String, Long> params = c1.getClockParameters();
    assertEquals(base, params.get("base").longValue(), "correct base");
    assertEquals(rate, params.get("rate").longValue(), "correct rate");
    assertEquals(modulo, params.get("modulo").longValue(), "correct modulo");
    
  }
  
  @Test
  public void testBootInterval ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertEquals(336, c1.getBootstrapTimeslotCount(), "correct boot count");
    assertEquals(24, c1.getBootstrapDiscardedTimeslots(), "correct discard count");
    assertEquals(360, c1.getBootstrapTimeslotCount() + c1.getBootstrapDiscardedTimeslots(), "correct total");
  }

  @Test
  public void testAddBroker ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertEquals(0, c1.getBrokers().size(), "no brokers");
    Competition cx = c1.addBroker("Jill");
    assertEquals(c1, cx, "correct return");
    assertEquals(1, c1.getBrokers().size(), "one broker");
    assertEquals("Jill", c1.getBrokers().get(0), "correct broker");
  }

  @Test
  public void testAddCustomer ()
  {
    Competition c1 = Competition.newInstance("c1");
    assertEquals(0, c1.getCustomers().size(), "no customers");
    CustomerInfo info = new CustomerInfo("Podunk", 42);
    Competition cx = c1.addCustomer(info);
    assertEquals(c1, cx, "correct return");
    assertEquals(1, c1.getCustomers().size(), "one customer");
    assertEquals(info, c1.getCustomers().get(0), "correct customer");
  }

  @Test
  public void serializationTest ()
  {
    Competition c1 = Competition.newInstance("c1").withDescription("serialization test");
    XStream xstream = XMLMessageConverter.getXStream();
    xstream.processAnnotations(Competition.class);
    StringWriter serialized = new StringWriter();
    serialized.write(xstream.toXML(c1));
    //System.out.println(serialized.toString());
    Competition xc1 = (Competition)xstream.fromXML(serialized.toString());
    assertNotNull(xc1, "deserialized something");
    assertEquals(c1.getId(), xc1.getId(), "correct id");
    assertEquals("c1", c1.getName(), "correct name");
  }
}
