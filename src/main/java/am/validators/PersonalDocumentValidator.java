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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.model.Directory;
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
  static final String VIOLATION_FILE_WRONG_DIRECTORY = "file_in_wrong_directory";
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

  private static final Logger LOGGER = LoggerFactory.getLogger(PersonalDocumentValidator.class);

  @Override
  public void validate(AppConfig config, Volume volume)
  {
    final Directory root = volume.getRoot();
    markFilesInvalid(root, VIOLATION_FILE_WRONG_DIRECTORY);
  }

  @Override
  public String getMessagePrefix()
  {
    return "personalmediavalidator";
  }
}
