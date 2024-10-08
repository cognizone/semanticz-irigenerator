/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package zone.cogni.semanticz.irigenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import zone.cogni.asquare.cube.json5.Json5Light;
import zone.cogni.semanticz.irigenerator.json.Prefix;
import zone.cogni.semanticz.irigenerator.json.IriGeneratorSpecification;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;

public class Utils {

  /**
   * Loads the Java object from the given resource in the given format.
   *
   * @param resource URL to load the object from
   * @param format format of data that dwell at the URL
   * @return the Java object
   */
  public static IriGeneratorSpecification load(URL resource, Format format) {
    try {
      ObjectMapper mapper = format.equals(Format.JSON5) ? Json5Light.getJson5Mapper() : JsonLdObjectMapperFactory.getJsonLdMapper();
      return mapper.readValue(resource.openStream(), IriGeneratorSpecification.class);
    } catch (IOException e) {
      throw new RuntimeException("Unable to load uri generator configuration." + resource, e);
    }
  }


  /**
   * Constructs the prefix clauses from the objects.
   *
   * @param prefixes prefixes to generate
   * @return the SPARQL representation
   */
  public static String getPrefixQuery(final Collection<Prefix> prefixes) {
    return prefixes
            .stream()
            .map(e -> "PREFIX "
                    + StringUtils.rightPad(e.getKey() + ":", 8) + " <" + e.getValue() + ">\n")
            .collect(Collectors.joining())
            + "\n";
  }
}