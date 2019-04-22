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

import java.util.Date;

/**
 * Data model class for a file as part of a {@link Directory} in a {@link Volume}.
 */
public class File
{
  private String name;
  private Date lastModified;
  private Long byteSize;
  private String fileType;
  private java.io.File entry;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public Date getLastModified()
  {
    return lastModified == null ? null : new Date(lastModified.getTime());
  }

  public void setLastModified(Date lastModified)
  {
    this.lastModified = lastModified == null ? null : new Date(lastModified.getTime());
  }

  public Long getByteSize()
  {
    return byteSize;
  }

  public void setByteSize(Long byteSize)
  {
    this.byteSize = byteSize;
  }

  public String getFileType()
  {
    return fileType;
  }

  public void setFileType(String fileType)
  {
    this.fileType = fileType;
  }

  public java.io.File getEntry()
  {
    return entry;
  }

  public void setEntry(java.io.File entry)
  {
    this.entry = entry;
  }
}
