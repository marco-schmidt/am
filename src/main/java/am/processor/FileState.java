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

/**
 * Enumeration type for the current state of a file compared to its previous state.
 *
 * @author Marco Schmidt
 */
public enum FileState
{
  /**
   * File was added to directory.
   */
  New,

  /**
   * File no longer exists.
   */
  Missing,

  /**
   * File was changed, either its content or metadata.
   */
  Modified,

  /**
   * File has remained the same.
   */
  Identical,

  /**
   * The file's state is unknown, the default value in a newly-created {@link am.filesystem.model.File}.
   */
  Unknown;
}
