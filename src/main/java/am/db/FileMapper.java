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
import java.util.Date;
import am.filesystem.model.File;
import am.filesystem.model.FileState;

/**
 * {@link ModelMapper} for the {@link File} class.
 *
 * @author Marco Schmidt
 */
public class FileMapper extends ModelMapper<File>
{
  private static final String TABLE_FILES = "files";
  private static final String COL_VOLUME_REF = "volume_ref";
  private static final String COL_DIR_REF = "dir_ref";
  private static final String COL_NAME = "name";
  private static final String COL_SIZE = "size";
  private static final String COL_LAST_MODIFIED = "last_modified";
  private static final String COL_MIME_TYPE = "mime_type";
  private static final String COL_FILE_GROUP = "file_group";
  private static final String COL_FILE_TYPE = "file_type";
  private static final String COL_STATE = "state";
  private static final String COL_HASH_VALUE = "hash_value";
  private static final String COL_HASH_CREATED = "hash_created";
  private static final String[] COLUMNS =
  {
      COL_VOLUME_REF, COL_DIR_REF, COL_NAME, COL_SIZE, COL_LAST_MODIFIED, COL_MIME_TYPE, COL_FILE_GROUP, COL_FILE_TYPE,
      COL_STATE, COL_HASH_VALUE, COL_HASH_CREATED
  };

  @Override
  File create()
  {
    return new File();
  }

  @Override
  String getTableName()
  {
    return TABLE_FILES;
  }

  @Override
  public File from(ResultSet rs)
  {
    final File file = super.from(rs);
    try
    {
      file.setVolumeRef(rs.getLong(COL_VOLUME_REF));
      file.setDirectoryRef(rs.getLong(COL_DIR_REF));
      file.setName(rs.getString(COL_NAME));
      file.setByteSize(rs.getLong(COL_SIZE));
      file.setLastModified(rs.getDate(COL_LAST_MODIFIED));
      file.setMimeType(rs.getString(COL_MIME_TYPE));
      file.setFileGroup(rs.getString(COL_FILE_GROUP));
      file.setFileType(rs.getString(COL_FILE_TYPE));
      file.setState(FileState.values()[rs.getInt(COL_STATE)]);
      file.setHashValue(rs.getString(COL_HASH_VALUE));
      file.setHashCreated(rs.getDate(COL_HASH_CREATED));
    }
    catch (final SQLException e)
    {
      e.printStackTrace();
    }
    return file;
  }

  @Override
  public void to(PreparedStatement stat, File file, boolean appendModelId)
  {
    try
    {
      stat.setLong(1, file.getVolumeRef());
      ModelMapper.setLong(stat, 2, file.getDirectoryRef());
      stat.setString(3, file.getName());
      stat.setLong(4, file.getByteSize());
      stat.setLong(5, file.getLastModified().getTime());
      stat.setString(6, file.getMimeType());
      stat.setString(7, file.getFileGroup());
      stat.setString(8, file.getFileType());
      stat.setInt(9, file.getState().getNumericValue());
      ModelMapper.setString(stat, 10, file.getHashValue());
      final Date hashCreated = file.getHashCreated();
      ModelMapper.setLong(stat, 11, hashCreated == null ? null : hashCreated.getTime());
      if (appendModelId)
      {
        stat.setLong(12, file.getId());
      }

    }
    catch (final SQLException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  String getTableDefinition()
  {
    return COL_VOLUME_REF + " bigint,\n" + COL_DIR_REF + " bigint,\n" + COL_NAME + " text,\n" + COL_SIZE + " bigint,\n"
        + COL_LAST_MODIFIED + " integer,\n" + COL_MIME_TYPE + " text,\n" + COL_FILE_GROUP + " text,\n" + COL_FILE_TYPE
        + " text,\n" + COL_STATE + " int,\n" + COL_HASH_VALUE + " text,\n" + COL_HASH_CREATED + " integer\n";
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
