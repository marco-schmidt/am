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
package am.filesystem.model;

import java.io.File;

/**
 * Data model class for a volume, a directory tree mounted below a path.
 */
public class Volume
{
  private String path;
  private Directory root;
  private File entry;

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
}
