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
package am.app.validators;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.Volume;

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
  private static final String VIOLATION_FILE_NO_EXTENSION = "nofileextension";
  private static final String VIOLATION_FILE_EXTENSION_UNKNOWN = "fileextensionunknown";
  private static final String VIOLATION_FILE_WRONG_DIRECTORY = "fileinwrongdirectory";
  private static final String VIOLATION_DIRECTORY_TOO_DEEP = "directorytoodeep";

  /**
   * Smallest value allowed for the year a movie was created.
   *
   * @see <a href=
   *      "https://en.wikipedia.org/wiki/Sallie_Gardner_at_a_Gallop">https://en.wikipedia.org/wiki/Sallie_Gardner_at_a_Gallop</a>
   */
  private static final int MIN_MOVIE_YEAR = 1878;

  @Override
  public void validate(AppConfig config, Volume volume)
  {
    validateDirectory(config, volume.getRoot(), 0);
  }

  private int findMaxMovieYear()
  {
    final Calendar cal = new GregorianCalendar();
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
        // name invalid, not a number
        year = null;
      }

      // make sure that number is a reasonable year
      if (year != null)
      {
        if (year < MIN_MOVIE_YEAR)
        {
          // number too small
          year = null;
        }
        else
        {
          final int maxYear = findMaxMovieYear();
          if (year > maxYear)
          {
            // number too large
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
      validateFile(config, dir, year, level, file);
    }
  }

  private void validateFile(AppConfig config, Directory dir, Long year, int level, File file)
  {
    if (level != 1)
    {
      addViolation(file, VIOLATION_FILE_WRONG_DIRECTORY);
    }

    validateFileName(dir, file, year);
  }

  // null
  // ""
  // abcd
  // abcd.mkv
  // 1941.mkv
  // 1941.1979.mkv
  // A Movie Title.1979.mkv
  // 1941.1979.neitheryearnorresolution.mkv
  // 1941.1979.1080p.mkv
  private void validateFileName(Directory dir, File file, Long dirYear)
  {
    final List<String> parts = Arrays.asList(file.getName().split("\\."));
    Collections.reverse(parts);
    final Iterator<String> iter = parts.iterator();

    // file extension
    if (!iter.hasNext())
    {
      addViolation(file, VIOLATION_FILE_NO_EXTENSION);
    }
    final String ext = iter.next();
    if (!isValidVideoFileExtension(ext))
    {
      addViolation(file, VIOLATION_FILE_EXTENSION_UNKNOWN);
    }
  }

  private boolean isValidVideoFileExtension(String ext)
  {
    // TODO: real implementation
    return ext != null && ("mkv".equals(ext) || "mp4".equals(ext) || "avi".equals(ext));
  }

  @Override
  public String getMessagePrefix()
  {
    return "movievalidator";
  }
}
