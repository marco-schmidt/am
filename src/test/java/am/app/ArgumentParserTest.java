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
package am.app;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.validators.MovieValidatorTest;
import ch.qos.logback.classic.Level;

/**
 * Test {@link ArgumentParser} class.
 */
public class ArgumentParserTest
{
  @Test
  public void testParseEmpty()
  {
    final AppConfig config = new AppConfig();
    final ArgumentParser parser = new ArgumentParser();
    parser.parse(config, new String[]
    {});
  }

  @Test
  public void testParseLogLevelWarn()
  {
    final AppConfig config = new AppConfig();
    final ArgumentParser parser = new ArgumentParser();
    parser.parse(config, new String[]
    {
        "-l", "warn"
    });
    final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
        .getLogger(Logger.ROOT_LOGGER_NAME);
    final Level level = rootLogger.getLevel();
    Assert.assertEquals("Expect root logger level to be warn.", Level.WARN, level);
  }

  @Test
  public void testParseNumThreads()
  {
    final AppConfig config = new AppConfig();
    final ArgumentParser parser = new ArgumentParser();
    parser.parse(config, new String[]
    {
        "-j", "17"
    });
    Assert.assertEquals("Expect number of threads to be 17.", Integer.valueOf(17), config.getNumberOfThreads());
  }

  static class TestParam extends AbstractParameter
  {
    TestParam()
    {
      super("s2", "long", "s", ParameterType.Boolean);
    }

    @Override
    public void process(AppConfig config, String nextArg)
    {
    }
  }

  @Test
  public void testFormat()
  {
    final AppConfig config = new AppConfig();
    config.setBundle(new MovieValidatorTest.TestBundle());
    final TestParam testParam = new TestParam();
    Assert.assertEquals("Expected formatted parameter.", "-s  --long BOOLEAN 1",
        ArgumentParser.format(config, testParam));
  }
}
