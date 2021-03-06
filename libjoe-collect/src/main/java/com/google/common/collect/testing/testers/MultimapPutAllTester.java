/*
 * Copyright (C) 2007 Google Inc.
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
import static com.google.common.collect.testing.features.MultimapFeature.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import com.google.common.collect.testing.features.MultimapFeature;
import com.google.common.collect.testing.features.MultimapFeature.Require;

/**
 * A generic JUnit test which tests {@code putAll} operations on a multimap.
 */
@SuppressWarnings("unchecked")
// too many "unchecked generic array creations"
public class MultimapPutAllTester<K, V> extends AbstractMultimapTester<K, V, Multimap<K, V>> {
	private List<Entry<K, V>> containsNullKey;
	private List<Entry<K, V>> containsNullValue;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		containsNullKey = singletonList(entry(null, samples.e3.getValue()));
		containsNullValue = singletonList(entry(samples.e3.getKey(), null));
	}

	@MapFeature.Require(SUPPORTS_PUT_ALL)
	public void testPutAll_supportedNothing() {
		getMultimap().putAll(emptyMultimap());
		expectUnchanged();
	}

	@MapFeature.Require(absent = SUPPORTS_PUT_ALL)
	public void testPutAll_unsupportedNothing() {
		try {
			getMultimap().putAll(emptyMultimap());
		} catch (UnsupportedOperationException tolerated) {}
		expectUnchanged();
	}

	@MapFeature.Require(SUPPORTS_PUT_ALL)
	public void testPutAll_supportedNonePresent() {
		putAll(createDisjointCollection());
		expectAdded(samples.e3, samples.e4);
	}

	@MapFeature.Require(absent = SUPPORTS_PUT_ALL)
	public void testPutAll_unsupportedNonePresent() {
		try {
			putAll(createDisjointCollection());
			fail("putAll(nonePresent) should throw");
		} catch (UnsupportedOperationException expected) {}
		expectUnchanged();
		expectMissing(samples.e3, samples.e4);
	}

	@MultimapFeature.Require(ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require(SUPPORTS_PUT_ALL)
	@CollectionSize.Require(absent = ZERO)
	public void testPutAll_supportedSomePresent_dupsSupported() {
		putAll(MinimalCollection.of(samples.e3, samples.e0));
		expectAdded(samples.e3, samples.e0);
	}
	@MultimapFeature.Require(absent = ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require(SUPPORTS_PUT_ALL)
	@CollectionSize.Require(absent = ZERO)
	public void testPutAll_supportedSomePresent_dupsUnsupported() {
		putAll(MinimalCollection.of(samples.e3, samples.e0));
		expectAdded(samples.e3);
	}

	@MapFeature.Require(absent = SUPPORTS_PUT_ALL)
	@CollectionSize.Require(absent = ZERO)
	public void testPutAll_unsupportedSomePresent() {
		try {
			putAll(MinimalCollection.of(samples.e3, samples.e0));
			fail("putAll(somePresent) should throw");
		} catch (UnsupportedOperationException expected) {}
		expectUnchanged();
	}

	@MapFeature.Require(absent = SUPPORTS_PUT_ALL)
	@CollectionSize.Require(absent = ZERO)
	public void testPutAll_unsupportedAllPresent() {
		try {
			putAll(MinimalCollection.of(samples.e0));
		} catch (UnsupportedOperationException tolerated) {}
		expectUnchanged();
	}

	@MapFeature.Require({SUPPORTS_PUT_ALL, ALLOWS_NULL_KEYS})
	public void testPutAll_nullKeySupported() {
		putAll(containsNullKey);
		expectAdded(containsNullKey.get(0));
	}

	@MapFeature.Require(value = SUPPORTS_PUT_ALL, absent = ALLOWS_NULL_KEYS)
	public void testAdd_nullKeyUnsupported() {
		try {
			putAll(containsNullKey);
			fail("putAll(containsNullKey) should throw");
		} catch (NullPointerException expected) {}
		expectUnchanged();
		expectNullKeyMissingWhenNullKeysUnsupported("Should not contain null key after unsupported "
				+ "putAll(containsNullKey)");
	}

	@MapFeature.Require({SUPPORTS_PUT_ALL, ALLOWS_NULL_VALUES})
	public void testPutAll_nullValueSupported() {
		putAll(containsNullValue);
		expectAdded(containsNullValue.get(0));
	}

	@MapFeature.Require(value = SUPPORTS_PUT_ALL, absent = ALLOWS_NULL_VALUES)
	public void testAdd_nullValueUnsupported() {
		try {
			putAll(containsNullValue);
			fail("putAll(containsNullValue) should throw");
		} catch (NullPointerException expected) {}
		expectUnchanged();
		expectNullValueMissingWhenNullValuesUnsupported("Should not contain null value after unsupported "
				+ "putAll(containsNullValue)");
	}

	@MapFeature.Require(SUPPORTS_PUT_ALL)
	public void testPutAll_nullCollectionReference() {
		try {
			getMultimap().putAll(null);
			fail("putAll(null) should throw NullPointerException");
		} catch (NullPointerException expected) {}
	}

	private Multimap<K, V> emptyMultimap() {
		return ImmutableMultimap.of();
	}

	private void putAll(Iterable<Entry<K, V>> entries) {
		Multimap<K, V> map = LinkedHashMultimap.create();
		for (Entry<K, V> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		getMultimap().putAll(map);
	}
}
