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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.filesystem.model.Directory;

/**
 * {@link ModelMapper} for the {@link Directory} class.
 *
 * @author Marco Schmidt
 */
public class DirectoryMapper extends ModelMapper<Directory>
{
  private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryMapper.class);
  private static final String TABLE_DIRS = "dirs";
  /**
   * Name of column referencing volume.
   */
  public static final String TABLE_DIRS_VOLUME_REF = "volume_ref";
  private static final String TABLE_DIRS_PARENT_REF = "parent_ref";
  private static final String TABLE_DIRS_NAME = "name";
  private static final String TABLE_DIRS_WIKIDATA_ENT_ID = "wikidata_ent_id";
  private static final String[] COLUMNS =
  {
      TABLE_DIRS_VOLUME_REF, TABLE_DIRS_PARENT_REF, TABLE_DIRS_NAME, TABLE_DIRS_WIKIDATA_ENT_ID
  };

  @Override
  Directory create()
  {
    return new Directory();
  }

  @Override
  String getTableName()
  {
    return TABLE_DIRS;
  }

  @Override
  public Directory from(ResultSet rs)
  {
    final Directory dir = super.from(rs);
    try
    {
      dir.setVolumeRef(rs.getLong(TABLE_DIRS_VOLUME_REF));
      final long parentRef = rs.getLong(TABLE_DIRS_PARENT_REF);
      dir.setParentRef(parentRef < 1 ? null : Long.valueOf(parentRef));
      dir.setName(rs.getString(TABLE_DIRS_NAME));
      dir.setWikidataEntityId(rs.getString(TABLE_DIRS_WIKIDATA_ENT_ID));
    }
    catch (final SQLException e)
    {
      LOGGER.error(getConfig().msg("database.error.convert_from_result_set", this.getClass().getSimpleName()), e);
    }
    return dir;
  }

  @Override
  public void to(PreparedStatement stat, Directory dir, boolean appendModelId)
  {
    try
    {
      stat.setLong(1, dir.getVolumeRef());
      setLong(stat, 2, dir.getParentRef());
      stat.setString(3, dir.getName());
      stat.setString(4, dir.getWikidataEntityId());
      if (appendModelId)
      {
        stat.setLong(5, dir.getId());
      }
    }
    catch (final SQLException e)
    {
      LOGGER.error(getConfig().msg("database.error.convert_to_prepared_statement", this.getClass().getSimpleName()), e);
    }
  }

  @Override
  String getTableDefinition()
  {
    return TABLE_DIRS_VOLUME_REF + " bigint,\n" + TABLE_DIRS_PARENT_REF + " bigint,\n" + TABLE_DIRS_NAME + " text,\n"
        + TABLE_DIRS_WIKIDATA_ENT_ID + " text\n";
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
}
