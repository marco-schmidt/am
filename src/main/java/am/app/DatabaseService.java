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
import am.db.JdbcSerialization;
import am.db.VolumeMapper;
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

  private boolean isDuplicate(AppConfig config, String path)
  {
    final JdbcSerialization io = config.getDatabaseSerializer();
    final VolumeMapper volumeMapper = io.getVolumeMapper();
    final List<Volume> volumes = volumeMapper.loadAll(io);
    for (final Volume v : volumes)
    {
      final String volPath = v.getPath();
      if (path.equals(volPath))
      {
        LOGGER.error(config.msg("addvolume.error.path_already_used", path, v.getId()));
        return true;
      }
    }
    return false;
  }

  public void addVolume(AppConfig config)
  {
    // is path a valid directory?
    String path = config.getAddVolumePath();
    final File dir = new File(path);
    if (!dir.isDirectory())
    {
      LOGGER.error(config.msg("addvolume.error.directory_invalid", path));
      return;
    }

    // get normalized version of directory path to identify duplicates
    try
    {
      path = dir.getCanonicalPath();
    }
    catch (final IOException e)
    {
      LOGGER.error(config.msg("addvolume.error.cannot_get_canonical_directory_name", path), e);
      return;
    }

    // this only works with a database connection
    final JdbcSerialization io = config.getDatabaseSerializer();
    if (io == null)
    {
      LOGGER.error(config.msg("addvolume.error.no_database_connection"));
      return;
    }

    // is this path already assigned to a volume?
    if (isDuplicate(config, path))
    {
      return;
    }

    // if optional validator name was provided, is it known?
    final Volume vol = new Volume();
    if (!checkValidator(config, vol))
    {
      return;
    }

    vol.setPath(path);
    vol.setMain(true);

    // create new volume record in database
    final VolumeMapper volumeMapper = io.getVolumeMapper();
    final String validator = config.getAddVolumeValidator();
    if (volumeMapper.insert(io, vol))
    {
      LOGGER.info(config.msg("addvolume.info.added_volume_success", path, vol.getId(), validator));
    }
    else
    {
      LOGGER.error(config.msg("addvolume.error.added_volume_failure", path, validator));
    }
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
}
