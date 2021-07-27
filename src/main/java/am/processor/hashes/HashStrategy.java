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

/**
 * Enumeration type to describe how the application decides in a single run which hashes are to be computed.
 *
 * @author Marco Schmidt
 */
public enum HashStrategy
{
  /**
   * Compute hashes for all files.
   */
  All,

  /**
   * Do not compute any hashes.
   */
  None,

  /**
   * Compute hashes for a part of the available files given as a percentage value from 0 (none) to 100 (all).
   */
  Percentage,

  /**
   * Compute hashes until a given amount of data has been consumed. Note that the hash for the current file is computed
   * even if this means that the amount of data is exceeded. Not yet implemented.
   */
  Data,

  /**
   * Compute hashes until a given time has passed. Not yet implemented.
   */
  Time,

  /**
   * Compute hashes for a given number of files. Time may vary depending on their size. Not yet implemented.
   */
  Files
}
