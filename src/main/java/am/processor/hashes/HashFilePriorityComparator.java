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
package am.processor.hashes;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import am.filesystem.model.File;

/**
 * Compare {@link am.filesystem.model.File} objects according to existence and age of hash values.
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
    if (rel != 0)
    {
      return rel;
    }
    rel = compareCreationDate(f1.getHashCreated(), f2.getHashCreated());
    return rel;
  }

  private int compareCreationDate(Date hashCreated1, Date hashCreated2)
  {
    return Objects.compare(hashCreated1, hashCreated2, Date::compareTo);
  }

  private int compareValues(String hashValue1, String hashValue2)
  {
    return Objects.compare(hashValue1, hashValue2, String::compareTo);
  }
}
