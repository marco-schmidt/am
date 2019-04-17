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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.app.AppConfigLoader;
import am.filesystem.FileSystemHelper;
import am.filesystem.model.Directory;
import am.filesystem.model.Volume;

/**
 * Read and write {@link Volume} information from and to a tab-separated values (tsv) file.
 */
public class TsvSerialization
{
  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigLoader.class);
  private static final String DIR_SEPARATOR = "/";
  private static final String END_OF_LINE = "\n";
  private static final String TAB = "\t";

  public List<Volume> load(final AppConfig config)
  {
    final List<Volume> result = new ArrayList<>();
    final File dir = config.getTsvDirectory();
    final File[] files = dir.listFiles();
    final File newest = FileSystemHelper.findNewest(files);
    if (newest == null)
    {
      return result;
    }
    BufferedReader in = null;
    try
    {
      in = new BufferedReader(new InputStreamReader(new FileInputStream(newest), StandardCharsets.UTF_8));
      String line;
      while ((line = in.readLine()) != null)
      {
        if (line.equals("todo: parse this"))
        {
          break;
        }
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
    final File dir = config.getTsvDirectory();
    if (dir == null)
    {
      return;
    }
    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    final String name = formatter.format(new Date()) + ".log";
    final File file = new File(dir, name);
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
    out.write(sb.toString());
    out.write(END_OF_LINE);
  }
}
