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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import zone.cogni.semanticz.irigenerator.json.Prefix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Deserializes the map of prefixes into Java POJOs.
 * TODO: note that this is here only to ensure compliance of the new data model with the original JSON5 serialization.
 */
public class PrefixDeserializer extends JsonDeserializer<List<Prefix>> {

  @Override
  public List<Prefix> deserialize(JsonParser p, DeserializationContext ctxt)
          throws IOException {
    final List<Prefix> keyValueList = new ArrayList<>();
    final ObjectMapper mapper = (ObjectMapper) p.getCodec();
    final JsonNode rootNode = mapper.readTree(p);

    final Iterator<String> fieldNames = rootNode.fieldNames();
    while (fieldNames.hasNext()) {
      final String key = fieldNames.next();
      final String value = rootNode.get(key).asText();
      keyValueList.add(new Prefix().setKey(key).setValue(value));
    }

    return keyValueList;
  }
}
