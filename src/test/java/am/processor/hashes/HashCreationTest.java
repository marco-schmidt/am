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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import am.app.AppConfig;
import am.filesystem.model.File;

public class HashCreationTest
{
  private AppConfig config;
  private HashConfig hashConfig;
  private HashCreation creation;

  @Before
  public void setup()
  {
    config = new AppConfig();
    hashConfig = new HashConfig();
    creation = new HashCreation();
    config.setHashConfig(hashConfig);
    hashConfig.setAlgorithm(HashConfig.DEFAULT_HASH_ALGORITHM);
    hashConfig.setPercentage(Double.valueOf(0d));
    hashConfig.setStrategy(HashStrategy.All);
  }

  @Test
  public void toStringEmpty()
  {
    Assert.assertEquals("Empty array leads to empty string.", "", new HashCreation().toString(new byte[]
    {}));
  }

  @Test
  public void toStringNonEmpty()
  {
    Assert.assertEquals("Null array leads to empty string.", "12fe", new HashCreation().toString(new byte[]
    {
        0x12, (byte) 0xfe
    }));
  }

  @Test
  public void toStringNull()
  {
    Assert.assertEquals("Null array leads to empty string.", "", new HashCreation().toString(null));
  }

  @Test
  public void testCreateDigestInvalid()
  {
    hashConfig.setAlgorithm("invalidAlgorithmName");
    final MessageDigest digest = creation.createDigest(config, hashConfig);
    Assert.assertNull("Invalid algorithm name leads to null digest.", digest);
  }

  @Test
  public void testCreateDigestDefault()
  {
    hashConfig.setAlgorithm(HashConfig.DEFAULT_HASH_ALGORITHM);
    final MessageDigest digest = creation.createDigest(config, hashConfig);
    Assert.assertNotNull("Default algorithm name leads to non-null digest.", digest);
  }

  @Test
  public void testUpdate()
  {
    hashConfig.setAlgorithm(HashConfig.DEFAULT_HASH_ALGORITHM);
    final File file = new File();
    creation.update(config, file);
  }

  @Test
  public void testUpdateInputStreamEmpty()
  {
    hashConfig.setAlgorithm(HashConfig.DEFAULT_HASH_ALGORITHM);
    final MessageDigest digest = creation.createDigest(config, hashConfig);
    final InputStream in = new ByteArrayInputStream(new byte[]
    {});
    final File file = new File();
    file.setByteSize(Long.valueOf(0));
    creation.update(config, file, digest, in, "testin");
    final String hashValue = file.getHashValue();
    Assert.assertNotNull("After update we do have a hash value.", hashValue);
    Assert.assertEquals("Empty SHA-256 message digest value expected.",
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hashValue);
    Assert.assertNotNull("After update we do have a hash date.", file.getHashCreated());
  }

  @Test
  public void testUpdateInputStreamAbc()
  {
    hashConfig.setAlgorithm(HashConfig.DEFAULT_HASH_ALGORITHM);
    final MessageDigest digest = creation.createDigest(config, hashConfig);
    final InputStream in = new ByteArrayInputStream(new byte[]
    {
        'a', 'b', 'c'
    });
    final File file = new File();
    file.setByteSize(Long.valueOf(3));
    creation.update(config, file, digest, in, "testin");
    final String hashValue = file.getHashValue();
    Assert.assertNotNull("After update we do have a hash value.", hashValue);
    Assert.assertEquals("Empty SHA-256 message digest value expected.",
        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hashValue);
    Assert.assertNotNull("After update we do have a hash date.", file.getHashCreated());
  }
}
