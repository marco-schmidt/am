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

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.Volume;

/**
 * Base class for validators.
 *
 * @author Marco Schmidt
 */
public abstract class AbstractValidator
{
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractValidator.class);
  private AppConfig config;
  private final Set<String> ids = new HashSet<>();

  public void addViolation(File file, String id)
  {
    final java.io.File entry = file.getEntry();
    final String path = entry == null ? file.getName() : entry.getAbsolutePath();
    final String keyViolation = getMessagePrefix() + ".violation";
    final String keyMsg = getMessagePrefix() + "." + id;
    ids.add(id);
    LOGGER.error(config.msg(keyViolation, path) + config.msg(keyMsg));
  }

  public void addViolation(Directory dir, String id)
  {
    final String path = dir.getEntry().getAbsolutePath();
    final String keyViolation = getMessagePrefix() + ".violation";
    final String keyMsg = getMessagePrefix() + "." + id;
    ids.add(id);
    LOGGER.error(config.msg(keyViolation, path) + config.msg(keyMsg));
  }

  public abstract String getMessagePrefix();

  public abstract void validate(AppConfig config, Volume volume);

  public AppConfig getConfig()
  {
    return config;
  }

  public void setConfig(AppConfig config)
  {
    this.config = config;
  }

  public void resetIds()
  {
    ids.clear();
  }

  public boolean containsOnly(String id)
  {
    return ids.contains(id) && ids.size() == 1;
  }
}
