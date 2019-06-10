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
package am.processor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardTag;
import com.thebuzzmedia.exiftool.core.UnspecifiedTag;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.Volume;

/**
 * Retrieve metadata included in file by running exiftool on that file and parsing exiftool's standard output. Requires
 * exiftool being installed on the system and the path to the exiftool executable defined in the am configuration file.
 *
 * @author Marco Schmidt
 */
public class MetadataExtraction
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MetadataExtraction.class);
  /**
   * Non-null value for examination result 'unknown file type', to be stored so that a file will not be examined again.
   */
  public static final String UNKNOWN = "?";
  private static final UnspecifiedTag DURATION = new UnspecifiedTag("Duration");
  private static final List<Tag> EXIFTOOL_TAGS = Arrays.asList(new Tag[]
  {
      StandardTag.MIME_TYPE, StandardTag.IMAGE_WIDTH, StandardTag.IMAGE_HEIGHT, DURATION,
  });
  private long numExamined;

  public long getNumExamined()
  {
    return numExamined;
  }

  public void setNumExamined(long numExamined)
  {
    this.numExamined = numExamined;
  }

  public void update(final AppConfig config, final Volume volume)
  {
    update(config, volume.getRoot());
  }

  private void update(AppConfig config, Directory dir)
  {
    for (final File f : dir.getFiles())
    {
      update(config, f);
    }
    for (final Directory d : dir.getSubdirectories())
    {
      update(config, d);
    }
  }

  private void update(AppConfig config, File file)
  {
    final String fileType = file.getFileType();
    final java.io.File entry = file.getEntry();
    if (fileType == null && entry.isFile())
    {
      if (LOGGER.isTraceEnabled())
      {
        LOGGER.trace(config.msg("exiftool.trace.examining_file", entry.getAbsolutePath()));
      }
      ExifTool exifTool = config.getExifTool();
      try
      {
        setNumExamined(getNumExamined() + 1);
        final Long exifToolMaxUsage = config.getExifToolMaxUsage();
        if (exifToolMaxUsage != null && exifToolMaxUsage.longValue() > 0
            && getNumExamined() % exifToolMaxUsage.longValue() == 0)
        {
          LOGGER.info(config.msg("exiftool.info.reopen"));
          try
          {
            exifTool.close();
          }
          catch (final Exception e)
          {
            LOGGER.error(config.msg("exiftool.error.failed_to_close"), e);
          }
          exifTool = new ExifToolBuilder().withPath(config.getExifToolPath()).enableStayOpen().build();
          config.setExifTool(exifTool);
        }
        final Map<Tag, String> meta = exifTool.getImageMeta(entry, EXIFTOOL_TAGS);
        extractType(meta, file);
        extractImageResolution(meta, file);
        extractDuration(meta, file);
        LOGGER.info(
            config.msg("exiftool.info.examined_file", getNumExamined(), entry.getAbsolutePath(), file.getFileType()));
      }
      catch (final IOException e)
      {
        LOGGER.error(config.msg("exiftool.error.failed_to_retrieve", entry.getAbsolutePath()), e);
      }
    }
  }

  private Long getAsLong(Map<Tag, String> meta, Tag tag)
  {
    final String value = meta.get(tag);
    if (value == null)
    {
      return null;
    }
    try
    {
      return Long.valueOf(value);
    }
    catch (final NumberFormatException e)
    {
      return null;
    }
  }

  private void extractDuration(Map<Tag, String> meta, File file)
  {
    final String s = meta.get(DURATION);
    Long result = null;
    if (s != null)
    {
      final BigDecimal d = new BigDecimal(s).scaleByPowerOfTen(9);
      result = Long.valueOf(d.longValue());
    }
    file.setDurationNanos(result);
  }

  private void extractImageResolution(Map<Tag, String> meta, File file)
  {
    file.setImageHeight(getAsLong(meta, StandardTag.IMAGE_HEIGHT));
    file.setImageWidth(getAsLong(meta, StandardTag.IMAGE_WIDTH));
  }

  private void extractType(final Map<Tag, String> meta, final File file)
  {
    String mimeType = meta.get(StandardTag.MIME_TYPE);
    if (mimeType == null)
    {
      mimeType = UNKNOWN;
    }
    else
    {
      final String[] items = mimeType.split("/");
      if (items != null && items.length == 2 && items[0] != null && !items[0].isEmpty() && items[1] != null
          && !items[1].isEmpty())
      {
        final String fileGroup = items[0];
        file.setFileGroup(fileGroup);
        final String fileType = items[1];
        file.setFileType(fileType);
      }
    }
    file.setMimeType(mimeType);
  }

  public void update(final AppConfig config, final List<Volume> volumes)
  {
    final ExifTool exifTool = config.getExifTool();
    if (exifTool == null)
    {
      LOGGER.debug(config.msg("exiftool.debug.undefined_path"));
    }
    else
    {
      for (final Volume v : volumes)
      {
        update(config, v);
      }
      LOGGER.info(config.msg("exiftool.info.number_examined_files", getNumExamined()));
      try
      {
        exifTool.close();
      }
      catch (final Exception e)
      {
        LOGGER.error("exiftool.error.failed_to_close", e);
      }
    }
  }
}
