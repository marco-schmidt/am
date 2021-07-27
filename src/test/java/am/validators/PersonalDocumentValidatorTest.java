/*
 * Copyright 2019, 2020, 2021 the original author or authors.
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
 * Test {@link PersonalDocumentValidator} class.
 */
public class PersonalDocumentValidatorTest
{
  private AppConfig config;
  private PersonalDocumentValidator validator;
  private Volume volume;
  private Directory root;
  private Directory personDir;
  private Directory yearDir;
  private Directory dayDir;

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
    validator = new PersonalDocumentValidator();
    validator.setConfig(config);
    volume = new Volume();
    root = new Directory();
    volume.setRoot(root);
    personDir = new Directory();
    personDir.setName("john-doe");
    root.add(personDir);
    yearDir = new Directory();
    yearDir.setName("2020");
    personDir.add(yearDir);
    dayDir = new Directory();
    dayDir.setName("2020-01-01");
    yearDir.add(dayDir);
  }

  @Test
  public void testValidateNoViolations()
  {
    initialize();
    validator.validate(config, volume);
    Assert.assertTrue("No violations with empty volume.", validator.isViolationsEmpty());
  }

  @Test
  public void testValidateRootFile()
  {
    initialize();
    final File file = new File();
    file.setName("some.jpg");
    root.add(file);
    validator.validate(config, volume);
    Assert.assertTrue("File in root directory not allowed.",
        validator.containsOnly(PersonalDocumentValidator.VIOLATION_FILE_WRONG_DIRECTORY));
  }

  @Test
  public void testValidatePersonDirFile()
  {
    initialize();
    final File file = new File();
    file.setName("some.jpg");
    personDir.add(file);
    validator.validate(config, volume);
    Assert.assertTrue("File in person directory not allowed.",
        validator.containsOnly(PersonalDocumentValidator.VIOLATION_FILE_WRONG_DIRECTORY));
  }

  @Test
  public void testValidateYearFile()
  {
    initialize();
    final File file = new File();
    file.setName("some.jpg");
    yearDir.add(file);
    validator.validate(config, volume);
    Assert.assertTrue("File in year directory not allowed.",
        validator.containsOnly(PersonalDocumentValidator.VIOLATION_FILE_WRONG_DIRECTORY));
  }

  @Test
  public void testValidateDateDirDirectory()
  {
    initialize();
    final Directory sub = new Directory();
    sub.setName("test");
    dayDir.add(sub);
    validator.validate(config, volume);
    Assert.assertTrue("Directory in day directory not allowed.",
        validator.containsOnly(PersonalDocumentValidator.VIOLATION_DIRECTORY_TOO_DEEP));
  }

  @Test
  public void testValidateCreatorYearDay()
  {
    initialize();

    final File noExt = new File();
    noExt.setName("foo");
    dayDir.add(noExt);

    final File regWithXmp = new File();
    regWithXmp.setName("test.jpg");
    dayDir.add(regWithXmp);

    final File regXmp = new File();
    regXmp.setName("test.xmp");
    dayDir.add(regXmp);

    final File soloXmp = new File();
    soloXmp.setName("bar.xmp");
    dayDir.add(soloXmp);

    validator.validate(config, volume);
    Assert.assertTrue("Solo XMP in day directory not allowed.",
        validator.containsOnly(PersonalDocumentValidator.VIOLATION_XMP_FILE_WITHOUT_REGULAR_FILE));
  }
}
