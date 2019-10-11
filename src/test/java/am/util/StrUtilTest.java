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
package am.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class {@link StrUtil}.
 *
 * @author Marco Schmidt
 */
public class StrUtilTest
{
  @Test
  public void testEscapeNull()
  {
    Assert.assertNull("Null input yields null result.", StrUtil.escapeControl(null));
  }

  @Test
  public void testEscapeEmpty()
  {
    Assert.assertEquals("Empty input yields empty result.", "", StrUtil.escapeControl(""));
  }

  @Test
  public void testEscapeWithoutEscapeCharacters()
  {
    final String test = "abcABC123!";
    Assert.assertEquals("Input without special characters yields identical result.", test, StrUtil.escapeControl(test));
  }

  @Test
  public void testEscapeWithEscapeCharacters()
  {
    final String input = "\tabcABC123!";
    final String expected = " abcABC123!";
    Assert.assertEquals("Input with special characters yields differing result.", expected,
        StrUtil.escapeControl(input));
  }
}
