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
package am.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.filesystem.model.Volume;

/**
 * {@link ModelMapper} for the {@link Volume} class.
 *
 * @author Marco Schmidt
 */
public class VolumeMapper extends ModelMapper<Volume>
{
  private static final Logger LOGGER = LoggerFactory.getLogger(VolumeMapper.class);
  private static final String TABLE_VOLUMES = "volumes";
  private static final String TABLE_VOLUMES_MAIN = "main";
  private static final String TABLE_VOLUMES_MAIN_REF = "main_ref";
  private static final String TABLE_VOLUMES_PATH = "path";
  private static final String TABLE_VOLUMES_VALIDATOR = "validator";
  private static final String[] COLUMNS =
  {
      TABLE_VOLUMES_PATH, TABLE_VOLUMES_MAIN, TABLE_VOLUMES_MAIN_REF, TABLE_VOLUMES_VALIDATOR
  };

  @Override
  protected Volume create()
  {
    return new Volume();
  }

  @Override
  protected String getTableName()
  {
    return TABLE_VOLUMES;
  }

  @Override
  public Volume from(ResultSet rs)
  {
    final Volume vol = super.from(rs);
    try
    {
      vol.setPath(rs.getString(TABLE_VOLUMES_PATH));
      vol.setMain(rs.getInt(TABLE_VOLUMES_MAIN) != 0);
      vol.setMainRef(rs.getLong(TABLE_VOLUMES_MAIN_REF));
      vol.setValidator(rs.getString(TABLE_VOLUMES_VALIDATOR));
    }
    catch (final SQLException e)
    {
      LOGGER.error(getConfig().msg("database.error.convert_from_result_set", this.getClass().getSimpleName()), e);
    }
    return vol;
  }

  @Override
  public void to(PreparedStatement stat, Volume vol, boolean appendModelId)
  {
    try
    {
      stat.setString(1, vol.getPath());
      stat.setInt(2, vol.isMain() ? 1 : 0);
      setLong(stat, 3, vol.getMainRef());
      stat.setString(4, vol.getValidator());
      if (appendModelId)
      {
        stat.setLong(5, vol.getId());
      }
    }
    catch (final SQLException e)
    {
      LOGGER.error(getConfig().msg("database.error.convert_to_prepared_statement", this.getClass().getSimpleName()), e);
    }
  }

  @Override
  protected String getTableDefinition()
  {
    return TABLE_VOLUMES_PATH + " text,\n" + TABLE_VOLUMES_MAIN + " int,\n" + TABLE_VOLUMES_MAIN_REF + " bigint,\n"
        + TABLE_VOLUMES_VALIDATOR + " text\n";
  }

  @Override
  public String getInsertQuery()
  {
    return getInsertQuery(COLUMNS);
  }

  @Override
  public String getUpdateQuery()
  {
    return getUpdateQuery(COLUMNS);
  }

  public Volume loadByPath(JdbcSerialization io, final String path)
  {
    final List<Volume> list = loadByField(io, TABLE_VOLUMES_PATH, path);
    return list != null && !list.isEmpty() ? list.get(0) : null;
  }
}
