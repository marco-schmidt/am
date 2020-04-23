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

import java.util.ListResourceBundle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.Volume;

/**
 * Test {@link MovieValidator} class.
 */
public class MovieValidatorTest
{
  private AppConfig config;
  private MovieValidator validator;
  private Volume volume;
  private Directory root;
  private Directory yearDir;

  public static class TestBundle extends ListResourceBundle
  {
    @Override
    protected Object[][] getContents()
    {
      return new Object[][]
      {
          {
              "s2", "1"
          },
      };
    }
  }

  @Before
  public void setUp()
  {
    initialize();
  }

  private void initialize()
  {
    config = new AppConfig();
    config.setBundle(new TestBundle());
    validator = new MovieValidator();
    validator.setConfig(config);
    volume = new Volume();
    root = new Directory();
    volume.setRoot(root);
    yearDir = new Directory();
    yearDir.setName("2019");
    root.add(yearDir);
  }

  @Test
  public void testGetAsResolutionull()
  {
    Assert.assertNull("Null input leads to null output.", validator.getAsResolution(null));
  }

  @Test
  public void testGetAsResolutionNonNumber()
  {
    Assert.assertNull("Non-number input leads to null output.", validator.getAsResolution("xyp"));
  }

  @Test
  public void testGetAsResolutionOne()
  {
    Assert.assertEquals("Input '1p' leads to Long one output.", Long.valueOf(1), validator.getAsResolution("1p"));
  }

  @Test
  public void testGetAsResolution1080P()
  {
    Assert.assertEquals("Input '1080P' leads to Long one output.", Long.valueOf(1080),
        validator.getAsResolution("1080P"));
  }

  @Test
  public void testValidateFileNameNoExtension()
  {
    final File file = new File();
    file.setName("x");
    validator.validateFileName(file, Long.valueOf(1999));
    Assert.assertTrue("Missing file extension.", validator.containsOnly(MovieValidator.VIOLATION_FILE_NO_EXTENSION));
  }

  @Test
  public void testIsValidVideoFileExtensionNull()
  {
    Assert.assertFalse("Null is not valid.", validator.isValidVideoFileExtension(null));
  }

  @Test
  public void testIsValidVideoFileExtensionEmpty()
  {
    Assert.assertFalse("Empty is not valid.", validator.isValidVideoFileExtension(""));
  }

  @Test
  public void testIsValidVideoFileExtensionMkv()
  {
    Assert.assertTrue("'mkv' is  valid.", validator.isValidVideoFileExtension("mkv"));
  }

  @Test
  public void testValidateNoViolations()
  {
    initialize();
    validator.validate(config, volume);
    Assert.assertTrue("No violations with empty volume.", validator.isViolationsEmpty());
  }

  @Test
  public void testValidateViolationFileRootDirectory()
  {
    initialize();
    final File file = new File();
    file.setName("title.2019.mkv");
    root.add(file);
    validator.validate(config, volume);
    Assert.assertTrue("Has one violation file in root.",
        validator.contains(MovieValidator.VIOLATION_FILE_WRONG_DIRECTORY));
  }

  @Test
  public void testValidateViolationSubDirectoryTooDeep()
  {
    initialize();
    final Directory dir = new Directory();
    dir.setName("sub");
    yearDir.add(dir);
    validator.validate(config, volume);
    Assert.assertTrue("Has one violation about the directory structure being too deep.",
        validator.contains(MovieValidator.VIOLATION_DIRECTORY_TOO_DEEP));
  }

  @Test
  public void testValidateViolationInvalidYearDirectory()
  {
    initialize();
    final Directory invalidYear = new Directory();
    invalidYear.setName("x");
    root.add(invalidYear);
    validator.validate(config, volume);
    Assert.assertTrue("Has one violation about the directory not being a valid year.",
        validator.contains(MovieValidator.VIOLATION_DIRECTORY_NOT_A_NUMBER));
  }

  @Test
  public void testValidateViolationYearDirectoryTooSmall()
  {
    initialize();
    final Directory invalidYear = new Directory();
    invalidYear.setName("1850");
    root.add(invalidYear);
    validator.validate(config, volume);
    Assert.assertTrue("Has one violation about the directory not being a valid year (too small).",
        validator.contains(MovieValidator.VIOLATION_DIRECTORY_YEAR_TOO_SMALL));
  }

  @Test
  public void testValidateViolationYearDirectoryTooLarge()
  {
    initialize();
    final Directory invalidYear = new Directory();
    invalidYear.setName(Integer.toString(Integer.MAX_VALUE));
    root.add(invalidYear);
    validator.validate(config, volume);
    Assert.assertTrue("Has one violation about the directory not being a valid year (too large).",
        validator.contains(MovieValidator.VIOLATION_DIRECTORY_YEAR_TOO_LARGE));
  }
}
