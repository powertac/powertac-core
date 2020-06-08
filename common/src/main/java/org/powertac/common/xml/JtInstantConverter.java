/*
 * Copyright (c) 2012-2013 by the original author
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
 */package org.powertac.common.xml;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.time.Instant;

/**
 * Serializes java.time.Instance to look like org.joda.time.Instance for
 * backward compatibility with older brokers.
 * 
 * @author John Collins
 */
public class JtInstantConverter implements Converter
{

  public JtInstantConverter ()
  {
    super();
  }
  
  @Override
  @SuppressWarnings("rawtypes")
  public boolean canConvert (Class type)
  {
    return Instant.class.isAssignableFrom(type);
  }
  public void marshal(Object instant, HierarchicalStreamWriter writer,
                      MarshallingContext context) {
    Long milli = ((Instant) instant).toEpochMilli();
    writer.startNode("iMillis");
    writer.setValue(milli.toString());
    writer.endNode();
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                      UnmarshallingContext context) {
    Long millis = 0L;
    reader.moveDown();
    millis = Long.parseLong(reader.getValue());
    reader.moveUp();
    return Instant.ofEpochMilli(millis);
  }
}
