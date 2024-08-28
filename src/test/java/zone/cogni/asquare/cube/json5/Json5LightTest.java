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

package zone.cogni.asquare.cube.json5;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Json5LightTest {

  @Test
  public void read_json() throws IOException {
    // given
    URL resource = getClass().getResource("/json5/read_json.json5");
    ObjectMapper json5Mapper = Json5Light.getJson5Mapper();

    // when
    JsonNode root = json5Mapper.readTree(Objects.requireNonNull(resource).openStream());

    // then
    assertTrue(root.has("id"));
    assertTrue(root.has("someNumber"));
    assertTrue(root.has("multiLine"));
  }
}