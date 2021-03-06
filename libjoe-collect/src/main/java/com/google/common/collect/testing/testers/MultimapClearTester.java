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
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

/**
 * A generic JUnit test which tests {@code clear()} operations on a multimap.
 */
public class MultimapClearTester<K, V> extends AbstractMultimapTester<K, V, Multimap<K, V>> {
	@MapFeature.Require(SUPPORTS_CLEAR)
	public void testClear() {
		getMultimap().clear();
		assertTrue("After clear(), a map should be empty.", getMultimap().isEmpty());
	}

	@MapFeature.Require(absent = SUPPORTS_CLEAR)
	@CollectionSize.Require(absent = ZERO)
	public void testClear_unsupported() {
		try {
			getMultimap().clear();
			fail("clear() should throw UnsupportedOperation if a map does " + "not support it and is not empty.");
		} catch (UnsupportedOperationException expected) {}
		expectUnchanged();
	}

	@MapFeature.Require(absent = SUPPORTS_CLEAR)
	@CollectionSize.Require(ZERO)
	public void testClear_unsupportedByEmptyCollection() {
		try {
			getMultimap().clear();
		} catch (UnsupportedOperationException tolerated) {}
		expectUnchanged();
	}
}
