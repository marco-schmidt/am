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
package am.app;

import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link AppConfigLoader} class.
 */
public class AppConfigLoaderTest
{
  @Test
  public void testInterpretProperties()
  {
    final AppConfig config = new AppConfig();
    final Properties props = new Properties();
    // props.put("logDir", ".");
    // props.put("databaseDir", ".");
    props.put("createHashes", "1%");
    props.put("ignoreDirNames", "@eaDir");
    props.put("ignoreFileNames", ".DS_Store,Thumbs.db");
    props.put("wikidata", "true");
    config.setProperties(props);
    final boolean result = AppConfigLoader.interpretProperties(config);
    Assert.assertTrue("Interpreting correct properties returns success result.", result);
    Assert.assertTrue("Wikidata enabled.", config.getWikidataConfiguration().isEnabled());
  }
}
