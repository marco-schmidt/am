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

/**
 * Static helper methods to deal with strings.
 */
public final class StrUtil
{
  private StrUtil()
  {
  }

  /**
   * Replace all control characters (character value below 32) with a single space character. This makes a string safer
   * for printing. If input contains no control character the input string itself is returned.
   *
   * @param input
   *          string to check
   * @return input string if there were no control characters, input with control characters replaced with spaces
   *         otherwise
   */
  public static String escapeControl(final String input)
  {
    String result = input;
    if (input != null)
    {
      boolean hasControlCharacter = false;
      final char[] array = input.toCharArray();
      for (int index = 0; index < input.length(); index++)
      {
        final char ch = array[index];
        if (ch < 32)
        {
          hasControlCharacter = true;
          array[index] = ' ';
        }
      }
      if (hasControlCharacter)
      {
        result = new String(array);
      }
    }
    return result;
  }

  /**
   * Convert string to {@link BigInteger} object.
   *
   * @param s
   *          String to be converted
   * @return resulting BigInteger, possibly null
   */
  public static BigInteger getAsBigInteger(final String s)
  {
    BigInteger result;
    try
    {
      if (s == null)
      {
        result = null;
      }
      else
      {
        result = new BigInteger(s);
      }
    }
    catch (final NumberFormatException nfe)
    {
      result = null;
    }
    return result;
  }

  /**
   * Parse number string (radix 10) into {@link Long} object.
   *
   * @param s
   *          String containing number
   * @return Long object or null if argument could not be represented as a Long value
   */
  public static Long getAsLong(String s)
  {
    try
    {
      return Long.valueOf(s);
    }
    catch (final NumberFormatException nfe)
    {
      return null;
    }
  }
}
