/*
 * Copyright 2019 the original author or authors.
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

import org.junit.Assert;
import org.junit.Test;
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
    Assert.assertEquals("Expected query of test resource file.",
        "select distinct ?show where {   ?show wdt:P31/wdt:P279* wd:Q15416.   ?show"
            + " rdfs:label ?label .   ?show wdt:P580 ?start .   filter(year(?start) = 2019)   filter(str(?label) = \"Show\") }",
        query);
  }
}
