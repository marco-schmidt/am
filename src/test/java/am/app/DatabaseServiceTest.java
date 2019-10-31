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
package am.app;

import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import am.db.JdbcSerialization;
import am.db.ModelMapper;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.Volume;

/**
 * Test {@link DatabaseService}.
 *
 * @author Marco Schmidt
 */
public class DatabaseServiceTest
{
  @Test
  public void testAddVolumeSuccess()
  {
    final AppConfig config = new AppConfig();
    config.setAddVolumePath(".");
    final JdbcSerialization io = new JdbcSerialization();
    io.setConfig(config);
    config.setDatabaseSerializer(io);
    final boolean connected = io.connect(null);
    Assert.assertTrue("In-memory connect works.", connected);
    io.createTables();
    final DatabaseService service = new DatabaseService();
    final boolean inserted = service.addVolume(config);
    Assert.assertTrue("Insert works.", inserted);
    final List<Volume> list = io.loadAll();
    Assert.assertEquals("Loaded one volume.", 1, list.size());
  }

  @Test
  public void testAddVolumeNonExistingDirectory()
  {
    final AppConfig config = new AppConfig();
    config.setAddVolumePath("Directory name does not exist 3124");
    final JdbcSerialization io = new JdbcSerialization();
    io.setConfig(config);
    config.setDatabaseSerializer(io);
    final boolean connected = io.connect(null);
    Assert.assertTrue("In-memory connect works.", connected);
    io.createTables();
    final DatabaseService service = new DatabaseService();
    final boolean inserted = service.addVolume(config);
    Assert.assertFalse("Insert failed.", inserted);
    final List<Volume> list = io.loadAll();
    Assert.assertTrue("Loaded no volumes.", list.isEmpty());
  }

  @Test
  public void testAddVolumeFailToCanonicalize()
  {
    final AppConfig config = new AppConfig();
    config.setAddVolumePath("Directory\000");
    final JdbcSerialization io = new JdbcSerialization();
    io.setConfig(config);
    config.setDatabaseSerializer(io);
    final boolean connected = io.connect(null);
    Assert.assertTrue("In-memory connect works.", connected);
    io.createTables();
    final DatabaseService service = new DatabaseService();
    final boolean inserted = service.addVolume(config);
    Assert.assertFalse("Insert failed.", inserted);
    final List<Volume> list = io.loadAll();
    Assert.assertTrue("Loaded no volumes.", list.isEmpty());
  }

  @Test
  public void testAddVolumeNoConnection()
  {
    final AppConfig config = new AppConfig();
    config.setAddVolumePath(".");
    final DatabaseService service = new DatabaseService();
    final boolean inserted = service.addVolume(config);
    Assert.assertFalse("Insert failed.", inserted);
  }

  @Test
  public void testAddVolumeFailDuplicateInsertion()
  {
    final AppConfig config = new AppConfig();
    config.setAddVolumePath(".");
    final JdbcSerialization io = new JdbcSerialization();
    io.setConfig(config);
    config.setDatabaseSerializer(io);
    final boolean connected = io.connect(null);
    Assert.assertTrue("In-memory connect works.", connected);
    io.createTables();
    final DatabaseService service = new DatabaseService();
    boolean inserted = service.addVolume(config);
    Assert.assertTrue("Insert works.", inserted);
    final List<Volume> list = io.loadAll();
    Assert.assertEquals("Loaded one volume.", 1, list.size());
    inserted = service.addVolume(config);
    Assert.assertFalse("Second insert attempt fails.", inserted);
  }

  @Test
  public void testAddVolumeFailUnknownValidator()
  {
    final AppConfig config = new AppConfig();
    config.setAddVolumePath(".");
    config.setAddVolumeValidator("unknown-validator");
    final JdbcSerialization io = new JdbcSerialization();
    io.setConfig(config);
    config.setDatabaseSerializer(io);
    final boolean connected = io.connect(null);
    Assert.assertTrue("In-memory connect works.", connected);
    io.createTables();
    final DatabaseService service = new DatabaseService();
    final boolean inserted = service.addVolume(config);
    Assert.assertFalse("Insert failed.", inserted);
  }

  @Test
  public void testDeleteVolumeSuccess()
  {
    final String volumePath = ".";
    final AppConfig config = new AppConfig();
    config.setAddVolumePath(volumePath);
    final JdbcSerialization io = new JdbcSerialization();
    io.setConfig(config);
    config.setDatabaseSerializer(io);
    final boolean connected = io.connect(null);
    Assert.assertTrue("In-memory connect works.", connected);
    io.createTables();
    final DatabaseService service = new DatabaseService();
    service.addVolume(config);
    List<Volume> list = io.loadAll();
    Assert.assertEquals("Loaded one volume.", 1, list.size());
    config.setDeleteVolumePath(volumePath);
    service.deleteVolume(config);
    list = io.loadAll();
    Assert.assertTrue("Loaded zero volumes.", list.isEmpty());
  }

  @Test
  public void testDeleteVolumeFailureCanonicalization()
  {
    final AppConfig config = new AppConfig();
    config.setDeleteVolumePath("\000");
    final JdbcSerialization io = new JdbcSerialization();
    io.setConfig(config);
    config.setDatabaseSerializer(io);
    final boolean connected = io.connect(null);
    Assert.assertTrue("In-memory connect works.", connected);
    io.createTables();
    final DatabaseService service = new DatabaseService();
    final boolean deleted = service.deleteVolume(config);
    Assert.assertFalse("Deletion failed.", deleted);
  }

  @Test
  public void testDeleteVolumeFailureNoConnection()
  {
    final AppConfig config = new AppConfig();
    config.setDeleteVolumePath(".");
    final DatabaseService service = new DatabaseService();
    final boolean deleted = service.deleteVolume(config);
    Assert.assertFalse("Deletion failed.", deleted);
  }

  @Test
  public void testDeleteVolumeFailureVolumeUnknown()
  {
    final String volumePath = ".";
    final AppConfig config = new AppConfig();
    final JdbcSerialization io = new JdbcSerialization();
    io.setConfig(config);
    config.setDatabaseSerializer(io);
    final boolean connected = io.connect(null);
    Assert.assertTrue("In-memory connect works.", connected);
    io.createTables();
    final DatabaseService service = new DatabaseService();
    config.setDeleteVolumePath(volumePath);
    final boolean deleted = service.deleteVolume(config);
    Assert.assertFalse("Deletion failed.", deleted);
  }

  @Test
  public void testUpdateWikidataEntityIdSuccess()
  {
    final AppConfig config = new AppConfig();
    config.setAddVolumePath(".");
    final JdbcSerialization io = new JdbcSerialization();
    io.setConfig(config);
    config.setDatabaseSerializer(io);
    final boolean connected = io.connect(null);
    Assert.assertTrue("In-memory connect works.", connected);
    io.createTables();
    final DatabaseService service = new DatabaseService();
    service.addVolume(config);
    final List<Volume> list = io.loadAll();
    Assert.assertEquals("Loaded one volume.", 1, list.size());
    final Volume vol = list.iterator().next();
    final Directory root = new Directory();
    root.setName("");
    root.setVolumeRef(vol.getId());
    boolean inserted = io.getDirectoryMapper().insert(io, root);
    Assert.assertTrue("Inserted root directory.", inserted);
    final File file = new File();
    final String fileName = "x";
    final Long fileSize = Long.valueOf(0);
    file.setName(fileName);
    file.setByteSize(fileSize);
    file.setWikidataEntityId(null);
    file.setDirectoryRef(root.getId());
    file.setVolumeRef(vol.getId());
    file.setLastModified(new Date());
    inserted = io.getFileMapper().insert(io, file);
    Assert.assertTrue("Inserted one file.", inserted);
    final String entId = "Q42";
    file.setWikidataEntityId(entId);
    final boolean updated = service.updateWikidataEntityId(config, file);
    Assert.assertTrue("File update succeeded.", updated);
    final List<File> files = io.getFileMapper().loadByField(io, ModelMapper.ID, file.getId());
    Assert.assertTrue("Loaded one file.", files != null && files.size() == 1);
    Assert.assertEquals("Loaded file wikidata id like saved one.", entId, files.get(0).getWikidataEntityId());
  }

  @Test
  public void testUpdateWikidataEntityIdFailureNoConnection()
  {
    final AppConfig config = new AppConfig();
    final File file = new File();
    final boolean updated = new DatabaseService().updateWikidataEntityId(config, file);
    Assert.assertFalse("File update failed.", updated);
  }
}
