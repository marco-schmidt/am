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
package am.validators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.Volume;

/**
 * Validate documents created by a person.
 *
 * There are several rules:
 * <ul>
 * <li>The root directory of a volume contains directories only.</li>
 * <li>All such directories contain a person's name.</li>
 * </ul>
 * <h2>Example Files</h2>
 * <p>
 * /home/johndoe/docs/john-doe/2019/2019-12-31/New_Years_Eve_2019_12_31_0001.JPG
 * </p>
 *
 * @author Marco Schmidt
 */
public class PersonalDocumentValidator extends AbstractValidator
{
  /**
   * File not allowed in this directory.
   */
  public static final String VIOLATION_FILE_WRONG_DIRECTORY = "file_in_wrong_directory";
  /**
   * XMP file without matching regular file ("test.xmp", but no "test.SOMETHING").
   */
  public static final String VIOLATION_XMP_FILE_WITHOUT_REGULAR_FILE = "xmp_without_file";
  /**
   * Directory too deep.
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

  @Override
  public void validate(AppConfig config, Volume volume)
  {
    final Directory root = volume.getRoot();
    markFilesInvalid(root, VIOLATION_FILE_WRONG_DIRECTORY);
    for (final Directory sub : root.getSubdirectories())
    {
      validateCreator(config, sub);
    }
  }

  private void validateCreator(AppConfig config, Directory dir)
  {
    markFilesInvalid(dir, VIOLATION_FILE_WRONG_DIRECTORY);
    final String creator = dir.getName();
    for (final Directory sub : dir.getSubdirectories())
    {
      validateCreatorYear(config, creator, sub);
    }
  }

  private void validateCreatorYear(AppConfig config, String creator, Directory dir)
  {
    markFilesInvalid(dir, VIOLATION_FILE_WRONG_DIRECTORY);
    for (final Directory sub : dir.getSubdirectories())
    {
      validateCreatorYearDay(config, creator, sub);
    }
  }

  private void validateCreatorYearDay(AppConfig config, String creator, Directory dir)
  {
    markDirectoriesInvalid(dir, VIOLATION_DIRECTORY_TOO_DEEP);
    final Map<String, File> xmp = new HashMap<>();
    final Set<String> regular = new HashSet<>();
    for (final File file : dir.getFiles())
    {
      final String name = file.getName();
      final int lastIndex = name.lastIndexOf('.');
      if (lastIndex < 0)
      {
        regular.add(name);
      }
      else
      {
        final String base = name.substring(0, lastIndex);
        final String ext = name.substring(lastIndex + 1);
        final String normExt = ext.toLowerCase(Locale.ENGLISH);
        if ("xmp".equals(normExt))
        {
          xmp.put(base, file);
        }
        else
        {
          regular.add(base);
        }
      }
    }
    for (final Entry<String, File> entry : xmp.entrySet())
    {
      final String key = entry.getKey();
      if (!regular.contains(key))
      {
        addViolation(entry.getValue(), VIOLATION_XMP_FILE_WITHOUT_REGULAR_FILE);
      }
    }
  }

  @Override
  public String getMessagePrefix()
  {
    return "personalmediavalidator";
  }
}
