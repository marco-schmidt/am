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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import am.app.AppConfig;
import am.app.validators.MovieValidatorTest.TestBundle;
import am.filesystem.model.Directory;
import am.filesystem.model.VideoFileName;
import am.filesystem.model.Volume;
import am.validators.TvSeriesValidator;

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
    volume = new Volume();
    root = new Directory();
    volume.setRoot(root);
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
}
