/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect.testing.testers;

import static com.google.common.collect.testing.features.CollectionSize.*;
import static com.google.common.collect.testing.features.MapFeature.*;

import com.google.common.collect.Multimap;
import com.google.common.collect.testing.WrongType;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

/**
 * A generic JUnit test which tests {@code containsValue()} operations on a multimap.
 */
public class MultimapContainsValueTester<K, V> extends AbstractMultimapTester<K, V, Multimap<K, V>> {
  @CollectionSize.Require(absent = ZERO)
  public void testContains_yes() {
    assertTrue("containsValue(present) should return true",
        getMultimap().containsValue(samples.e0.getValue()));
  }

  public void testContains_no() {
    assertFalse("containsValue(notPresent) should return false",
        getMultimap().containsValue(samples.e3.getValue()));
  }

  @MapFeature.Require(ALLOWS_NULL_VALUES)
  public void testContains_nullNotContainedButSupported() {
    assertFalse("containsValue(null) should return false",
        getMultimap().containsValue(null));
  }

  @MapFeature.Require(absent = ALLOWS_NULL_VALUES)
  public void testContains_nullNotContainedAndUnsupported() {
    expectNullValueMissingWhenNullValuesUnsupported(
        "containsValue(null) should return false or throw");
  }

  @MapFeature.Require(ALLOWS_NULL_VALUES)
  @CollectionSize.Require(absent = ZERO)
  public void testContains_nonNullWhenNullContained() {
    initMapWithNullValue();
    assertFalse("containsValue(notPresent) should return false",
        getMultimap().containsValue(samples.e3.getValue()));
  }

  @MapFeature.Require(ALLOWS_NULL_VALUES)
  @CollectionSize.Require(absent = ZERO)
  public void testContains_nullContained() {
    initMapWithNullValue();
    assertTrue("containsValue(null) should return true",
        getMultimap().containsValue(null));
  }

  public void testContains_wrongType() {
    try {
      //noinspection SuspiciousMethodCalls
      assertFalse("containsValue(wrongType) should return false or throw",
          getMultimap().containsValue(WrongType.VALUE));
    } catch (ClassCastException tolerated) {
    }
  }
}
