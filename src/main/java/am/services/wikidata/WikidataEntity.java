/*
 * Copyright 2019, 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.services.wikidata;

/**
 * Interface for classes which can be assigned a single Wikidata entity ID associated with it.
 *
 * @author Marco Schmidt
 */
public interface WikidataEntity
{
  /**
   * The string representing an unknown Wikidata entity, a non-null value to indicate that the application searched for
   * it without result, avoiding identical queries in the future.
   */
  String UNKNOWN_ENTITY = "?";

  /**
   * Return Wikidata entity ID associated with this object.
   *
   * @return String with entity ID
   */
  String getWikidataEntityId();

  /**
   * Assign entity ID associated with this object.
   *
   * @param newValue
   *          entity ID value, possibly null
   */
  void setWikidataEntityId(String newValue);
}
