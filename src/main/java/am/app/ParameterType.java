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
package am.app;

/**
 * Enumeration of types for command line parameter arguments.
 */
public enum ParameterType
{
  /** Parameter is true or false. */
  Boolean,

  /** Parameter is a regular file, symlink or hardlink. */
  File,

  /** Parameter is a string. */
  String,

  /** Parameter is an integer value. */
  Integer,

  /** Parameter is a directory. */
  Directory;
}
