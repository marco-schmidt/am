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
package am.app;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

/**
 * Parse arguments from command line and configuration files.
 */
public class ArgumentParser
{
  /**
   * Program parameters.
   */
  private static final AbstractParameter[] PARAMETERS =
  {
      new AbstractParameter("args.print_help", "help", "H", null)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          config.setMode(ProcessMode.ShowHelp);
        };
      },

      new AbstractParameter("args.print_version", "version", "V", null)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          config.setMode(ProcessMode.ShowVersion);
        };
      },

      new AbstractParameter("args.stop_interpreting_switches", "", null, null)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          // nothing to do, is interpreted at a higher level
        };
      },

      new AbstractParameter("args.num_threads", "threads", "j", ParameterType.Integer)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          try
          {
            final Integer numThreads = Integer.valueOf(nextArg);
            if (numThreads > 0)
            {
              config.setNumberOfThreads(numThreads);
            }
            else
            {
              config.msg("args.error.invalid_number_of_threads", nextArg);
            }
          }
          catch (final NumberFormatException nfe)
          {
            config.msg("args.error.invalid_number_of_threads_exception", nextArg, nfe.getMessage());
          }
        };
      },

      new AbstractParameter("args.log_level", "log-level", "l", ParameterType.String)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          final Level level = Level.toLevel(nextArg, null);
          if (level == null)
          {
            config.msg("args.error.invalid_log_level", nextArg);
          }
          else
          {
            final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(Logger.ROOT_LOGGER_NAME);
            if (rootLogger != null)
            {
              rootLogger.setLevel(level);
            }
          }
        };
      },

      new AbstractParameter("args.print_environment", "print-env", null, null)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          config.setShowEnvironment(true);
        };
      }, new AbstractParameter("args.quiet", "quiet", "q", null)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          config.setQuiet(true);
        };
      }, new AbstractParameter("args.config_file", "config", null, ParameterType.File)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          config.setConfigFileName(nextArg);
        };
      }, new AbstractParameter("args.add_volume", "add-volume", null, ParameterType.Directory)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          config.setMode(ProcessMode.AddVolume);
          config.setAddVolumePath(nextArg);
        };
      }, new AbstractParameter("args.delete_volume", "delete-volume", null, ParameterType.Directory)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          config.setMode(ProcessMode.DeleteVolume);
          config.setDeleteVolumePath(nextArg);
        };
      }, new AbstractParameter("args.set_volume_validator", "set-validator", null, ParameterType.String)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          config.setAddVolumeValidator(nextArg);
        };
      }, new AbstractParameter("args.check", "check", "c", ParameterType.String)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          config.setMode(ProcessMode.Check);
          config.addFileSystemItem(nextArg);
        };
      }, new AbstractParameter("args.wikidata", "wikidata", null, ParameterType.Boolean)
      {
        @Override
        public void process(final AppConfig config, final String nextArg)
        {
          config.getWikidataConfiguration().setEnabled(Boolean.parseBoolean(nextArg));
        };
      }
  };
  private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentParser.class);
  private static final String DASH = "-";
  private static final String TWO_DASHES = DASH + DASH;
  private static Map<String, AbstractParameter> mapLong = new HashMap<String, AbstractParameter>();
  private static Map<String, AbstractParameter> mapShort = new HashMap<String, AbstractParameter>();

  static
  {
    for (final AbstractParameter p : PARAMETERS)
    {
      add(mapLong, p.getLongName(), p);
      add(mapShort, p.getShortName(), p);
    }
  }

  public static List<AbstractParameter> getParameters()
  {
    return Arrays.asList(PARAMETERS);
  }

  private static void add(final Map<String, AbstractParameter> map, final String name, final AbstractParameter param)
  {
    if (name != null)
    {
      if (map.containsKey(name))
      {
        LOGGER.error(String.format(Locale.ROOT, "Switch defined twice: %s.", name));
      }
      else
      {
        map.put(name, param);
      }
    }
  }

  public boolean parse(final AppConfig config, final String... args)
  {
    boolean success = true;
    boolean findSwitches = true;
    final Iterator<String> iterator = Arrays.asList(args).iterator();
    while (iterator.hasNext())
    {
      final String arg = iterator.next();
      if (findSwitches)
      {
        if (TWO_DASHES.equals(arg))
        {
          findSwitches = false;
        }
        else
        {
          if (!processParameter(config, arg, iterator))
          {
            success = false;
            break;
          }
        }
      }
    }
    return success;
  }

  private boolean processParameter(final AppConfig config, final String arg, final Iterator<String> iterator)
  {
    boolean success = true;
    String name = null;
    Map<String, AbstractParameter> map = null;
    if (arg.startsWith(TWO_DASHES))
    {
      name = arg.substring(TWO_DASHES.length());
      map = mapLong;
    }
    else
    {
      if (arg.startsWith(DASH))
      {
        name = arg.substring(DASH.length());
        map = mapShort;
      }
    }
    if (name != null)
    {
      final int equalsIndex = name.indexOf('=');
      String nextArg = null;
      if (equalsIndex >= 0)
      {
        nextArg = name.substring(equalsIndex + 1);
        name = name.substring(0, equalsIndex);
      }
      final AbstractParameter param = map.get(name);
      if (param == null)
      {
        LOGGER.error(config.msg("args.unknown_switch", arg));
        success = false;
      }
      else
      {
        final ParameterType argumentType = param.getArgumentType();
        if (argumentType == null)
        {
          if (nextArg != null)
          {
            LOGGER.error(config.msg("args.error.superfluous_switch_argument", name));
            success = false;
          }
        }
        else
        {
          if (iterator.hasNext())
          {
            nextArg = iterator.next();
          }
          else
          {
            LOGGER.error(config.msg("args.error.missing_argument", name));
            success = false;
          }
        }
        processParameter(config, param, nextArg);
      }
    }
    return success;
  }

  private void processParameter(final AppConfig config, final AbstractParameter param, final String nextArg)
  {
    param.process(config, nextArg);
  }

  private static String formatSwitch(final String prefix, final String name)
  {
    String result;
    if (name == null)
    {
      result = "";
    }
    else
    {
      result = prefix + name;
    }
    return result;
  }

  public static String format(final AppConfig config, final AbstractParameter param)
  {
    final String longName = formatSwitch(TWO_DASHES, param.getLongName());
    final String shortName = formatSwitch(DASH, param.getShortName());
    String argument = " ";
    final ParameterType argumentType = param.getArgumentType();
    if (argumentType != null)
    {
      argument = " " + argumentType.name().toUpperCase(Locale.ENGLISH) + " ";
    }
    return String.format(Locale.ROOT, "%s  %s%s%s", shortName, longName, argument, config.msg(param.getMessage()));
  }
}
