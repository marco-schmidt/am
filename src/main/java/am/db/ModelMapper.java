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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  abstract T create();

  abstract String getTableDefinition();

  abstract String getTableName();

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
    final List<T> result = new ArrayList<>();
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
    }
    catch (final SQLException e)
    {
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

  public abstract void to(PreparedStatement stat, T model);

  public void insert(JdbcSerialization io, T model)
  {
    final PreparedStatement stat = createInsert(io);
    ResultSet generatedKeys = null;
    if (stat == null)
    {
      return;
    }
    try
    {
      to(stat, model);
      stat.executeUpdate();
      generatedKeys = stat.getGeneratedKeys();
      if (generatedKeys.next())
      {
        final long key = generatedKeys.getLong(1);
        model.setId(key);
      }
    }
    catch (final SQLException e)
    {
      e.printStackTrace();
    }
    finally
    {
      io.close(generatedKeys);
      io.close(stat);
    }
  }

  public String getSelectAllQuery()
  {
    return "select " + ROWID + ", * from " + getTableName() + ";";
  }

  public abstract String getInsertQuery();

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

  public String getCreateTableQuery()
  {
    return "create table if not exists " + getTableName() + " (\n" + getTableDefinition() + ");\n";
  }
}
