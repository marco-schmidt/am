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
package am.util;

import java.math.BigInteger;
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

  @Test
  public void testGetAsBigInteger()
  {
    Assert.assertNull("Null input yields null result.", StrUtil.getAsBigInteger(null));
    Assert.assertNull("Invalid input yields null result.", StrUtil.getAsBigInteger("xyz"));
    Assert.assertEquals("Input '0' leads to zero.", BigInteger.ZERO, StrUtil.getAsBigInteger("0"));
    Assert.assertEquals("Input '1' leads to one.", BigInteger.ONE, StrUtil.getAsBigInteger("1"));
    Assert.assertEquals("Input '10' leads to ten.", BigInteger.TEN, StrUtil.getAsBigInteger("10"));
    Assert.assertEquals("Input Long.MIN_VALUE leads to that value.", BigInteger.valueOf(Long.MIN_VALUE),
        StrUtil.getAsBigInteger("-9223372036854775808"));
  }

  @Test
  public void testGetAsLongNull()
  {
    Assert.assertNull("Null input leads to null output.", StrUtil.getAsLong(null));
  }

  @Test
  public void testGetAsLongNonNumber()
  {
    Assert.assertNull("Non-number input leads to null output.", StrUtil.getAsLong("xy"));
  }

  @Test
  public void testGetAsLongOne()
  {
    Assert.assertEquals("Input '1' leads to Long one output.", Long.valueOf(1), StrUtil.getAsLong("1"));
  }
}
