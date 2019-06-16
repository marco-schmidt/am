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

import java.security.MessageDigest;

/**
 * Configuration for file hashes.
 *
 * @author Marco Schmidt
 */
public class HashConfig
{
  /**
   * Default name of hash algorithm to be used in {@link MessageDigest#getInstance(String)}.
   */
  public static final String DEFAULT_HASH_ALGORITHM = "SHA-256";

  /**
   * Default percentage value when following hashing strategy {@link HashStrategy#Percentage}.
   */
  public static final Double DEFAULT_PERCENTAGE = Double.valueOf(3.33d);

  /**
   * Default hashing strategy.
   */
  public static final HashStrategy DEFAULT_STRATEGY = HashStrategy.Percentage;

  private String algorithm = DEFAULT_HASH_ALGORITHM;
  private Double percentage = DEFAULT_PERCENTAGE;
  private HashStrategy strategy = DEFAULT_STRATEGY;

  public String getAlgorithm()
  {
    return algorithm;
  }

  public void setAlgorithm(String hashAlgorithm)
  {
    this.algorithm = hashAlgorithm;
  }

  public Double getPercentage()
  {
    return percentage;
  }

  public void setPercentage(Double percentage)
  {
    this.percentage = percentage;
  }

  public HashStrategy getStrategy()
  {
    return strategy;
  }

  public void setStrategy(HashStrategy strategy)
  {
    this.strategy = strategy;
  }
}
