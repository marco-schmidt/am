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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.FileSystemHelper;
import am.filesystem.model.Volume;

/**
 * Connect to relational database with JDBC.
 *
 * @author Marco Schmidt
 */
public class JdbcSerialization
{
  private static final Logger LOGGER = LoggerFactory.getLogger(JdbcSerialization.class);
  private static final String ROWID = "rowid";
  private static final String TABLE_VOLUMES = "volumes";
  private static final String TABLE_VOLUMES_MAIN = "main";
  private static final String TABLE_VOLUMES_MAIN_REF = "main_ref";
  private static final String TABLE_VOLUMES_PATH = "path";
  private static final String TABLE_VOLUMES_VALIDATOR = "validator";
  private AppConfig config;
  private Connection conn;
  private String uri;

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

  public void connect(File dir)
  {
    if (conn != null)
    {
      return;
    }
    uri = createConnectorString(dir);
    try
    {
      conn = DriverManager.getConnection(uri);
      LOGGER.info(config.msg("init.info.database_connection_attempt_succeeded", uri));
    }
    catch (final SQLException e)
    {
      LOGGER.error(config.msg("init.error.database_connection_attempt_failed", uri), e);
      uri = null;
    }
  }

  public String createConnectorString(File dir)
  {
    final File dbFile = new File(dir, "am.db");
    final String path = FileSystemHelper.normalizePath(dbFile.getAbsolutePath());
    return "jdbc:sqlite:" + path;
  }

  public void createTables()
  {
    createTable(TABLE_VOLUMES, TABLE_VOLUMES_PATH + " text,\n" + TABLE_VOLUMES_MAIN + " int,\n" + TABLE_VOLUMES_MAIN_REF
        + " bigint,\n" + TABLE_VOLUMES_VALIDATOR + " text\n");
  }

  private void createTable(String tableName, String definition)
  {
    final String sql = "create table if not exists " + tableName + " (\n" + definition + ");\n";
    executeUpdate(sql);
  }

  public List<Volume> loadVolumes()
  {
    final List<Volume> result = new ArrayList<>();
    final PreparedStatement stat = createSelectAll(TABLE_VOLUMES);
    ResultSet resultSet = null;
    try
    {
      resultSet = stat.executeQuery();
      while (resultSet.next())
      {
        final int main = resultSet.getInt(TABLE_VOLUMES_MAIN);
        final long mainRef = resultSet.getLong(TABLE_VOLUMES_MAIN_REF);
        final String path = resultSet.getString(TABLE_VOLUMES_PATH);
        final String validator = resultSet.getString(TABLE_VOLUMES_VALIDATOR);
        final Volume vol = new Volume();
        vol.setPath(path);
        vol.setMain(main != 0);
        vol.setMainRef(mainRef);
        vol.setValidator(validator);
        vol.setId(resultSet.getLong(ROWID));
        result.add(vol);
      }
    }
    catch (final SQLException e)
    {
      result.clear();
    }
    finally
    {
      close(resultSet);
      close(stat);
    }
    return result;
  }

  private PreparedStatement createSelectAll(String tableName)
  {
    if (isConnected())
    {
      try
      {
        return conn.prepareStatement("select " + ROWID + ", * from " + tableName);
      }
      catch (final SQLException e)
      {
        LOGGER.error(config.msg(""), e);
        return null;
      }
    }
    else
    {
      return null;
    }
  }

  private String escape(String sql)
  {
    return sql.replace('\n', ' ');
  }

  private void executeUpdate(String sql)
  {
    Statement stat = null;
    try
    {
      stat = conn.createStatement();
      stat.executeUpdate(sql);
    }
    catch (final SQLException e)
    {
      LOGGER.error(config.msg("init.error.database_execute_update_failed", escape(sql)), e);
    }
    finally
    {
      close(stat);
    }
  }

  private void close(ResultSet rs)
  {
    if (rs != null)
    {
      try
      {
        rs.close();
      }
      catch (final SQLException e)
      {
      }
    }
  }

  private void close(Statement stat)
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
  }
}
