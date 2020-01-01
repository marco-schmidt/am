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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.VideoFileName;
import am.filesystem.model.Volume;
import am.validators.MovieValidatorTest.TestBundle;

/**
 * Test {@link TvSeriesValidator} class.
 */
public class TvSeriesValidatorTest
{
  private static final String SHOW_TITLE = "Episode Show Name";
  private static final String EPISODE_TITLE = "The Episode Title";
  private static final Long SEASON = Long.valueOf(3);
  private static final Long EPISODE = Long.valueOf(17);
  private AppConfig config;
  private TvSeriesValidator validator;
  private Volume volume;
  private Directory root;
  private Directory year;
  private Directory show;
  private Directory season;
  private File file;

  @Before
  public void setup()
  {
    initialize();
  }

  private void initialize()
  {
    config = new AppConfig();
    config.setBundle(new TestBundle());
    validator = new TvSeriesValidator();
    validator.setConfig(config);
    validator.resetIds();
    volume = new Volume();
    root = new Directory();
    volume.setRoot(root);
    year = new Directory();
    year.setName("2019");
    year.setEntry(new java.io.File(year.getName()));
    root.add(year);
    show = new Directory();
    show.setName("Show Title");
    year.add(show);
    season = new Directory();
    season.setName("01");
    show.add(season);
    file = new File();
    file.setName("Show Title S01E01.mp4");
    season.add(file);
  }

  private void checkBasics(final VideoFileName name)
  {
    Assert.assertNull("Episode file name has null year because it is never set.", name.getYear());
  }

  @Test
  public void testParseVideoFileNameNull()
  {
    final VideoFileName name = validator.parseFileName(null);
    checkBasics(name);
    Assert.assertNull("Null file name leads to null show title.", name.getTitle());
    Assert.assertNull("Null file name leads to null episode title.", name.getEpisodeTitle());
    Assert.assertNull("Null file name leads to null season.", name.getSeason());
    Assert.assertNull("Null file name leads to null first episode.", name.getFirstEpisode());
    Assert.assertNull("Null file name leads to null last episode.", name.getLastEpisode());
    Assert.assertNull("Null file name leads to null resolution.", name.getResolution());
  }

  @Test
  public void testParseVideoFileNameRegularWithoutEpisodeTitle()
  {
    final String fileName = SHOW_TITLE + " S0" + SEASON.toString() + "E" + EPISODE.toString() + ".mkv";
    final VideoFileName name = validator.parseFileName(fileName);
    checkBasics(name);
    Assert.assertEquals("Specific show name expected.", SHOW_TITLE, name.getTitle());
    Assert.assertEquals("Empty episode title.", "", name.getEpisodeTitle());
    Assert.assertEquals("Specific season expected.", SEASON, name.getSeason());
    Assert.assertEquals("Specific first episode expected.", EPISODE, name.getFirstEpisode());
    Assert.assertEquals("Specific last episode expected.", EPISODE, name.getLastEpisode());
    Assert.assertNull("Missing resolution in file name leads to null resolution.", name.getResolution());
  }

  @Test
  public void testParseVideoFileNameRegularWithEpisodeTitle()
  {
    final String fileName = SHOW_TITLE + " S0" + SEASON.toString() + "E" + EPISODE.toString() + " " + EPISODE_TITLE
        + ".mkv";
    final VideoFileName name = validator.parseFileName(fileName);
    checkBasics(name);
    Assert.assertEquals("Specific show name expected.", SHOW_TITLE, name.getTitle());
    Assert.assertEquals("Empty episode title.", EPISODE_TITLE, name.getEpisodeTitle());
    Assert.assertEquals("Specific season expected.", SEASON, name.getSeason());
    Assert.assertEquals("Specific first episode expected.", EPISODE, name.getFirstEpisode());
    Assert.assertEquals("Specific last episode expected.", EPISODE, name.getLastEpisode());
    Assert.assertNull("Missing resolution in file name leads to null resolution.", name.getResolution());
  }

  @Test
  public void testValidateCorrect()
  {
    initialize();
    validator.validate(config, volume);
    Assert.assertTrue("Regular setup leads to no rule violations.", validator.isViolationsEmpty());
  }

  @Test
  public void testValidateFileRoot()
  {
    initialize();
    final File rootFile = new File();
    root.add(rootFile);
    validator.validate(config, volume);
    Assert.assertTrue("File in root directory is determined as only violation.",
        validator.containsOnly(TvSeriesValidator.VIOLATION_NO_FILES_IN_ROOT));
  }

  @Test
  public void testValidateFileYear()
  {
    initialize();
    final File yearFile = new File();
    year.add(yearFile);
    validator.validate(config, volume);
    Assert.assertTrue("File in year directory is determined as only violation.",
        validator.containsOnly(TvSeriesValidator.VIOLATION_NO_FILES_IN_YEAR_DIRECTORY));
  }

  @Test
  public void testValidateFileShow()
  {
    initialize();
    final File f = new File();
    show.add(f);
    validator.validate(config, volume);
    Assert.assertTrue("File in show directory is determined as only violation.",
        validator.containsOnly(TvSeriesValidator.VIOLATION_NO_FILES_IN_SHOW_DIRECTORY));
  }

  @Test
  public void testValidateDirSeason()
  {
    initialize();
    final Directory d = new Directory();
    season.add(d);
    validator.validate(config, volume);
    Assert.assertTrue("Directory in season directory is determined as only violation.",
        validator.containsOnly(TvSeriesValidator.VIOLATION_NO_DIRECTORIES_IN_SEASON_DIRECTORY));
  }

  @Test
  public void testValidateYearTooSmall()
  {
    initialize();
    final Directory d = new Directory();
    d.setName("1900");
    root.add(d);
    validator.validate(config, volume);
    Assert.assertTrue("Directory with a year too small must be only violation.",
        validator.containsOnly(TvSeriesValidator.VIOLATION_DIRECTORY_YEAR_TOO_SMALL));
  }

  @Test
  public void testValidateYearTooLarge()
  {
    initialize();
    final Directory d = new Directory();
    d.setName(Integer.toString(Integer.MAX_VALUE));
    root.add(d);
    validator.validate(config, volume);
    Assert.assertTrue("Directory with a year too large must be only violation.",
        validator.containsOnly(TvSeriesValidator.VIOLATION_DIRECTORY_YEAR_TOO_LARGE));
  }

  @Test
  public void testValidateShowYearNotANumber()
  {
    initialize();
    final Directory d = new Directory();
    d.setName("a");
    root.add(d);
    validator.validate(config, volume);
    Assert.assertTrue("Year directory not a number must be only violation.",
        validator.containsOnly(TvSeriesValidator.VIOLATION_DIRECTORY_NOT_A_NUMBER));
  }

  @Test
  public void testValidateSeasonNotANumber()
  {
    initialize();
    final Directory d = new Directory();
    d.setName("a");
    show.add(d);
    validator.validate(config, volume);
    Assert.assertTrue("Show subdirectory not a number must be only violation.",
        validator.containsOnly(TvSeriesValidator.VIOLATION_SEASON_DIRECTORY_NOT_A_NUMBER));
  }

  @Test
  public void testValidateDuplicateSeasonDirectory()
  {
    initialize();
    final Directory d = new Directory();
    d.setName("1");
    show.add(d);
    validator.validate(config, volume);
    Assert.assertTrue("Duplicate season subdirectory must be only violation.",
        validator.containsOnly(TvSeriesValidator.VIOLATION_DUPLICATE_SEASON_DIRECTORY));
  }

  @Test
  public void testValidateSeasonDirectoryNumberTooSmall()
  {
    initialize();
    final Directory d = new Directory();
    d.setName("0");
    d.setEntry(new java.io.File("0"));
    show.add(d);
    validator.validate(config, volume);
    Assert.assertTrue("Season directory number too small must be only violation.",
        validator.containsOnly(TvSeriesValidator.VIOLATION_SEASON_DIRECTORY_NUMBER_TOO_SMALL));
  }

  @Test
  public void testValidateFileNameSeasonDiffers()
  {
    initialize();
    final File f = new File();
    f.setName("Show S02E01.mkv");
    f.setEntry(new java.io.File("Show S02E01.mkv"));
    season.add(f);
    validator.validate(config, volume);
    Assert.assertTrue("File name's season differing from season directory must be only violation.",
        validator.containsOnly(TvSeriesValidator.VIOLATION_EPISODE_SEASON_AND_SEASON_DIRECTORY_DIFFER));
  }
}
