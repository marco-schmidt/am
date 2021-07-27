/*
 * Copyright 2019, 2020, 2021 the original author or authors.
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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.FileSystemHelper;
import am.filesystem.model.Directory;
import am.filesystem.model.Volume;

/**
 * Connect to relational database with JDBC.
 *
 * @author Marco Schmidt
 */
public class JdbcSerialization
{
  private static final Logger LOGGER = LoggerFactory.getLogger(JdbcSerialization.class);
  private AppConfig config;
  private Connection conn;
  private String uri;
  private VolumeMapper volumeMapper = new VolumeMapper();
  private DirectoryMapper directoryMapper = new DirectoryMapper();
  private FileMapper fileMapper = new FileMapper();

  public boolean isConnected()
  {
    return conn != null;
  }

  public void close()
  {
    try
    {
      if (conn != null)
      {
        conn.close();
        LOGGER.info(config.msg("init.info.database_connection_closed", uri));
      }
    }
    catch (final SQLException e)
    {
      LOGGER.error(config.msg("init.error.database_close_failed"), e);
    }
    finally
    {
      conn = null;
      uri = null;
    }
  }

  public boolean connect(File dir)
  {
    if (conn != null)
    {
      return true;
    }
    uri = dir == null ? createConnectorStringInMemory() : createConnectorString(dir);
    try
    {
      final long millis = System.currentTimeMillis();
      conn = DriverManager.getConnection(uri);
      LOGGER.debug(
          config.msg("init.debug.database_connection_attempt_succeeded", uri, System.currentTimeMillis() - millis));
      enableForeignKeys();
      return true;
    }
    catch (final SQLException e)
    {
      LOGGER.error(config.msg("init.error.database_connection_attempt_failed", uri), e);
      uri = null;
      return false;
    }
  }

  private boolean enableForeignKeys()
  {
    final PreparedStatement stat = prepare("PRAGMA foreign_keys = ON;");
    try
    {
      stat.execute();
      return true;
    }
    catch (final SQLException e)
    {
      return false;
    }
    finally
    {
      close(stat);
    }
  }

  public String createConnectorString(File dir)
  {
    final File dbFile = new File(dir, "am.db");
    final String path = FileSystemHelper.normalizePath(dbFile.getAbsolutePath());
    return "jdbc:sqlite:" + path;
  }

  public String createConnectorStringInMemory()
  {
    return "jdbc:sqlite::memory:";
  }

  public void createTables()
  {
    createTable(getVolumeMapper());
    createTable(getDirectoryMapper());
    createTable(getFileMapper());
  }

  private void createTable(ModelMapper<? extends Model> mapper)
  {
    if (isConnected())
    {
      final String query = mapper.getCreateTableQuery();
      PreparedStatement stat = null;
      try
      {
        stat = conn.prepareStatement(query);
        stat.execute();
      }
      catch (final SQLException e)
      {
        LOGGER.error("failed creation: " + query, e);
      }
      finally
      {
        close(stat);
      }
    }
  }

  /**
   * Create a {@link PreparedStatement} object from argument query.
   *
   * @param query
   *          SQL statement from which to create a {@link PreparedStatement} object
   * @return a {@link PreparedStatement} object on success or null if the attempt failed or there was no database
   *         connection
   */
  public PreparedStatement prepare(String query)
  {
    if (isConnected())
    {
      try
      {
        return conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      }
      catch (final SQLException e)
      {
        LOGGER.error(config.msg("database.error.prepare_statement_failed", query), e);
      }
    }
    return null;
  }

  /**
   * Close argument {@link ResultSet} to release any underlying resources.
   *
   * @param rs
   *          {@link ResultSet} object to be closed
   */
  public void close(ResultSet rs)
  {
    if (rs != null)
    {
      try
      {
        rs.close();
      }
      catch (final SQLException e)
      {
        LOGGER.error(config.msg("database.error.close_resultset_failed"), e);
      }
    }
  }

  public void close(Statement stat)
  {
    if (stat != null)
    {
      try
      {
        stat.close();
      }
      catch (final SQLException e)
      {
      }
    }
  }

  public AppConfig getConfig()
  {
    return config;
  }

  public void setConfig(AppConfig config)
  {
    this.config = config;
    fileMapper.setConfig(config);
    directoryMapper.setConfig(config);
    volumeMapper.setConfig(config);
  }

  public VolumeMapper getVolumeMapper()
  {
    return volumeMapper;
  }

  public void setVolumeMapper(VolumeMapper volumeMapper)
  {
    this.volumeMapper = volumeMapper;
  }

  public DirectoryMapper getDirectoryMapper()
  {
    return directoryMapper;
  }

  public void setDirectoryMapper(DirectoryMapper directoryMapper)
  {
    this.directoryMapper = directoryMapper;
  }

  public FileMapper getFileMapper()
  {
    return fileMapper;
  }

  public void setFileMapper(FileMapper fileMapper)
  {
    this.fileMapper = fileMapper;
  }

  public List<Volume> loadAll()
  {
    final List<Volume> vols = volumeMapper.loadAll(this);
    final Map<Long, Volume> volMap = volumeMapper.toMap(vols);
    final List<Directory> dirs = directoryMapper.loadAll(this);
    final Map<Long, Directory> dirMap = directoryMapper.toMap(dirs);
    for (final Directory d : dirs)
    {
      final Long parentRef = d.getParentRef();
      if (parentRef == null)
      {
        final Volume volume = volMap.get(d.getVolumeRef());
        volume.setRoot(d);
      }
      else
      {
        final Directory parent = dirMap.get(parentRef);
        parent.add(d);
      }
    }
    final List<am.filesystem.model.File> files = fileMapper.loadAll(this);
    for (final am.filesystem.model.File f : files)
    {
      final Long directoryRef = f.getDirectoryRef();
      final Directory directory = dirMap.get(directoryRef);
      directory.add(f);
    }
    return vols;
  }

  public void saveAll(List<Volume> vols)
  {
    final long millis = System.currentTimeMillis();
    for (final Volume vol : vols)
    {
      save(vol);
    }
    LOGGER.debug(config.msg("database.debug.database_saved", System.currentTimeMillis() - millis));
  }

  private void save(Volume vol)
  {
    final Directory root = vol.getRoot();
    root.setVolumeRef(vol.getId());
    save(root, null);
  }

  private void save(Directory dir, Long parentRef)
  {
    dir.setParentRef(parentRef);
    directoryMapper.upsert(this, dir);

    for (final Directory sub : dir.getSubdirectories())
    {
      sub.setVolumeRef(dir.getVolumeRef());
      save(sub, dir.getId());
    }

    for (final am.filesystem.model.File file : dir.getFiles())
    {
      file.setDirectoryRef(dir.getId());
      file.setVolumeRef(dir.getVolumeRef());
      fileMapper.upsert(this, file);
    }
  }
}
