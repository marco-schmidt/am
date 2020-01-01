/*
 * Copyright 2019, 2020 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.FileSystemHelper;
import am.filesystem.model.File;
import am.filesystem.model.FileState;

/**
 * Create hash values from input streams like files.
 *
 * @author Marco Schmidt
 */
public class HashCreation
{
  private static final Logger LOGGER = LoggerFactory.getLogger(HashCreation.class);

  public MessageDigest createDigest(final AppConfig config, final HashConfig hashConfig)
  {
    MessageDigest digest;
    final String algorithm = hashConfig.getAlgorithm();
    try
    {
      digest = MessageDigest.getInstance(algorithm);
    }
    catch (final NoSuchAlgorithmException e)
    {
      digest = null;
      LOGGER.error(config.msg("hashcreation.error.unknown_algorithm", algorithm), e);
    }
    return digest;
  }

  public void update(final AppConfig config, final File file)
  {
    // create digest to be used to compute hash value
    final MessageDigest digest = createDigest(config, config.getHashConfig());
    if (digest != null)
    {
      update(config, file, digest);
    }
  }

  public void update(final AppConfig config, final File file, final MessageDigest digest)
  {
    // create input stream to read from
    InputStream input = null;
    final java.io.File entry = file.getEntry();
    if (entry == null)
    {
      LOGGER.error(config.msg("hashcreation.error.no_file_object"));
      return;
    }
    final String path = entry.getAbsolutePath();
    try
    {
      input = Files.newInputStream(entry.toPath());
      update(config, file, digest, input, path);
    }
    catch (final InvalidPathException ipe)
    {
      LOGGER.error(config.msg("hashcreation.error.path_conversion_failed", path), ipe);
    }
    catch (final IOException e)
    {
      LOGGER.error(config.msg("hashcreation.error.file_open_failed", path), e);
    }
    FileSystemHelper.close(input);
  }

  public void update(final AppConfig config, final File file, final MessageDigest digest, final InputStream input,
      final String inputName)
  {
    long timeMillis = System.currentTimeMillis();

    // create buffer
    final long fileSize = file.getByteSize() == null ? Integer.MAX_VALUE : file.getByteSize();
    final int bufferSize = Math
        .max(Math.min(1024 * 1024, fileSize > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) fileSize), 1024);
    final byte[] buffer = new byte[bufferSize];

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
      file.setState(FileState.Corrupted);
      return;
    }

    // create final digest as byte array
    final byte[] result = digest.digest();

    // convert byte array to string}
    final String hashValue = toString(result);
    if (LOGGER.isDebugEnabled())
    {
      timeMillis = System.currentTimeMillis() - timeMillis;
      long mbPerSecond = 0;
      if (timeMillis > 0 && file.getByteSize().longValue() > 0)
      {
        mbPerSecond = file.getByteSize().longValue() / timeMillis / 1000L;
      }
      LOGGER.debug(config.msg("hashcreation.debug.computed_value", hashValue, inputName, timeMillis, mbPerSecond));
    }

    updateFileState(config, file, hashValue, inputName);
  }

  private void updateFileState(final AppConfig config, final File file, final String hashValue, final String inputName)
  {
    final String oldHashValue = file.getHashValue();
    if (oldHashValue == null)
    {
      // first time hash was computed: store value and time of its creation (now) in file object
      file.setHashValue(hashValue);
      file.setHashCreated(new Date());
    }
    else
    {
      // compare hash to existing value
      if (oldHashValue.equals(hashValue))
      {
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug(config.msg("hashcreation.debug.value_identical", hashValue, inputName));
        }
        file.setHashCreated(new Date());
      }
      else
      {
        // values differ, file was modified or corrupted, do not update value and date
        LOGGER.warn(config.msg("hashcreation.warn.value_differs", inputName, oldHashValue, hashValue));
        file.setState(FileState.Modified);
      }
    }
  }

  /**
   * Convert byte array to hexadecimal string.
   *
   * @param hashValueArray
   *          byte array to convert
   * @return string with hexadecimal representation, twice the length of input
   */
  String toString(byte[] hashValueArray)
  {
    final StringBuilder sb = new StringBuilder();
    if (hashValueArray != null)
    {
      for (int i = 0; i < hashValueArray.length; i++)
      {
        sb.append(String.format("%02x", hashValueArray[i]));
      }
    }
    return sb.toString();
  }
}
