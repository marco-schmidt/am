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
package am.filesystem.model;

import java.io.File;
import am.db.Model;

/**
 * Data model class for a volume, a directory tree mounted below a path.
 *
 * @author Marco Schmidt
 */
public class Volume extends Model
{
  private String path;
  private Directory root;
  private File entry;
  private String validator;
  private boolean main;
  private Long mainRef;

  public String getPath()
  {
    return path;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

  public Directory getRoot()
  {
    return root;
  }

  public void setRoot(Directory root)
  {
    this.root = root;
  }

  public File getEntry()
  {
    return entry;
  }

  public void setEntry(File entry)
  {
    this.entry = entry;
  }

  public String getValidator()
  {
    return validator;
  }

  public void setValidator(String validator)
  {
    this.validator = validator;
  }

  public boolean isMain()
  {
    return main;
  }

  public void setMain(boolean main)
  {
    this.main = main;
  }

  public Long getMainRef()
  {
    return mainRef;
  }

  public void setMainRef(Long mainRef)
  {
    this.mainRef = mainRef;
  }
}
