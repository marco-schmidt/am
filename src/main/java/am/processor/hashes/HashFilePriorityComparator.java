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
package am.processor.hashes;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import am.filesystem.model.File;

/**
 * Compare {@link am.filesystem.model.File} objects according to existence and age of hash values. Files with missing
 * hash values are smallest, then follow files by age of hash creation in descending order.
 *
 * A list of files sorted this way determines an order for hash computation.
 *
 * @author Marco Schmidt
 */
public class HashFilePriorityComparator implements Comparator<File>, Serializable
{
  private static final long serialVersionUID = 7271074576825351482L;

  @Override
  public int compare(File f1, File f2)
  {
    int rel = compareValues(f1.getHashValue(), f2.getHashValue());
    if (rel == 0)
    {
      rel = compareCreationDate(f1.getHashCreated(), f2.getHashCreated());
    }
    return rel;
  }

  private int compareCreationDate(Date hashCreated1, Date hashCreated2)
  {
    if (hashCreated1 == null)
    {
      if (hashCreated2 == null)
      {
        return 0;
      }
      else
      {
        return -1;
      }
    }
    else
    {
      if (hashCreated2 == null)
      {
        return 1;
      }
      else
      {
        return hashCreated1.compareTo(hashCreated2);
      }
    }
  }

  /**
   * Compare hash values, they are considered equal (0) if both values are null or both are non-null. Otherwise the null
   * value is considered smaller than the non-null value.
   *
   * @param hashValue1
   *          hash value of first file, possibly null
   * @param hashValue2
   *          hash value of second file, possibly null
   * @return relationship as 0, -1 or 1 as described above
   */
  private int compareValues(String hashValue1, String hashValue2)
  {
    if (hashValue1 == null)
    {
      if (hashValue2 == null)
      {
        return 0;
      }
      else
      {
        return -1;
      }
    }
    else
    {
      if (hashValue2 == null)
      {
        return 1;
      }
      else
      {
        return 0;
      }
    }
  }
}
