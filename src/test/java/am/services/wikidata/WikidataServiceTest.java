/*
 * Copyright 2019, 2020, 2021, 2022 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Assert;
import org.junit.Test;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.util.StrUtil;

/**
 * Unit tests for {@link WikidataService}.
 *
 * @author Marco Schmidt
 */
public class WikidataServiceTest
{
  @Test
  public void testLoadSparqlTemplate()
  {
    final String data = new WikidataService().loadSparqlTemplate("am/services/wikidata/Test.rq");
    Assert.assertEquals("Expected content of test resource file.", "SELECT * WHERE {}", data);
  }

  @Test
  public void testBuildFindTelevisionShowQuery()
  {
    String query = new WikidataService().buildFindTelevisionShowQuery("Show", Integer.valueOf(2019));
    query = StrUtil.escapeControl(query);
    Assert.assertTrue("Expected query to contain Show.", query != null && query.contains("Show"));
    Assert.assertTrue("Expected query to contain 2019.", query != null && query.contains("2019"));
  }

  @Test
  public void testBuildFindTelevisionSeasonsQuery()
  {
    String query = new WikidataService().buildFindTelevisionSeasonsQuery("Q886");
    query = StrUtil.escapeControl(query);
    Assert.assertTrue("Expected query to contain Q886.", query != null && query.contains("Q886"));
  }

  @Test
  public void testBuildFindTelevisionEpisodesQuery()
  {
    String query = new WikidataService().buildFindTelevisionEpisodesQuery("Q435566");
    query = StrUtil.escapeControl(query);
    Assert.assertTrue("Expected query to contain Q435566.", query != null && query.contains("Q435566"));
  }

  @Test
  public void testAssignUnknownEntityWhereNull()
  {
    final AppConfig config = new AppConfig();
    final WikidataService service = new WikidataService();
    service.setAppConfig(config);
    service.assignUnknownEntityWhereNull(null);
    final List<Directory> list = new ArrayList<Directory>();
    final Directory d1 = new Directory();
    d1.setWikidataEntityId(null);
    list.add(d1);
    final Directory d2 = new Directory();
    final String entityId = "Q1";
    d2.setWikidataEntityId(entityId);
    list.add(d2);
    service.assignUnknownEntityWhereNull(list);
    Assert.assertEquals("Element without entity now contains unknown entity.", WikidataEntity.UNKNOWN_ENTITY,
        d1.getWikidataEntityId());
    Assert.assertEquals("Element with entity still contains same entity.", entityId, d2.getWikidataEntityId());
  }

  @Test
  public void testExtractEntityStringNull()
  {
    Assert.assertNull("Null input returns null output.", WikidataService.extractEntity((String) null));
  }

  @Test
  public void testExtractEntityStringNoSlash()
  {
    Assert.assertNull("Input without slash returns null output.", WikidataService.extractEntity("a"));
  }

  @Test
  public void testExtractEntitySlash()
  {
    Assert.assertEquals("Input with slash returns everything after slash.", "Q123",
        WikidataService.extractEntity("http://a/Q123"));
  }

  @Test
  public void testExtractEntityValueNull()
  {
    Assert.assertNull("Null input returns null output.", WikidataService.extractEntity((Value) null));
  }

  @Test
  public void testExtractEntityValueNoSlash()
  {
    Assert.assertNull("Input without slash returns null output.",
        WikidataService.extractEntity(SimpleValueFactory.getInstance().createIRI("proto:ac")));
  }

  @Test
  public void testExtractEntityValueSlash()
  {
    Assert.assertEquals("Input with slash returns everything after slash.", "Q123",
        WikidataService.extractEntity(SimpleValueFactory.getInstance().createIRI("http://a.org/Q123")));
  }
}
