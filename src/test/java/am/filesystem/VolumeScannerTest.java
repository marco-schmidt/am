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
package am.filesystem;

import java.io.File;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import am.app.AppConfig;
import am.filesystem.model.Volume;

/**
 * Test {@link VolumeScanner} class.
 */
public class VolumeScannerTest
{
  private VolumeScanner scanner;

  @Before
  public void setup()
  {
    final AppConfig config = new AppConfig();
    config.setIgnoreDirNames(new HashSet<String>());
    config.setIgnoreFileNames(new HashSet<String>());
    final Volume vol = new Volume();
    vol.setPath(".");
    scanner = new VolumeScanner(config, vol);
  }

  @Test
  public void testScan()
  {
    scanner.scan();
    Assert.assertNull("Null array yields null result.", FileSystemHelper.findNewest((File[]) null));
  }
}
