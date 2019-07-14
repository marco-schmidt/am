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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.FileSystemHelper;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.FileState;
import am.filesystem.model.Volume;

/**
 * Merge two descriptions of a directory tree (volume), one as was just determined from the file system and one stored
 * in a database from a previous run.
 *
 * @author Marco Schmidt
 */
public class VolumeProcessor
{
  private static final Logger LOGGER = LoggerFactory.getLogger(VolumeProcessor.class);
  private AppConfig config;

  private void assignFileState(Directory dir, FileState state)
  {
    for (final Directory sub : dir.getSubdirectories())
    {
      assignFileState(sub, state);
    }
    for (final File file : dir.getFiles())
    {
      file.setState(state);
    }
  }

  private Directory mergeDirectory(Directory scanned, Directory loaded)
  {
    if (scanned == null)
    {
      assignFileState(loaded, FileState.Missing);
      return loaded;
    }
    if (loaded == null)
    {
      assignFileState(scanned, FileState.New);
      return scanned;
    }
    final Directory merged = new Directory();
    merged.setId(loaded.getId());

    merged.setName(scanned.getName());

    final Set<String> names = new HashSet<>();
    names.addAll(scanned.getSubdirectoryNames());
    names.addAll(loaded.getSubdirectoryNames());
    for (final String subName : names)
    {
      merged.add(mergeDirectory(scanned.getSubdirectory(subName), loaded.getSubdirectory(subName)));
    }

    names.clear();
    names.addAll(scanned.getFileNames());
    names.addAll(loaded.getFileNames());
    for (final String fileName : names)
    {
      merged.add(mergeFile(scanned.getFile(fileName), loaded.getFile(fileName)));
    }

    return merged;
  }

  private File mergeFile(File scanned, File loaded)
  {
    if (scanned == null)
    {
      loaded.setState(FileState.Missing);
      return loaded;
    }
    if (loaded == null)
    {
      scanned.setState(FileState.New);
      return scanned;
    }
    final File result = new File();
    result.setId(loaded.getId());
    result.setName(scanned.getName());
    final Date scannedLastMod = scanned.getLastModified();
    final Date loadedLastMod = loaded.getLastModified();
    boolean modified = !scannedLastMod.equals(loadedLastMod);
    result.setLastModified(scannedLastMod);
    final Long scannedSize = scanned.getByteSize();
    final Long loadedSize = loaded.getByteSize();
    if (!modified)
    {
      modified = !scannedSize.equals(loadedSize);
    }
    result.setState(modified ? FileState.Modified : FileState.Identical);
    result.setByteSize(scannedSize);
    final String fileType = loaded.getFileType();
    result.setFileType(fileType);
    if (result.getState() == FileState.Identical)
    {
      // if loaded and scanned are identical copy metadata from loaded record
      result.setHashCreated(loaded.getHashCreated());
      result.setHashValue(loaded.getHashValue());
      result.setDurationNanos(loaded.getDurationNanos());
      result.setImageHeight(loaded.getImageHeight());
      result.setImageWidth(loaded.getImageWidth());
      result.setMimeType(loaded.getMimeType());
      result.setFileGroup(loaded.getFileGroup());
      result.setWikidataEntityId(loaded.getWikidataEntityId());
    }
    return result;
  }

  /**
   * Merge information from scanned and loaded volume.
   *
   * @param scannedVolume
   *          volume information just loaded from the file system
   * @param loadedVolume
   *          volume information from database
   * @return new volume with information from both merged
   */
  public Volume mergeVolume(Volume scannedVolume, Volume loadedVolume)
  {
    final Volume result = new Volume();
    result.setId(loadedVolume.getId());
    result.setPath(FileSystemHelper.normalizePath(scannedVolume.getPath()));
    result.setValidator(loadedVolume.getValidator());
    final Directory root = mergeDirectory(scannedVolume.getRoot(), loadedVolume.getRoot());
    result.setRoot(root);
    return result;
  }

  private static Map<String, Volume> toMap(List<Volume> volumes)
  {
    final Map<String, Volume> result = new HashMap<>();
    for (final Volume vol : volumes)
    {
      result.put(FileSystemHelper.normalizePath(vol.getPath()), vol);
    }
    return result;
  }

  private void assignFileSystemEntries(java.io.File parent, Directory dir)
  {
    final java.io.File dirEntry = new java.io.File(parent, dir.getName());
    dir.setEntry(dirEntry);
    for (final Directory sub : dir.getSubdirectories())
    {
      assignFileSystemEntries(dirEntry, sub);
    }
    for (final File file : dir.getFiles())
    {
      file.setEntry(new java.io.File(dirEntry, file.getName()));
    }
  }

  private void assignFileSystemEntries(Volume vol)
  {
    final String path = vol.getPath();
    final java.io.File entry = new java.io.File(path);
    vol.setEntry(entry);
    assignFileSystemEntries(entry, vol.getRoot());
  }

  public List<Volume> processVolumes(List<Volume> scannedVolumes, List<Volume> loadedVolumes)
  {
    final List<Volume> result = new ArrayList<>();

    // convert lists to maps for easy retrieval of volumes by path
    final Map<String, Volume> scannedMap = toMap(scannedVolumes);
    final Map<String, Volume> loadedMap = toMap(loadedVolumes);

    // create union of keys from both maps
    final Set<String> keys = new HashSet<>();
    keys.addAll(scannedMap.keySet());
    keys.addAll(loadedMap.keySet());

    // traverse all volumes
    for (final String key : keys)
    {
      final Volume scanned = scannedMap.get(key);
      final Volume loaded = loadedMap.get(key);
      Volume merged;
      if (loaded == null)
      {
        merged = scanned;
        assignFileState(merged.getRoot(), FileState.New);
      }
      else
      {
        if (scanned == null)
        {
          merged = loaded;
          Directory root = merged.getRoot();
          if (root == null)
          {
            root = new Directory();
            root.setName("");
            root.setVolumeRef(loaded.getId());
            root.setEntry(new java.io.File(merged.getPath()));
            merged.setRoot(root);
          }
          assignFileState(root, FileState.Missing);
        }
        else
        {
          merged = mergeVolume(scanned, loaded);
        }
      }
      result.add(merged);
      assignFileSystemEntries(merged);
    }

    print(result);

    return result;
  }

  private void print(List<Volume> volumes)
  {
    for (final Volume vol : volumes)
    {
      print(vol);
    }
  }

  private void print(Volume vol)
  {
    print(vol.getRoot());
  }

  private void print(Directory dir)
  {
    for (final Directory sd : dir.getSubdirectories())
    {
      print(sd);
    }
    for (final File file : dir.getFiles())
    {
      print(file);
    }
  }

  private void print(File file)
  {
    final FileState state = file.getState();
    if (state == null)
    {
      return;
    }
    switch (state)
    {
    case New:
    {
      LOGGER.info(config.msg("volumeprocessor.info.new_file", file.getEntry().getAbsolutePath()));
      break;
    }
    case Modified:
    {
      LOGGER.warn(config.msg("volumeprocessor.warn.modified_file", file.getEntry().getAbsolutePath()));
      break;
    }
    case Missing:
    {
      LOGGER.warn(config.msg("volumeprocessor.warn.missing_file", file.getEntry().getAbsolutePath()));
      break;
    }
    default:
    {
      if (LOGGER.isTraceEnabled())
      {
        LOGGER.trace(config.msg("volumeprocessor.trace.file_state", file.getEntry().getAbsolutePath(), state));
      }
      break;
    }
    }
  }

  public AppConfig getConfig()
  {
    return config;
  }

  public void setConfig(AppConfig config)
  {
    this.config = config;
  }
}
