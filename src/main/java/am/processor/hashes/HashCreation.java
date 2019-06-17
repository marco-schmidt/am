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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.FileSystemHelper;
import am.filesystem.model.File;

/**
 * Create hash values from input streams like files.
 *
 * @author Marco Schmidt
 */
public class HashCreation
{
  private static final Logger LOGGER = LoggerFactory.getLogger(HashCreation.class);

  public void update(final AppConfig config, final File file)
  {
    // create digest to be used to compute hash value
    MessageDigest digest;
    final String algorithm = config.getHashConfig().getAlgorithm();
    try
    {
      digest = MessageDigest.getInstance(algorithm);
      update(config, file, digest);
    }
    catch (final NoSuchAlgorithmException e)
    {
      LOGGER.error(config.msg("hashcreation.error.unknown_algorithm", algorithm), e);
    }
  }

  public void update(final AppConfig config, final File file, final MessageDigest digest)
  {
    // create input stream to read from
    InputStream input = null;
    final java.io.File entry = file.getEntry();
    try
    {
      input = new FileInputStream(entry);
      update(config, file, digest, input, entry.getAbsolutePath());
    }
    catch (final FileNotFoundException e)
    {
      LOGGER.error(config.msg("hashcreation.error.file_open_failed", entry.getAbsolutePath()), e);
    }
    FileSystemHelper.close(input);
  }

  public void update(final AppConfig config, final File file, final MessageDigest digest, final InputStream input,
      final String inputName)
  {
    long timeMillis = System.currentTimeMillis();

    // create buffer
    final byte[] buffer = new byte[1024 * 1024];

    // read complete file in chunks and update digest
    int numRead;
    try
    {
      while ((numRead = input.read(buffer)) >= 0)
      {
        digest.update(buffer, 0, numRead);
      }
    }
    catch (final IOException e)
    {
      LOGGER.error(config.msg("hashcreation.error.input_read", inputName), e);
    }

    // create final digest as byte array
    final byte[] result = digest.digest();

    // convert byte array to string}
    final String hashValue = toString(result);

    // store hash value and time of its creation (now) in file object
    file.setHashValue(hashValue);
    file.setHashCreated(new Date());
    timeMillis = System.currentTimeMillis() - timeMillis;
    long mbPerSecond = 0;
    if (timeMillis > 0 && file.getByteSize().longValue() > 0)
    {
      mbPerSecond = file.getByteSize().longValue() / timeMillis / 1000L;
    }
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug(config.msg("hashcreation.debug.computed_value", hashValue, inputName, timeMillis, mbPerSecond));
    }
  }

  /**
   * Convert byte array to hexadecimal string.
   *
   * @param hashValueArray
   *          byte array to convert
   * @return string with hexadecimal representation, twice the length of input
   */
  private String toString(byte[] hashValueArray)
  {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < hashValueArray.length; i++)
    {
      sb.append(String.format("%02x", hashValueArray[i]));
    }
    return sb.toString();
  }
}
