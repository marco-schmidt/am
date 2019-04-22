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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.Volume;

/**
 * Process the content of a volume.
 *
 * @author Marco Schmidt
 */
public class VolumeProcessor
{
  private Directory mergeDirectory(Directory scanned, Directory loaded)
  {
    if (scanned == null)
    {
      return loaded;
    }
    if (loaded == null)
    {
      return scanned;
    }
    final Directory merged = new Directory();
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
      return loaded;
    }
    if (loaded == null)
    {
      return scanned;
    }
    final File result = new File();
    result.setName(scanned.getName());
    result.setLastModified(scanned.getLastModified());
    final Long size = scanned.getByteSize();
    result.setByteSize(size);
    final String fileType = loaded.getFileType();
    result.setFileType(fileType);
    // check size, last mod, hash, last hash check
    // if (!size.equals(loaded.getByteSize()))
    // {
    //
    // }
    return result;
  }

  public Volume mergeVolume(Volume scannedVolume, Volume loadedVolume)
  {
    final Volume result = new Volume();
    result.setPath(scannedVolume.getPath());
    final Directory root = mergeDirectory(scannedVolume.getRoot(), loadedVolume.getRoot());
    result.setRoot(root);
    return result;
  }

  private static Map<String, Volume> toMap(List<Volume> volumes)
  {
    final Map<String, Volume> result = new HashMap<>();
    for (final Volume vol : volumes)
    {
      result.put(vol.getPath(), vol);
    }
    return result;
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
      }
      else
      {
        if (scanned == null)
        {
          merged = loaded;
        }
        else
        {
          merged = mergeVolume(scanned, loaded);
        }
      }
      result.add(merged);
    }

    return result;
  }
}
