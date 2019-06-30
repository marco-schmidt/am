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
import am.filesystem.model.Volume;

/**
 * Mapper for the {@link Volume} class.
 *
 * @author Marco Schmidt
 */
public class VolumeMapper extends ModelMapper<Volume>
{
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
  Volume create()
  {
    return new Volume();
  }

  @Override
  String getTableName()
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
    }
    return vol;
  }

  @Override
  public void to(PreparedStatement stat, Volume vol)
  {
    try
    {
      stat.setString(1, vol.getPath());
      stat.setInt(2, vol.isMain() ? 1 : 0);
      final Long mainRef = vol.getMainRef();
      if (mainRef == null)
      {
        stat.setNull(3, Types.BIGINT);
      }
      else
      {
        stat.setLong(3, mainRef.longValue());
      }
      stat.setString(4, vol.getValidator());
    }
    catch (final SQLException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  String getTableDefinition()
  {
    return TABLE_VOLUMES_PATH + " text,\n" + TABLE_VOLUMES_MAIN + " int,\n" + TABLE_VOLUMES_MAIN_REF + " bigint,\n"
        + TABLE_VOLUMES_VALIDATOR + " text\n";
  }

  @Override
  public String getInsertQuery()
  {
    return getInsertQuery(COLUMNS);
  }
}
