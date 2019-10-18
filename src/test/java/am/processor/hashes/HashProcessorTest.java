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
package am.processor.hashes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.Volume;

public class HashProcessorTest
{
  private AppConfig config;
  private HashConfig hashConfig;
  private HashProcessor proc;
  private List<Volume> volumes;

  @Before
  public void setup()
  {
    config = new AppConfig();
    hashConfig = new HashConfig();
    config.setHashConfig(hashConfig);
    proc = new HashProcessor();
    volumes = new ArrayList<Volume>();
    final Volume vol = new Volume();
    final Directory root = new Directory();
    vol.setRoot(root);
    final File file1 = new File();
    file1.setName("a");
    file1.setByteSize(Long.valueOf(10));
    file1.setHashCreated(new Date(100000));
    file1.setHashValue("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
    root.add(file1);
    final File file2 = new File();
    file2.setName("b");
    file2.setByteSize(Long.valueOf(20));
    file2.setHashCreated(new Date(500));
    file2.setHashValue("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
    root.add(file2);
    volumes.add(vol);
  }

  @Test
  public void testUpdateStrategyNone()
  {
    hashConfig.setStrategy(HashStrategy.None);
    proc.update(config, null);
  }

  @Test
  public void testUpdateStrategyPercentage()
  {
    hashConfig.setStrategy(HashStrategy.Percentage);
    hashConfig.setPercentage(Double.valueOf(0.0d));
    proc.update(config, volumes);
  }
}
