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
import am.db.Model;

/**
 * Data model class for a file as part of a {@link Directory} in a {@link Volume}.
 */
public class File extends Model
{
  private Long volumeRef;
  private Long directoryRef;
  private String name;
  private Date lastModified;
  private Long byteSize;
  private String fileGroup;
  private String fileType;
  private String mimeType;
  private Long durationNanos;
  private Long imageWidth;
  private Long imageHeight;
  private java.io.File entry;
  private FileState state = FileState.Unknown;
  private String hashValue;
  private Date hashCreated;
  private VideoFileName videoFileName;
  private String wikidataEntityId;

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

  public FileState getState()
  {
    return state;
  }

  public void setState(FileState state)
  {
    this.state = state;
  }

  public String getFileGroup()
  {
    return fileGroup;
  }

  public void setFileGroup(String fileGroup)
  {
    this.fileGroup = fileGroup;
  }

  public String getMimeType()
  {
    return mimeType;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  public Long getDurationNanos()
  {
    return durationNanos;
  }

  public void setDurationNanos(Long durationNanos)
  {
    this.durationNanos = durationNanos;
  }

  public Long getImageWidth()
  {
    return imageWidth;
  }

  public void setImageWidth(Long imageWidth)
  {
    this.imageWidth = imageWidth;
  }

  public Long getImageHeight()
  {
    return imageHeight;
  }

  public void setImageHeight(Long imageHeight)
  {
    this.imageHeight = imageHeight;
  }

  public String getHashValue()
  {
    return hashValue;
  }

  public void setHashValue(String hashValue)
  {
    this.hashValue = hashValue;
  }

  public Date getHashCreated()
  {
    return hashCreated == null ? null : new Date(hashCreated.getTime());
  }

  public void setHashCreated(Date newValue)
  {
    this.hashCreated = newValue == null ? null : new Date(newValue.getTime());
  }

  public VideoFileName getVideoFileName()
  {
    return videoFileName;
  }

  public void setVideoFileName(VideoFileName videoFileName)
  {
    this.videoFileName = videoFileName;
  }

  public String getWikidataEntityId()
  {
    return wikidataEntityId;
  }

  public void setWikidataEntityId(String wikidataEntityId)
  {
    this.wikidataEntityId = wikidataEntityId;
  }

  public Long getVolumeRef()
  {
    return volumeRef;
  }

  public void setVolumeRef(Long volumeRef)
  {
    this.volumeRef = volumeRef;
  }

  public Long getDirectoryRef()
  {
    return directoryRef;
  }

  public void setDirectoryRef(Long directoryRef)
  {
    this.directoryRef = directoryRef;
  }
}
