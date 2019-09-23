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
package am.validators;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.Volume;

/**
 * Validate a television series volume.
 *
 * <ul>
 * <li>A volume root directory contains only directories, each one named like a year.</li>
 * <li>Each year directory contains only directories, each representing and named like one television series that was
 * first published during that year.</li>
 * <li>Each television series directory contains only directories, each representing a season.</li>
 * <li>A season directory has a decimal number as its name, leading zeroes allowed.</li>
 * <li>A season directory may contain files only.</li>
 * <li>Each file is a video file containing an episode of the season, or metadata (vsmeta, srt, and so on).</li>
 * <li>Video files start with the name of the series as written two directories above, followed by a space and the
 * episode number in format SxxEyy. Here xx is the season number from the file's directory followed by the number of the
 * episode within the season.</li>
 * </ul>
 *
 * @author Marco Schmidt
 */
public class TvSeriesValidator extends AbstractValidator
{
  /**
   * First year of television, see <a href="https://en.wikipedia.org/wiki/History_of_television">History of
   * television</a>.
   */
  private static final int MIN_TELEVISION_YEAR = 1926;
  private static final String NO_FILES_IN_ROOT = "no_files_in_root_directory";
  private static final String NO_FILES_IN_YEAR_DIRECTORY = "no_files_in_year_directory";
  private static final String VIOLATION_DIRECTORY_YEAR_TOO_SMALL = "directory_year_too_small";
  private static final String VIOLATION_DIRECTORY_YEAR_TOO_LARGE = "directory_year_too_large";
  private static final String VIOLATION_DIRECTORY_NOT_A_NUMBER = "directory_not_a_number";
  private static final String VIOLATION_DUPLICATE_SEASON_DIRECTORY = "duplicate_season_directory";
  private static final String VIOLATION_SEASON_DIRECTORY_NOT_A_NUMBER = "season_directory_not_a_number";
  private static final String VIOLATION_SEASON_DIRECTORY_NUMBER_TOO_SMALL = "season_directory_number_too_small";
  private static final Logger LOGGER = LoggerFactory.getLogger(TvSeriesValidator.class);

  @Override
  public String getMessagePrefix()
  {
    return "tvseriesvalidator";
  }

  @Override
  public void validate(AppConfig config, Volume volume)
  {
    validateRootDirectoryEntries(config, volume.getRoot());
  }

  private void validateRootDirectoryEntries(AppConfig config, Directory dir)
  {
    for (final Directory sub : dir.getSubdirectories())
    {
      validateYearEntries(config, sub);
    }

    markFilesInvalid(dir, NO_FILES_IN_ROOT);
  }

  private int findMaxTelevisionYear()
  {
    final Calendar cal = new GregorianCalendar();
    final int maxYear = cal.get(Calendar.YEAR) + 1;
    return maxYear;
  }

  private void validateYearEntries(AppConfig config, Directory dir)
  {
    LOGGER.debug(config.msg("tvseriesvalidator.debug.entering_year_directory", dir.getEntry().getAbsolutePath()));

    Long year;
    try
    {
      year = Long.valueOf(dir.getName());
    }
    catch (final NumberFormatException nfe)
    {
      addViolation(dir, VIOLATION_DIRECTORY_NOT_A_NUMBER);
      year = null;
    }

    // make sure that number is a reasonable year
    if (year != null)
    {
      if (year < MIN_TELEVISION_YEAR)
      {
        addViolation(dir, VIOLATION_DIRECTORY_YEAR_TOO_SMALL);
        year = null;
      }
      else
      {
        final int maxYear = findMaxTelevisionYear();
        if (year > maxYear)
        {
          addViolation(dir, VIOLATION_DIRECTORY_YEAR_TOO_LARGE);
          year = null;
        }
      }
    }

    markFilesInvalid(dir, NO_FILES_IN_YEAR_DIRECTORY);

    for (final Directory sub : dir.getSubdirectories())
    {
      validateShowEntries(config, sub, year);
    }
  }

  private BigInteger getAsNumber(String s)
  {
    try
    {
      return new BigInteger(s);
    }
    catch (final NumberFormatException nfe)
    {
      return null;
    }
  }

  private void validateShowEntries(AppConfig config, Directory dir, Long year)
  {
    LOGGER.debug(config.msg("tvseriesvalidator.debug.entering_show_directory", dir.getEntry().getAbsolutePath()));

    markFilesInvalid(dir, NO_FILES_IN_YEAR_DIRECTORY);

    final Map<BigInteger, Directory> mapSeasonNumberToDirectory = new HashMap<>();
    for (final Directory sub : dir.getSubdirectories())
    {
      final BigInteger number = getAsNumber(sub.getName());
      if (number == null)
      {
        addViolation(dir, VIOLATION_SEASON_DIRECTORY_NOT_A_NUMBER);
      }
      else
      {
        if (number.compareTo(BigInteger.ZERO) < 1)
        {
          addViolation(dir, VIOLATION_SEASON_DIRECTORY_NUMBER_TOO_SMALL);
        }
        else
        {
          if (mapSeasonNumberToDirectory.containsKey(number))
          {
            addViolation(dir, VIOLATION_DUPLICATE_SEASON_DIRECTORY);
          }
          else
          {
            mapSeasonNumberToDirectory.put(number, sub);
          }
          validateSeasonEntries(config, sub, number);
        }
      }
    }
  }

  private void validateSeasonEntries(AppConfig config, Directory dir, BigInteger seasonNumber)
  {
    LOGGER.debug(config.msg("tvseriesvalidator.debug.entering_season_directory", dir.getEntry().getAbsolutePath()));

    markDirectoriesInvalid(dir, NO_FILES_IN_YEAR_DIRECTORY);

    for (final File file : dir.getFiles())
    {
      validateEpisodeEntry(config, file, seasonNumber);
    }
  }

  private void validateEpisodeEntry(AppConfig config, File file, BigInteger seasonNumber)
  {
    LOGGER.debug(config.msg("tvseriesvalidator.debug.checking_episode_file", file.getEntry().getAbsolutePath()));
  }
}
