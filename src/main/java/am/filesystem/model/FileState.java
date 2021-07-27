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
package am.filesystem.model;

/**
 * Enumeration type for the current state of a file compared to its previous state.
 *
 * @author Marco Schmidt
 */
public enum FileState
{
  /**
   * The file's state is unknown, the default value in a newly-created {@link am.filesystem.model.File}.
   */
  Unknown(0),

  /**
   * File was added to directory.
   */
  New(1),

  /**
   * File has remained the same.
   */
  Identical(2),

  /**
   * File was changed, either its content or metadata.
   */
  Modified(3),

  /**
   * File no longer exists.
   */
  Missing(4),

  /**
   * There were errors reading from the file.
   */
  Corrupted(5);

  private int numericValue;

  FileState(int numericValue)
  {
    this.numericValue = numericValue;
  }

  public int getNumericValue()
  {
    return numericValue;
  }
}
