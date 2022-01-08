/*
 * Copyright 2019, 2020, 2021, 2022 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link FileSystemHelper} class.
 */
public class FileSystemHelperTest
{
  private static final Set<String> EMPTY = new HashSet<String>();

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

  @Test
  public void testNormalizeEmpty()
  {
    Assert.assertEquals("Empty path remains empty.", FileSystemHelper.normalizePath(""), "");
  }

  @Test
  public void testNormalizeUnix()
  {
    final String path = "/home/bob/sub";
    Assert.assertEquals("Unix path always remains identical.", FileSystemHelper.normalizePath(path), path);
  }

  @Test
  public void testSplitFileNamesNullNull()
  {
    Assert.assertEquals("Null value and separator lead to empty set.", EMPTY,
        FileSystemHelper.splitFileNames(null, null));
  }

  @Test
  public void testSplitFileNamesNullNonNull()
  {
    Assert.assertEquals("Null value and non-null separator lead to empty set.", EMPTY,
        FileSystemHelper.splitFileNames(null, "/"));
  }

  @Test
  public void testSplitFileNamesNonNullNull()
  {
    final String value = "value";
    final Set<String> result = FileSystemHelper.splitFileNames(value, null);
    Assert.assertNotNull("Non-null value and null separator lead to non-null set.", result);
    if (result != null)
    {
      final int size = result.size();
      Assert.assertEquals("Non-null value and null separator lead to set of size one.", 1, size);
      if (size == 1)
      {
        final String elem = result.iterator().next();
        Assert.assertEquals("Non-null value and null separator lead to set with that value.", value, elem);
      }
    }
  }

  @Test
  public void testSplitFileNamesNoSeparator()
  {
    final String value = "value";
    final String sep = "x";
    final Set<String> result = FileSystemHelper.splitFileNames(value, sep);
    Assert.assertNotNull("Non-null value and null separator lead to non-null set.", result);
    if (result != null)
    {
      final int size = result.size();
      Assert.assertEquals("Non-null value and null separator lead to set of size one.", 1, size);
      if (size == 1)
      {
        final String elem = result.iterator().next();
        Assert.assertEquals("Non-null value and null separator lead to set with that value.", value, elem);
      }
    }
  }

  @Test(expected = Test.None.class)
  public void testCloseNull()
  {
    FileSystemHelper.close(null);
    Assert.assertTrue("No exception thrown.", true);
  }

  @Test(expected = Test.None.class)
  public void testCloseNonNull()
  {
    final ByteArrayInputStream in = new ByteArrayInputStream(new byte[]
    {});
    FileSystemHelper.close(in);
    Assert.assertTrue("No exception thrown.", true);
  }
}
