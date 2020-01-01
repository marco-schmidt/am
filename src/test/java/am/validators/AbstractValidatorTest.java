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
package am.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link TvSeriesValidator} class.
 */
public class AbstractValidatorTest
{
  @Before
  public void setup()
  {
    AbstractValidator.register(MovieValidator.class);
    AbstractValidator.register(TvSeriesValidator.class);
  }

  @Test
  public void testHasRegisteredValidators()
  {
    Assert.assertTrue("After setup there are registered validators.", AbstractValidator.hasRegisteredValidators());
  }

  @Test
  public void testFindByNameNull()
  {
    Assert.assertNull("Null validator name leads to null validator class.", AbstractValidator.findByName(null));
  }

  @Test
  public void testFindByNameEmpty()
  {
    Assert.assertNull("Empty validator name leads to null validator class.", AbstractValidator.findByName(""));
  }

  @Test
  public void testFindByNameMovieValidator()
  {
    final Class<? extends AbstractValidator> val = AbstractValidator.findByName("MovieValidator");
    Assert.assertNotNull("Movie validator name leads to non-null class.", val);
    Assert.assertEquals("Movie validator name leads to movie validator class.", MovieValidator.class, val);
  }
}
