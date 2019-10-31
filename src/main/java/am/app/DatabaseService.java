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

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.db.DirectoryMapper;
import am.db.FileMapper;
import am.db.JdbcSerialization;
import am.db.ModelMapper;
import am.db.SearchResult;
import am.db.VolumeMapper;
import am.filesystem.FileSystemHelper;
import am.filesystem.model.Volume;
import am.validators.AbstractValidator;

/**
 * Application functionality relating to the database.
 *
 * @author Marco Schmidt
 */
public class DatabaseService
{
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

  private Volume findVolumeByPath(AppConfig config, String path)
  {
    final JdbcSerialization io = config.getDatabaseSerializer();
    final VolumeMapper volumeMapper = io.getVolumeMapper();
    final List<Volume> volumes = volumeMapper.loadAll(io);
    for (final Volume v : volumes)
    {
      final String volPath = v.getPath();
      if (path.equals(volPath))
      {
        return v;
      }
    }
    return null;
  }

  /**
   * Add volume to database.
   *
   * @param config
   *          application configuration
   * @return success of volume addition
   */
  public boolean addVolume(AppConfig config)
  {
    String path = config.getAddVolumePath();
    final File dir = new File(path);

    // get normalized version of directory path to identify duplicates
    try
    {
      path = dir.getCanonicalPath();
    }
    catch (final IOException e)
    {
      LOGGER.error(config.msg("addvolume.error.cannot_get_canonical_directory_name", path), e);
      return false;
    }

    // is path a valid directory?
    if (!dir.isDirectory())
    {
      LOGGER.error(config.msg("addvolume.error.directory_invalid", path));
      return false;
    }

    // this only works with a database connection
    final JdbcSerialization io = config.getDatabaseSerializer();
    if (io == null)
    {
      LOGGER.error(config.msg("addvolume.error.no_database_connection"));
      return false;
    }

    // is this path already assigned to a volume?
    final Volume duplicate = findVolumeByPath(config, path);
    if (duplicate != null)
    {
      LOGGER.error(config.msg("addvolume.error.path_already_used", path, duplicate.getId()));
      return false;
    }

    // if optional validator name was provided, is it known?
    final Volume vol = new Volume();
    if (!checkValidator(config, vol))
    {
      return false;
    }

    vol.setPath(path);
    vol.setMain(true);

    // create new volume record in database
    final VolumeMapper volumeMapper = io.getVolumeMapper();
    final String validator = config.getAddVolumeValidator();
    if (volumeMapper.insert(io, vol))
    {
      LOGGER.info(config.msg("addvolume.info.added_volume_success", path, vol.getId(), validator));
      return true;
    }
    else
    {
      LOGGER.error(config.msg("addvolume.error.added_volume_failure", path, validator));
      return false;
    }
  }

  /**
   * Delete volume as specified in the configuration.
   *
   * @param config
   *          application configuration
   * @return true on success, false otherwise
   */
  public boolean deleteVolume(AppConfig config)
  {
    // get normalized version of directory path to identify duplicates
    String path = config.getDeleteVolumePath();
    final File dir = new File(path);
    try
    {
      path = dir.getCanonicalPath();
    }
    catch (final IOException e)
    {
      LOGGER.error(config.msg("deletevolume.error.cannot_get_canonical_directory_name", path), e);
      return false;
    }

    // this only works with a database connection
    final JdbcSerialization io = config.getDatabaseSerializer();
    if (io == null)
    {
      LOGGER.error(config.msg("deletevolume.error.no_database_connection"));
      return false;
    }

    // find volume
    final VolumeMapper volumeMapper = io.getVolumeMapper();
    final Volume volume = volumeMapper.loadByPath(io, path);
    if (volume == null)
    {
      LOGGER.error(config.msg("deletevolume.error.unknown_path", path));
      return false;
    }

    // delete files and directories referencing the volume and then the volume itself
    final FileMapper fileMapper = io.getFileMapper();
    final int numFiles = fileMapper.deleteByField(io, FileMapper.COL_VOLUME_REF, volume.getId());
    final DirectoryMapper dirMapper = io.getDirectoryMapper();
    final int numDirs = dirMapper.deleteByField(io, DirectoryMapper.TABLE_DIRS_VOLUME_REF, volume.getId());
    final int numVolumes = volumeMapper.deleteByField(io, ModelMapper.ROWID, volume.getId());
    LOGGER.info(config.msg("database.info.deleted_volume", numFiles, numDirs, numVolumes));
    return numVolumes == 1;
  }

  /**
   * If there is an optional validator type defined for a new volume make sure that it is valid.
   *
   * @param config
   *          application configuration
   * @param vol
   *          new volume which gets assigned validator name if valid
   * @return whether check was successful if validator was defined, true otherwise
   */
  private boolean checkValidator(AppConfig config, Volume vol)
  {
    final String validator = config.getAddVolumeValidator();
    if (validator != null)
    {
      final Class<? extends AbstractValidator> cl = AbstractValidator.findByName(validator);
      if (cl == null)
      {
        LOGGER.error(config.msg("addvolume.error.unknown_validator_type", validator));
        return false;
      }
      vol.setValidator(validator);
    }
    return true;
  }

  public boolean find(String name, List<Volume> volumes, SearchResult result)
  {
    result.clear();
    File entry = new File(name);
    try
    {
      entry = entry.getCanonicalFile();
    }
    catch (final IOException e)
    {
      return false;
    }
    String query = entry.getAbsolutePath();
    query = FileSystemHelper.normalizePath(query);
    for (final Volume vol : volumes)
    {
      final String path = vol.getPath();
      if (query.startsWith(path))
      {
        final String local = query.substring(path.length());
        result.setVolume(vol);
        find(local, vol, result);
        return true;
      }
    }
    return false;
  }

  public boolean updateWikidataEntityId(AppConfig config, am.filesystem.model.File file)
  {
    // this only works with a database connection
    final JdbcSerialization io = config.getDatabaseSerializer();
    if (io == null)
    {
      LOGGER.error(config.msg("deletevolume.error.no_database_connection"));
      return false;
    }
    final FileMapper fileMapper = io.getFileMapper();
    return fileMapper.update(io, file);
  }

  public boolean find(String name, Volume volume, SearchResult result)
  {
    return true;
  }
}
