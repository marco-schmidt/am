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
package am.filesystem;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link FileSystemHelper} class.
 */
public class FileSystemHelperTest
{
  @Test
  public void testFindNewestNull()
  {
    Assert.assertNull("Null array yields null result.", FileSystemHelper.findNewest((File[]) null));
  }

  @Test
  public void testFindEmpty()
  {
    Assert.assertNull("Empty array yields null result.", FileSystemHelper.findNewest(new File[]
    {}));
  }

  @Test
  public void testFindNullElementsOnly()
  {
    Assert.assertNull("Empty array yields null result.", FileSystemHelper.findNewest(new File[]
    {
        null, null
    }));
  }

  @Test
  public void testFindSingle()
  {
    final File file = new File(".");
    Assert.assertEquals("Single-element array finds that element.", file, FileSystemHelper.findNewest(new File[]
    {
        file
    }));
  }

  @Test
  public void testFindSingleNull()
  {
    final File file = new File(".");
    Assert.assertEquals("Array with non-null element and null finds that element.", file,
        FileSystemHelper.findNewest(new File[]
        {
            file, null
        }));
  }

  @Test
  public void testFindNullSingle()
  {
    final File file = new File(".");
    Assert.assertEquals("Array with null and non-null element finds that element.", file,
        FileSystemHelper.findNewest(new File[]
        {
            null, file
        }));
  }

  @Test
  public void testFindTwoNonNull()
  {
    final File fileSmall = new File(".");
    if (!fileSmall.setLastModified(1000L))
    {
      Assert.fail("Setup failed.");
    }
    final File fileLarge = new File("..");
    if (!fileLarge.setLastModified(20000000L))
    {
      Assert.fail("Setup failed.");
    }
    Assert.assertEquals("Array with two non-null elements finds the correct element.", fileLarge,
        FileSystemHelper.findNewest(new File[]
        {
            fileSmall, fileLarge
        }));
  }
}
