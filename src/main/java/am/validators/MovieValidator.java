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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.FileState;
import am.filesystem.model.VideoFileName;
import am.filesystem.model.Volume;
import am.services.wikidata.WikidataConfiguration;
import am.services.wikidata.WikidataEntity;
import am.util.StrUtil;

/**
 * Validate a movie volume.
 *
 * There are several rules for a movie volume:
 * <ul>
 * <li>This directory has a tree depth of one. Only one level of subdirectories is allowed.</li>
 * <li>All files are in subdirectories.</li>
 * <li>All directories in the root level have names that represent years, so they must contain four digits. Valid years
 * are 1880 to the next year.</li>
 * <li>Files can either store a movie or contain metadata for a movie file.</li>
 * <li>File names contain two or more dots, separating name parts.</li>
 * <li>The last name part is the file extension denoting the file format, e.g. mkv. It must be one of the extensions
 * allowed for file type group video.</li>
 * <li>Other name parts are the release year (same as directory name), optionally the vertical resolution terminated
 * with p (e.g. 1080p) and the title of the movie.</li>
 * </ul>
 *
 * @author Marco Schmidt
 */
public class MovieValidator extends AbstractValidator
{
  /**
   * File name has no file extension.
   */
  public static final String VIOLATION_FILE_NO_EXTENSION = "no_file_extension";
  private static final String VIOLATION_FILE_EXTENSION_UNKNOWN = "file_extension_unknown";
  /**
   * File not allowed in this directory.
   */
  public static final String VIOLATION_FILE_WRONG_DIRECTORY = "file_in_wrong_directory";
  /**
   * Directory too deep, only root and year directories allowed.
   */
  public static final String VIOLATION_DIRECTORY_TOO_DEEP = "directory_too_deep";
  /**
   * Year directory not a number.
   */
  public static final String VIOLATION_DIRECTORY_NOT_A_NUMBER = "directory_not_a_number";
  /**
   * Year directory too small.
   */
  public static final String VIOLATION_DIRECTORY_YEAR_TOO_SMALL = "directory_year_too_small";
  /**
   * Year directory too large.
   */
  public static final String VIOLATION_DIRECTORY_YEAR_TOO_LARGE = "directory_year_too_large";
  private static final String VIOLATION_FILE_NO_YEAR = "file_no_year";
  private static final String VIOLATION_FILE_DIRECTORY_YEAR_DIFFER = "file_dir_year_differ";
  private static final String VIOLATION_TITLE_MISSING = "file_title_missing";
  private static final String VIOLATION_FILE_NAME_STRUCTURE = "file_name_structure";

  /**
   * Smallest value allowed for the year a movie was created.
   *
   * @see <a href=
   *      "https://en.wikipedia.org/wiki/Sallie_Gardner_at_a_Gallop">https://en.wikipedia.org/wiki/Sallie_Gardner_at_a_Gallop</a>
   */
  private static final int MIN_MOVIE_YEAR = 1878;
  private static final Set<String> VIDEO_METADATA_FILE_EXTENSIONS = new HashSet<>(Arrays.asList(new String[]
  {
      "vsmeta"
  }));

  private static final Logger LOGGER = LoggerFactory.getLogger(MovieValidator.class);

  @Override
  public void validate(AppConfig config, Volume volume)
  {
    validateDirectory(config, volume.getRoot(), 0);
  }

  private int findMaxMovieYear()
  {
    final Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ROOT);
    final int maxYear = cal.get(Calendar.YEAR) + 1;
    return maxYear;
  }

  private void validateDirectory(AppConfig config, Directory dir, int level)
  {
    if (level > 1)
    {
      addViolation(dir, VIOLATION_DIRECTORY_TOO_DEEP);
    }

    Long year;
    if (level == 1)
    {
      // directory name must be movie year; try to parse it
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
        if (year < MIN_MOVIE_YEAR)
        {
          addViolation(dir, VIOLATION_DIRECTORY_YEAR_TOO_SMALL);
          year = null;
        }
        else
        {
          final int maxYear = findMaxMovieYear();
          if (year > maxYear)
          {
            addViolation(dir, VIOLATION_DIRECTORY_YEAR_TOO_LARGE);
            year = null;
          }
        }
      }
    }
    else
    {
      year = null;
    }

    for (final Directory sub : dir.getSubdirectories())
    {
      validateDirectory(config, sub, level + 1);
    }

    for (final File file : dir.getFiles())
    {
      validateFile(year, level, file);
    }
  }

  private void validateFile(Long year, int level, File file)
  {
    final FileState state = file.getState();
    if (state != null && state == FileState.Missing)
    {
      return;
    }
    if (level != 1)
    {
      addViolation(file, VIOLATION_FILE_WRONG_DIRECTORY);
    }

    validateFileName(file, year);

    assignWikidataEntity(file);
  }

  private void assignWikidataEntity(File file)
  {
    // if we already know the entity id or we failed to retrieve it in the past ("?") we don't need to fetch it again
    final String id = file.getWikidataEntityId();
    if (id != null && !id.isEmpty())
    {
      return;
    }

    // are we supposed to retrieve Wikidata information?
    final AppConfig config = getConfig();
    final WikidataConfiguration wikidataConfiguration = config.getWikidataConfiguration();
    if (wikidataConfiguration == null || !wikidataConfiguration.isEnabled())
    {
      return;
    }

    // use title and year
    final VideoFileName videoFileName = file.getVideoFileName();
    final String query = videoFileName.getTitle();
    if (query == null || query.isEmpty())
    {
      return;
    }

    queryWikidata(config, wikidataConfiguration, file, videoFileName, query);
  }

  private void queryWikidata(AppConfig config, WikidataConfiguration wikidataConfiguration, File file,
      VideoFileName videoFileName, String query)
  {
    // reuse fetcher if it already exists
    WikibaseDataFetcher fetcher = wikidataConfiguration.getFetcher();
    if (fetcher == null)
    {
      fetcher = WikibaseDataFetcher.getWikidataDataFetcher();
      wikidataConfiguration.setFetcher(fetcher);
    }
    try
    {
      long millis = System.currentTimeMillis();
      final List<WbSearchEntitiesResult> list = fetcher.searchEntities(query, "en", Long.valueOf(10));
      millis = System.currentTimeMillis() - millis;
      boolean success = false;
      if (list.isEmpty())
      {
        LOGGER.warn(config.msg("movievalidator.warn.wikidata_no_result", query, millis));
      }
      else
      {
        success = parseResults(list, file, query, videoFileName.getYear(), millis);
      }
      if (!success)
      {
        file.setWikidataEntityId(WikidataEntity.UNKNOWN_ENTITY);
      }
    }
    catch (final IOException ioe)
    {
      LOGGER.error(getConfig().msg("movievalidator.error.connect_wikidata_failure"), ioe);
    }
    catch (final MediaWikiApiErrorException e)
    {
      LOGGER.error(getConfig().msg("movievalidator.error.connect_wikidata_failure"), e);
    }
  }

  private boolean parseResults(List<WbSearchEntitiesResult> list, File file, String query, Long year, long millis)
  {
    final Iterator<WbSearchEntitiesResult> iter = list.iterator();
    while (iter.hasNext())
    {
      final WbSearchEntitiesResult result = iter.next();
      final String description = result.getDescription();
      if (year != null && description != null && description.contains(year.toString()))
      {
        assignWikidataEntity(file, query, result, millis);
        return true;
      }
    }
    return false;
  }

  private void assignWikidataEntity(File file, String query, WbSearchEntitiesResult result, long millis)
  {
    final String entityId = result.getEntityId();
    LOGGER.info(getConfig().msg("movievalidator.info.wikidata_result", query, entityId, result.getDescription(),
        result.getLabel(), millis));
    file.setWikidataEntityId(entityId);
  }

  private String last(List<String> list)
  {
    if (list == null || list.isEmpty())
    {
      return null;
    }
    else
    {
      return list.remove(list.size() - 1);
    }
  }

  public Long getAsResolution(final String s)
  {
    if (s != null && s.length() > 1 && (s.endsWith("p") || s.endsWith("P")))
    {
      final String num = s.substring(0, s.length() - 1);
      return StrUtil.getAsLong(num);
    }
    else
    {
      return null;
    }
  }

  public void validateFileName(File file, Long dirYear)
  {
    final String name = file.getName();
    final String[] items = name.split("\\.");
    if (items.length < 2)
    {
      addViolation(file, VIOLATION_FILE_NO_EXTENSION);
      return;
    }

    final List<String> list = new ArrayList<>(Arrays.asList(items));

    if (isValidVideoMetadataExtension(list))
    {
      list.remove(list.size() - 1);
    }
    if (!isValidVideoFileExtension(last(list)))
    {
      addViolation(file, VIOLATION_FILE_EXTENSION_UNKNOWN);
    }

    final VideoFileName videoFileName = new VideoFileName();
    while (!list.isEmpty())
    {
      final String s = last(list);

      final boolean consumed = consumeYear(videoFileName, s) || consumeResolution(videoFileName, s);

      if (!consumed)
      {
        list.add(s);
        break;
      }
    }
    checkGeneralFileRules(file, list, videoFileName, dirYear);
    videoFileName.setTitle(String.join(" ", list));
    file.setVideoFileName(videoFileName);
  }

  private void checkGeneralFileRules(File file, List<String> list, VideoFileName videoFileName, Long dirYear)
  {
    final int listSize = list.size();
    if (listSize == 0)
    {
      addViolation(file, VIOLATION_TITLE_MISSING);
    }
    else
      if (listSize > 1)
      {
        addViolation(file, VIOLATION_FILE_NAME_STRUCTURE);
      }
    final Long year = videoFileName.getYear();
    if (year == null)
    {
      addViolation(file, VIOLATION_FILE_NO_YEAR);
    }
    else
    {
      if (!year.equals(dirYear))
      {
        addViolation(file, VIOLATION_FILE_DIRECTORY_YEAR_DIFFER);
      }
    }
  }

  private boolean isValidVideoMetadataExtension(List<String> list)
  {
    final String elem = list != null && !list.isEmpty() ? list.get(list.size() - 1) : null;
    return elem == null ? false : VIDEO_METADATA_FILE_EXTENSIONS.contains(elem);
  }

  private boolean consumeResolution(VideoFileName videoFileName, String s)
  {
    final Long res = getAsResolution(s);
    if (res == null)
    {
      return false;
    }
    if (videoFileName.getResolution() == null)
    {
      videoFileName.setResolution(res);
      return true;
    }
    else
    {
      return false;
    }
  }

  private boolean consumeYear(final VideoFileName videoFileName, final String yearString)
  {
    final Long year = StrUtil.getAsLong(yearString);
    if (year == null)
    {
      return false;
    }
    if (videoFileName.getYear() == null)
    {
      videoFileName.setYear(year);
      return true;
    }
    else
    {
      return false;
    }
  }

  public boolean isValidVideoFileExtension(String ext)
  {
    return ext != null && !"".equals(ext); // EXT.contains(ext);
  }

  @Override
  public String getMessagePrefix()
  {
    return "movievalidator";
  }
}
