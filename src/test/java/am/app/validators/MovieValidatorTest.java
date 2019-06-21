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
  static class TestBundle extends ListResourceBundle
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

  private AppConfig config;
  private MovieValidator validator;
  private Volume volume;
  private Directory root;

  @Before
  public void setup()
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
}