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
package am.db;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.Volume;

public class JdbcSerializationTest
{
  @Test
  public void isConnectedNullTest()
  {
    final JdbcSerialization io = new JdbcSerialization();
    Assert.assertFalse("New object is not connected.", io.isConnected());
  }

  @Test
  public void connectInMemoryTest()
  {
    final JdbcSerialization io = new JdbcSerialization();
    final AppConfig config = new AppConfig();
    io.setConfig(config);
    boolean connected = io.connect(null);
    Assert.assertTrue("Connection in memory always works.", connected);
    Assert.assertTrue("Connection in memory always works.", io.isConnected());

    connected = io.connect(null);
    Assert.assertTrue("Second connect works as well.", connected);
    Assert.assertTrue("Second connect works as well.", io.isConnected());

    io.close();
    Assert.assertFalse("Connection gone after closing.", io.isConnected());
  }

  @Test
  public void loadAllEmptyTest()
  {
    final JdbcSerialization io = new JdbcSerialization();
    final AppConfig config = new AppConfig();
    io.setConfig(config);
    io.connect(null);
    io.createTables();
    final List<Volume> vols = io.loadAll();
    Assert.assertNotNull("Load all on empty database returns non-null list.", vols);
    Assert.assertTrue("Load all on empty database returns empty list.", vols.isEmpty());
    io.close();
  }

  @Test
  public void saveAndloadAllTest()
  {
    final JdbcSerialization io = new JdbcSerialization();
    final AppConfig config = new AppConfig();
    io.setConfig(config);
    io.connect(null);
    io.createTables();
    final Volume vol = new Volume();
    final String path = "/db/path";
    vol.setPath(path);
    final Directory root = new Directory();
    vol.setRoot(root);
    final VolumeMapper volumeMapper = io.getVolumeMapper();
    final boolean inserted = volumeMapper.insert(io, vol);
    Assert.assertTrue("Inserting volume works.", inserted);
    final List<Volume> volsOut = io.loadAll();
    Assert.assertEquals("Load all on one-element database returns list of size one.", 1, volsOut.size());
    final Volume loadedVolume = volsOut.get(0);
    Assert.assertEquals("Loaded volume has same path as save volume.", path, loadedVolume.getPath());
    io.close();
  }
}
