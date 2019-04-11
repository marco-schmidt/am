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
package am.app;

/**
 * Command line parameter for the application.
 */
public abstract class AbstractParameter
{
  private final String message;
  private final String longName;
  private final String shortName;
  private final ParameterType argumentType;

  public AbstractParameter(final String message, final String longName, final String shortName,
      final ParameterType argumentType)
  {
    this.message = message;
    this.longName = longName;
    this.shortName = shortName;
    this.argumentType = argumentType;
  }

  public String getLongName()
  {
    return longName;
  }

  public String getShortName()
  {
    return shortName;
  }

  public boolean hasArgument()
  {
    return argumentType != null;
  }

  public String getMessage()
  {
    return message;
  }

  public abstract void process(AppConfig config, String nextArg);

  public ParameterType getArgumentType()
  {
    return argumentType;
  }
}
