/*
 * Copyright 2019, 2020, 2021 the original author or authors.
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
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.FileState;
import am.filesystem.model.Volume;

/**
 * Test {@link VolumeProcessor}.
 *
 * @author Marco Schmidt
 */
public class VolumeProcessorTest
{
  @Test(expected = NullPointerException.class)
  public void testMergeFileNullNull()
  {
    new VolumeProcessor().mergeFile(null, null);
  }

  @Test
  public void testMergeFileOneNull()
  {
    final File scanned = new File();
    final File loaded = new File();
    final VolumeProcessor processor = new VolumeProcessor();
    Assert.assertEquals("Null scanned and non-null loaded returns loaded.", loaded, processor.mergeFile(null, loaded));
    Assert.assertEquals("Non-null scanned and non-null loaded returns loaded.", scanned,
        processor.mergeFile(scanned, null));
  }

  @Test
  public void testMergeFileBothNonNull()
  {
    final Long size = Long.valueOf(12345);
    final Date lastMod = new Date(10000000);
    final File scanned = new File();
    scanned.setLastModified(lastMod);
    scanned.setByteSize(size);
    final File loaded = new File();
    loaded.setLastModified(lastMod);
    loaded.setByteSize(size);
    final VolumeProcessor processor = new VolumeProcessor();
    final File result = processor.mergeFile(scanned, loaded);
    Assert.assertNotNull("Merge of two non-null input values yields non-null result.", result);
    final FileState state = result.getState();
    Assert.assertEquals("Identical size and last modification leads to file state identical.", FileState.Identical,
        state);
  }

  @Test
  public void testMergeVolume()
  {
    final Volume scanned = new Volume();
    scanned.setRoot(new Directory());
    final Volume loaded = new Volume();
    loaded.setRoot(new Directory());
    final VolumeProcessor processor = new VolumeProcessor();
    final Volume result = processor.mergeVolume(scanned, loaded);
    Assert.assertNotNull("Merge of two non-null input values yields non-null result.", result);
  }

  @Test
  public void testMergeDirectory()
  {
    final VolumeProcessor processor = new VolumeProcessor();
    final Directory scanned = new Directory();
    final Directory sub = new Directory();
    final File file = new File();
    file.setName("afile");
    file.setByteSize(Long.valueOf(1));
    file.setLastModified(new Date(1000000));
    sub.setName("adirectory");
    scanned.add(sub);
    scanned.add(file);
    final Directory loaded = new Directory();
    loaded.add(sub);
    loaded.add(file);
    Assert.assertNull("Null if both null.", processor.mergeDirectory(null, null));
    Assert.assertEquals("Scanned null, loaded non-null yields loaded.", loaded, processor.mergeDirectory(null, loaded));
    Assert.assertEquals("Loaded null, scanned non-null yields scanned.", scanned,
        processor.mergeDirectory(scanned, null));
    final Directory result = processor.mergeDirectory(scanned, loaded);
    Assert.assertNotEquals("Merging two non-null volumes does not yield loaded.", loaded, result);
    Assert.assertNotEquals("Merging two non-null volumes does not yield scanned.", scanned, result);
  }

  @Test
  public void testProcessVolumes()
  {
    final Directory root = new Directory();
    root.setName("");
    final Volume scanned = new Volume();
    scanned.setRoot(root);
    scanned.setPath("/scanned");
    final Volume loaded = new Volume();
    loaded.setPath("/loaded");
    final Volume both = new Volume();
    both.setRoot(root);
    both.setPath("/both");
    final List<Volume> scannedList = new ArrayList<Volume>();
    scannedList.add(scanned);
    scannedList.add(both);
    final List<Volume> loadedList = new ArrayList<Volume>();
    loadedList.add(loaded);
    loadedList.add(both);
    final VolumeProcessor processor = new VolumeProcessor();
    final List<Volume> result = processor.processVolumes(scannedList, loadedList);
    Assert.assertNotNull("Merge of two non-null input values yields non-null result.", result);
    Assert.assertEquals("Merged volume list has three elements.", 3, result.size());
  }
}
