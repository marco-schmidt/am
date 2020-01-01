/*
 * Copyright 2019, 2020 the original author or authors.
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

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class {@link Directory}.
 *
 * @author Marco Schmidt
 */
public class DirectoryTest
{
  @Test
  public void testFindOrCreateSubdirectory()
  {
    final Directory dir = new Directory();
    final Directory sub = new Directory();
    sub.setName("sub");
    dir.add(sub);
    Assert.assertEquals("Number of subdirectories is one.", 1, dir.getSubdirectories().size());
    final Directory result = dir.findOrCreateSubdirectory("sub");
    Assert.assertEquals("Number of subdirectories is still one.", 1, dir.getSubdirectories().size());
    Assert.assertEquals("Found subdirectory is existing one.", result, sub);
    final Directory newDir = dir.findOrCreateSubdirectory("new");
    Assert.assertEquals("Number of subdirectories is now two.", 2, dir.getSubdirectories().size());
    Assert.assertEquals("New dir has given name.", "new", newDir.getName());
  }
}
