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
package am.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.app.AppConfigLoader;
import am.filesystem.FileSystemHelper;
import am.filesystem.model.Directory;
import am.filesystem.model.Volume;

/**
 * Read and write {@link Volume} information from and to a tab-separated values (tsv) file.
 *
 * @author Marco Schmidt
 */
public class TsvSerialization
{
  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigLoader.class);
  private static final String DIR_SEPARATOR = "/";
  private static final String END_OF_LINE = "\n";
  private static final String TAB = "\t";
  private static final String TSV_FILE_EXTENSION = ".tsv";

  /**
   * File name filter accepting files with the extension ".tsv".
   */
  private static class TsvFilenameFilter implements FilenameFilter
  {
    @Override
    public boolean accept(File dir, String name)
    {
      return name != null && name.toLowerCase(Locale.ENGLISH).endsWith(TSV_FILE_EXTENSION);
    }
  }

  private boolean checkTsvDirectory(final AppConfig config, final File tsvDir)
  {
    if (tsvDir == null)
    {
      LOGGER.info(config.msg("tsv.info.directory_undefined"));
      return false;
    }
    if (!tsvDir.isDirectory())
    {
      LOGGER.error(config.msg("tsv.error.directory_does_not_exist", tsvDir.getAbsolutePath()));
      return false;
    }
    return true;
  }

  private void parseLine(final List<Volume> list, final Map<String, Volume> map, final String line)
  {
    String[] items = line.split(TAB);
    if (items.length < 13)
    {
      final String[] temp = new String[13];
      System.arraycopy(items, 0, temp, 0, items.length);
      int i = items.length;
      while (i < temp.length)
      {
        temp[i++] = "";
      }
      items = temp;
    }
    final String volumePath = items[0];
    Volume volume = map.get(volumePath);
    if (volume == null)
    {
      volume = new Volume();
      volume.setPath(volumePath);
      map.put(volumePath, volume);
      list.add(volume);
    }
    final String dirPath = items[1];
    final Directory dir = findOrCreateDirectory(volume, dirPath);
    final am.filesystem.model.File file = new am.filesystem.model.File();
    file.setName(items[2]);
    file.setByteSize(Long.valueOf(items[3]));
    file.setLastModified(new Date(Long.parseLong(items[4])));
    file.setHashValue("".equals(items[5]) ? null : items[5]);
    file.setHashCreated(getAsDate(items[6]));
    file.setFileType(items[7]);
    file.setFileGroup(items[8]);
    file.setMimeType(items[9]);
    file.setImageWidth(getAsLong(items[10]));
    file.setImageHeight(getAsLong(items[11]));
    file.setDurationNanos(getAsLong(items[12]));
    dir.add(file);
  }

  private Long getAsLong(String string)
  {
    if (string == null)
    {
      return null;
    }
    else
    {
      try
      {
        return Long.parseLong(string);
      }
      catch (final NumberFormatException nfe)
      {
        return null;
      }
    }
  }

  private Date getAsDate(String string)
  {
    if (string == null || string.isEmpty())
    {
      return null;
    }
    else
    {
      final Long value = getAsLong(string);
      return value == null ? null : new Date(value.longValue());
    }
  }

  private Directory findOrCreateDirectory(Volume volume, String dirPath)
  {
    // remove leading separator
    if (dirPath.startsWith(DIR_SEPARATOR))
    {
      dirPath = dirPath.substring(DIR_SEPARATOR.length());
    }

    // find or create root directory
    Directory root = volume.getRoot();
    if (root == null)
    {
      root = new Directory();
      root.setName("");
      volume.setRoot(root);
    }

    // if the path is empty just return root
    if (dirPath.isEmpty())
    {
      return root;
    }

    // get path parts
    final String[] items = dirPath.split(DIR_SEPARATOR);

    // traverse or create tree using parts
    Directory dir = root;
    int index = 0;
    while (index < items.length)
    {
      dir = dir.findOrCreateSubdirectory(items[index++]);
    }

    return dir;
  }

  public List<Volume> load(final AppConfig config)
  {
    final List<Volume> result = new ArrayList<>();
    final Map<String, Volume> map = new HashMap<>();
    final File tsvDir = config.getTsvDirectory();
    if (!checkTsvDirectory(config, tsvDir))
    {
      return result;
    }
    final File[] files = tsvDir.listFiles(new TsvFilenameFilter());
    final File newest = FileSystemHelper.findNewest(files);
    if (newest == null)
    {
      return result;
    }
    LOGGER.info(config.msg("init.info.load_tsv", newest.getAbsolutePath()));
    BufferedReader in = null;
    try
    {
      in = new BufferedReader(new InputStreamReader(new FileInputStream(newest), StandardCharsets.UTF_8));
      String line;
      while ((line = in.readLine()) != null)
      {
        parseLine(result, map, line);
      }
    }
    catch (final IOException e)
    {
      LOGGER.error(config.msg("init.error.problem_loading_tsv", newest.getAbsolutePath()), e);
    }
    finally
    {
      if (in != null)
      {
        try
        {
          in.close();
        }
        catch (final IOException e)
        {
          LOGGER.error(config.msg("init.error.failed_to_close_tsv", newest.getAbsolutePath()), e);
        }
      }
    }
    return result;
  }

  public void save(final AppConfig config, final List<Volume> volumes)
  {
    final File tsvDir = config.getTsvDirectory();
    if (!checkTsvDirectory(config, tsvDir))
    {
      return;
    }
    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    final String name = formatter.format(new Date()) + TSV_FILE_EXTENSION;
    final File file = new File(tsvDir, name);
    final String fullName = file.getAbsolutePath();
    Writer out = null;
    try
    {
      LOGGER.info(config.msg("init.info.start_save_tsv", fullName));
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
      for (final Volume volume : volumes)
      {
        save(out, volume.getPath(), "", volume.getRoot());
      }
      LOGGER.info(config.msg("init.info.finish_save_tsv", fullName));
    }
    catch (final FileNotFoundException e)
    {
      LOGGER.error(config.msg("init.error.tsv_file_creation", fullName), e);
    }
    catch (final IOException e)
    {
      LOGGER.error(config.msg("init.error.tsv_file_write", fullName), e);
    }
    finally
    {
      if (out != null)
      {
        try
        {
          out.close();
        }
        catch (final IOException e)
        {
        }
      }
    }
  }

  private void save(Writer out, String volPath, String relPath, Directory dir) throws IOException
  {
    final String path = relPath + dir.getName() + DIR_SEPARATOR;
    for (final Directory sub : dir.getSubdirectories())
    {
      save(out, volPath, path, sub);
    }
    for (final am.filesystem.model.File file : dir.getFiles())
    {
      save(out, volPath, path, file);
    }
  }

  private void append(StringBuffer sb, String value)
  {
    sb.append(TAB);
    sb.append(value == null ? "" : value);
  }

  private void append(StringBuffer sb, Long value)
  {
    append(sb, value == null ? (String) null : value.toString());
  }

  private void save(Writer out, String volPath, String path, am.filesystem.model.File file) throws IOException
  {
    final StringBuffer sb = new StringBuffer();
    sb.append(volPath);
    sb.append(TAB);
    sb.append(path);
    sb.append(TAB);
    sb.append(file.getName());
    sb.append(TAB);
    sb.append(file.getByteSize());
    sb.append(TAB);
    sb.append(file.getLastModified().getTime());
    append(sb, file.getHashValue());
    final Date hashCreated = file.getHashCreated();
    append(sb, hashCreated == null ? "" : Long.toString(hashCreated.getTime()));
    append(sb, file.getFileType());
    append(sb, file.getFileGroup());
    append(sb, file.getMimeType());
    append(sb, file.getImageWidth());
    append(sb, file.getImageHeight());
    append(sb, file.getDurationNanos());
    out.write(sb.toString());
    out.write(END_OF_LINE);
  }
}
