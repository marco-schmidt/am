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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;

/**
 * Abstract base class for converting between {@link Model} objects on the one hand and {@link ResultSet} and
 * {@link PreparedStatement} objects on the other hand.
 *
 * @author Marco Schmidt
 *
 * @param <T>
 *          class derived from {@link Model}
 */
public abstract class ModelMapper<T extends Model>
{
  private static final Logger LOGGER = LoggerFactory.getLogger(ModelMapper.class);
  protected static final String ROWID = "rowid";
  private AppConfig config;

  abstract T create();

  abstract String getTableDefinition();

  abstract String getTableName();

  public Map<Long, T> toMap(List<T> list)
  {
    final Map<Long, T> result = new HashMap<>();
    for (final T t : list)
    {
      result.put(t.getId(), t);
    }
    return result;
  }

  /**
   * Convert from a {@link ResultSet} to a {@link Model} object.
   *
   * @param rs
   *          result set to read from
   * @return new {@link Model} object initialized with data
   */
  public T from(ResultSet rs)
  {
    final T result = create();
    try
    {
      result.setId(rs.getLong(ROWID));
      return result;
    }
    catch (final SQLException e)
    {
      LOGGER.error("failed", e);
      return null;
    }
  }

  public List<T> loadAll(JdbcSerialization io)
  {
    final AppConfig config = io.getConfig();
    final List<T> result = new ArrayList<>();
    final long timeMillis = System.currentTimeMillis();
    final PreparedStatement stat = createSelectAll(io);
    if (stat == null)
    {
      return null;
    }
    ResultSet resultSet = null;
    try
    {
      resultSet = stat.executeQuery();
      while (resultSet.next())
      {
        final T model = from(resultSet);
        result.add(model);
      }
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug(config.msg("init.debug.database_loaded", result.size(), this.getClass().getSimpleName(),
            System.currentTimeMillis() - timeMillis));
      }
    }
    catch (final SQLException e)
    {
      e.printStackTrace();
      result.clear();
    }
    finally
    {
      io.close(resultSet);
      io.close(stat);
    }
    return result;
  }

  private PreparedStatement createSelectAll(JdbcSerialization io)
  {
    if (io.isConnected())
    {
      return io.prepare(getSelectAllQuery());
    }
    else
    {
      return null;
    }
  }

  private PreparedStatement createInsert(JdbcSerialization io)
  {
    if (io.isConnected())
    {
      return io.prepare(getInsertQuery());
    }
    else
    {
      return null;
    }
  }

  private PreparedStatement createUpdate(JdbcSerialization io)
  {
    if (io.isConnected())
    {
      return io.prepare(getUpdateQuery());
    }
    else
    {
      return null;
    }
  }

  public abstract void to(PreparedStatement stat, T model, boolean appendModelId);

  public void upsert(JdbcSerialization io, T model)
  {
    final Long id = model.getId();
    if (id == null)
    {
      insert(io, model);
    }
    else
    {
      update(io, model);
    }
  }

  public boolean insert(JdbcSerialization io, T model)
  {
    final PreparedStatement stat = createInsert(io);
    ResultSet generatedKeys = null;
    if (stat == null)
    {
      return false;
    }
    try
    {
      to(stat, model, false);
      stat.executeUpdate();
      generatedKeys = stat.getGeneratedKeys();
      if (generatedKeys.next())
      {
        final long key = generatedKeys.getLong(1);
        model.setId(key);
      }
      return true;
    }
    catch (final SQLException e)
    {
      e.printStackTrace();
      return false;
    }
    finally
    {
      io.close(generatedKeys);
      io.close(stat);
    }
  }

  public void update(JdbcSerialization io, T model)
  {
    final PreparedStatement stat = createUpdate(io);
    if (stat == null)
    {
      return;
    }
    try
    {
      to(stat, model, true);
      stat.executeUpdate();
    }
    catch (final SQLException e)
    {
      e.printStackTrace();
    }
    finally
    {
      io.close(stat);
    }
  }

  public String getSelectAllQuery()
  {
    return "select " + ROWID + ", * from " + getTableName() + ";";
  }

  public abstract String getInsertQuery();

  public abstract String getUpdateQuery();

  public static void setString(PreparedStatement stat, int index, String value) throws SQLException
  {
    if (value == null)
    {
      stat.setNull(index, Types.VARCHAR);
    }
    else
    {
      stat.setString(index, value);
    }
  }

  public static void setLong(PreparedStatement stat, int index, Long value) throws SQLException
  {
    if (value == null)
    {
      stat.setNull(index, Types.BIGINT);
    }
    else
    {
      stat.setLong(index, value);
    }
  }

  public String getInsertQuery(String[] columnNames)
  {
    final StringBuilder sb = new StringBuilder();
    sb.append("insert into ");
    sb.append(getTableName());
    sb.append('(');
    boolean first = true;
    for (final String col : columnNames)
    {
      if (first)
      {
        first = false;
      }
      else
      {
        sb.append(',');
      }
      sb.append(col);
    }
    sb.append(") values (");
    int num = columnNames.length;
    while (num > 0)
    {
      if (num != columnNames.length)
      {
        sb.append(',');
      }
      sb.append('?');
      num--;
    }
    sb.append(')');
    return sb.toString();
  }

  public String getUpdateQuery(String[] columnNames)
  {
    final StringBuilder sb = new StringBuilder();
    sb.append("update ");
    sb.append(getTableName());
    sb.append(" set ");
    boolean first = true;
    for (final String col : columnNames)
    {
      if (first)
      {
        first = false;
      }
      else
      {
        sb.append(',');
      }
      sb.append(col);
      sb.append("=?");
    }
    sb.append(" where ");
    sb.append(ROWID);
    sb.append(" =?");
    return sb.toString();
  }

  public String getCreateTableQuery()
  {
    return "create table if not exists " + getTableName() + " (\n" + getTableDefinition() + ");\n";
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
