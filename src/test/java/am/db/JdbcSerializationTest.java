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
package am.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
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

    // insert volume
    final Volume vol = new Volume();
    final String path = "/db/path";
    vol.setPath(path);
    final VolumeMapper volumeMapper = io.getVolumeMapper();
    boolean inserted = volumeMapper.insert(io, vol);
    Assert.assertTrue("Inserting volume works.", inserted);

    // insert volume's root directory
    final DirectoryMapper directoryMapper = io.getDirectoryMapper();
    final Directory root = new Directory();
    vol.setRoot(root);
    root.setVolumeRef(vol.getId());
    inserted = directoryMapper.insert(io, root);
    Assert.assertTrue("Inserting root directory works.", inserted);

    // find root directory by search for its id
    final List<Directory> dirs = directoryMapper.loadByField(io, ModelMapper.ID, root.getId());
    Assert.assertEquals("Searching for root directory rowid finds one dir.", 1, dirs.size());
    Assert.assertEquals("Searching for root directory rowid finds only root dir.", root.getId(), dirs.get(0).getId());

    // insert subdirectory
    final Directory sub = new Directory();
    sub.setName("sub");
    sub.setParentRef(root.getId());
    sub.setVolumeRef(vol.getId());
    root.add(sub);
    inserted = directoryMapper.insert(io, sub);
    Assert.assertTrue("Inserting subdirectory works.", inserted);

    // insert file
    final File file = new File();
    file.setName("file");
    file.setDirectoryRef(sub.getId());
    file.setVolumeRef(vol.getId());
    file.setByteSize(Long.valueOf(0));
    file.setLastModified(new Date());
    sub.add(file);
    final FileMapper fileMapper = io.getFileMapper();
    inserted = fileMapper.insert(io, file);
    Assert.assertTrue("Inserting file works.", inserted);

    final List<Volume> vols = new ArrayList<Volume>();
    vols.add(vol);
    io.saveAll(vols);

    final List<Volume> volsOut = io.loadAll();
    Assert.assertEquals("Load all on one-element database returns list of size one.", 1, volsOut.size());
    final Volume loadedVolume = volsOut.get(0);
    Assert.assertEquals("Loaded volume has same path as save volume.", path, loadedVolume.getPath());

    // delete subdirectory given its id
    final int numDeleted = directoryMapper.deleteByField(io, ModelMapper.ID, sub.getId());
    Assert.assertEquals("Deleting sub directory results in one deletion.", 1, numDeleted);

    io.close();
  }
}
