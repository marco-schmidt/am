/*
 * Copyright 2019, 2020, 2021, 2022 the original author or authors.
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.VideoFileName;
import am.filesystem.model.Volume;
import am.services.wikidata.WikidataConfiguration;
import am.services.wikidata.WikidataEntity;
import am.services.wikidata.WikidataService;
import am.util.StrUtil;

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
  /**
   * No files in root directory allowed.
   */
  public static final String VIOLATION_NO_FILES_IN_ROOT = "no_files_in_root_directory";
  /**
   * No files in year directory allowed.
   */
  public static final String VIOLATION_NO_FILES_IN_YEAR_DIRECTORY = "no_files_in_year_directory";
  /**
   * No files in show directory allowed.
   */
  public static final String VIOLATION_NO_FILES_IN_SHOW_DIRECTORY = "no_files_in_show_directory";
  /**
   * Year too small.
   */
  public static final String VIOLATION_DIRECTORY_YEAR_TOO_SMALL = "directory_year_too_small";
  /**
   * Year too large.
   */
  public static final String VIOLATION_DIRECTORY_YEAR_TOO_LARGE = "directory_year_too_large";
  /**
   * Directory must be a number.
   */
  public static final String VIOLATION_DIRECTORY_NOT_A_NUMBER = "directory_not_a_number";
  /**
   * Duplicate season directory in show directory, e.g. "1" and "01".
   */
  public static final String VIOLATION_DUPLICATE_SEASON_DIRECTORY = "duplicate_season_directory";
  /**
   * Directory must be a number.
   */
  public static final String VIOLATION_SEASON_DIRECTORY_NOT_A_NUMBER = "season_directory_not_a_number";
  /**
   * Season directory number too small.
   */
  public static final String VIOLATION_SEASON_DIRECTORY_NUMBER_TOO_SMALL = "season_directory_number_too_small";
  /**
   * No directories in season directory.
   */
  public static final String VIOLATION_NO_DIRECTORIES_IN_SEASON_DIRECTORY = "no_directories_in_season_directory";
  /**
   * Season in file name differs from season directory.
   */
  public static final String VIOLATION_EPISODE_SEASON_AND_SEASON_DIRECTORY_DIFFER = "episode_season_and_season_directory_differ";
  private static final Pattern FILE_NAME_PATTERN = Pattern.compile("(.+)[sS](\\d+)[eE](\\d+)(.*)\\.(.*)");
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

    markFilesInvalid(dir, VIOLATION_NO_FILES_IN_ROOT);
  }

  private int findMaxTelevisionYear()
  {
    final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.ROOT);
    final int maxYear = cal.get(Calendar.YEAR) + 1;
    return maxYear;
  }

  private void validateYearEntries(AppConfig config, Directory dir)
  {
    LOGGER.debug(config.msg("tvseriesvalidator.debug.entering_year_directory",
        dir.getEntry() == null ? dir.getName() : dir.getEntry().getAbsolutePath()));

    Integer year;
    try
    {
      year = Integer.valueOf(dir.getName());
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

    markFilesInvalid(dir, VIOLATION_NO_FILES_IN_YEAR_DIRECTORY);

    for (final Directory sub : dir.getSubdirectories())
    {
      validateShowEntries(config, sub, year);
    }
  }

  private void validateShowEntries(AppConfig config, Directory dir, Integer year)
  {
    LOGGER.debug(config.msg("tvseriesvalidator.debug.entering_show_directory",
        dir.getEntry() == null ? dir.getName() : dir.getEntry().getAbsolutePath()));

    markFilesInvalid(dir, VIOLATION_NO_FILES_IN_SHOW_DIRECTORY);

    findShowWikidataEntity(config, dir, year);

    final Map<BigInteger, Directory> mapSeasonNumberToDirectory = new HashMap<>();
    final Map<String, Directory> mapMissing = new HashMap<>();
    final String showName = dir.getName();
    final List<Entry<BigInteger, Directory>> seasons = new ArrayList<Map.Entry<BigInteger, Directory>>();
    for (final Directory sub : dir.getSubdirectories())
    {
      final BigInteger number = StrUtil.getAsBigInteger(sub.getName());
      if (number == null)
      {
        addViolation(sub, VIOLATION_SEASON_DIRECTORY_NOT_A_NUMBER);
      }
      else
      {
        if (number.compareTo(BigInteger.ZERO) < 1)
        {
          addViolation(sub, VIOLATION_SEASON_DIRECTORY_NUMBER_TOO_SMALL);
        }
        else
        {
          if (mapSeasonNumberToDirectory.containsKey(number))
          {
            addViolation(sub, VIOLATION_DUPLICATE_SEASON_DIRECTORY);
          }
          else
          {
            mapSeasonNumberToDirectory.put(number, sub);
            if (sub.getWikidataEntityId() == null)
            {
              mapMissing.put(number.toString(), sub);
            }
          }
          seasons.add(new AbstractMap.SimpleEntry<BigInteger, Directory>(number, sub));
        }
      }
    }

    // find season-specific semantic information
    final String showEntityId = dir.getWikidataEntityId();
    final WikidataService service = config.getWikidataConfiguration().getService();
    if (showEntityId != null && !showEntityId.equals(WikidataEntity.UNKNOWN_ENTITY) && service != null
        && !mapMissing.isEmpty())
    {
      service.searchTelevisionSeasons(dir, showEntityId, mapMissing);
      service.assignUnknownEntityWhereNull(mapMissing.values());
    }

    // find episode information for each season
    for (final Entry<BigInteger, Directory> entry : seasons)
    {
      validateSeasonEntries(config, entry.getValue(), showName, entry.getKey());
    }
  }

  private void findShowWikidataEntity(AppConfig config, Directory dir, Integer year)
  {
    if (dir.getWikidataEntityId() != null)
    {
      return;
    }
    final WikidataConfiguration wikiConfig = config.getWikidataConfiguration();
    if (wikiConfig.isEnabled())
    {
      WikidataService service = wikiConfig.getService();
      if (service == null)
      {
        service = new WikidataService();
        service.setAppConfig(config);
        service.setConfig(wikiConfig);
        wikiConfig.setService(service);
      }
      String entityId = service.searchTelevisionShow(dir.getName(), year);
      if (entityId == null)
      {
        entityId = WikidataEntity.UNKNOWN_ENTITY;
      }
      dir.setWikidataEntityId(entityId);
      LOGGER.info(config.msg("tvseriesvalidator.info.television_show", entityId, dir.getName(), year.toString()));
    }
  }

  private void validateSeasonEntries(AppConfig config, Directory dir, String showName, BigInteger seasonNumber)
  {
    LOGGER.trace(config.msg("tvseriesvalidator.trace.entering_season_directory",
        dir.getEntry() == null ? dir.getName() : dir.getEntry().getAbsolutePath(), showName));

    markDirectoriesInvalid(dir, VIOLATION_NO_DIRECTORIES_IN_SEASON_DIRECTORY);

    final Map<Long, File> map = new HashMap<>();
    for (final File file : dir.getFiles())
    {
      validateEpisodeEntry(config, file, seasonNumber);
      if (file.getWikidataEntityId() == null)
      {
        final VideoFileName videoFileName = file.getVideoFileName();
        final Long firstEpisode = videoFileName == null ? null : videoFileName.getFirstEpisode();
        if (firstEpisode != null)
        {
          map.put(firstEpisode, file);
        }
      }
    }
    final WikidataService service = config.getWikidataConfiguration().getService();
    service.searchTelevisionEpisodes(dir.getWikidataEntityId(), map);
    service.assignUnknownEntityWhereNull(map.values());
  }

  private void validateEpisodeEntry(AppConfig config, File file, BigInteger seasonNumber)
  {
    LOGGER.trace(config.msg("tvseriesvalidator.trace.checking_episode_file",
        file.getEntry() == null ? file.getName() : file.getEntry().getAbsolutePath()));
    final VideoFileName videoFileName = parseFileName(file.getName());
    file.setVideoFileName(videoFileName);
    if (seasonNumber != null)
    {
      final Long nameSeason = videoFileName.getSeason();
      if (nameSeason == null || !BigInteger.valueOf(nameSeason).equals(seasonNumber))
      {
        addViolation(file, VIOLATION_EPISODE_SEASON_AND_SEASON_DIRECTORY_DIFFER);
      }
    }
  }

  public VideoFileName parseFileName(final String name)
  {
    final VideoFileName result = new VideoFileName();
    if (name == null)
    {
      return result;
    }
    final Matcher matcher = FILE_NAME_PATTERN.matcher(name);
    if (matcher.matches())
    {
      String showTitle = matcher.group(1);
      if (showTitle != null)
      {
        showTitle = showTitle.trim();
      }
      final String seasonString = matcher.group(2);
      final String episodeString = matcher.group(3);
      String episodeTitle = matcher.group(4);
      if (episodeTitle != null)
      {
        episodeTitle = episodeTitle.trim();
      }

      result.setTitle(showTitle);
      result.setSeason(Long.valueOf(seasonString));
      result.setFirstEpisode(Long.valueOf(episodeString));
      result.setLastEpisode(result.getFirstEpisode());
      result.setEpisodeTitle(episodeTitle);
    }
    return result;
  }
}
