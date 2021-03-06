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

import java.util.Arrays;
import java.util.Map.Entry;

import com.google.common.collect.Multimap;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import com.google.common.collect.testing.features.MultimapFeature;
import com.google.common.collect.testing.features.MultimapFeature.Require;

/**
 * A generic JUnit test which tests creation (typically through a constructor or
 * static factory method) of a multimap.
 */
public class MultimapCreationTester<K, V> extends AbstractMultimapTester<K, V, Multimap<K, V>> {
	@MapFeature.Require(ALLOWS_NULL_KEYS)
	@CollectionSize.Require(absent = ZERO)
	public void testCreateWithNullKeySupported() {
		initMapWithNullKey();
		expectContents(createArrayWithNullKey());
	}

	@MapFeature.Require(absent = ALLOWS_NULL_KEYS)
	@CollectionSize.Require(absent = ZERO)
	public void testCreateWithNullKeyUnsupported() {
		try {
			initMapWithNullKey();
			fail("Creating a map containing a null key should fail");
		} catch (NullPointerException expected) {}
	}

	@MapFeature.Require(ALLOWS_NULL_VALUES)
	@CollectionSize.Require(absent = ZERO)
	public void testCreateWithNullValueSupported() {
		initMapWithNullValue();
		expectContents(createArrayWithNullValue());
	}

	@MapFeature.Require(absent = ALLOWS_NULL_VALUES)
	@CollectionSize.Require(absent = ZERO)
	public void testCreateWithNullValueUnsupported() {
		try {
			initMapWithNullValue();
			fail("Creating a map containing a null value should fail");
		} catch (NullPointerException expected) {}
	}

	@MapFeature.Require({ALLOWS_NULL_KEYS, ALLOWS_NULL_VALUES})
	@CollectionSize.Require(absent = ZERO)
	public void testCreateWithNullKeyAndValueSupported() {
		Entry<K, V>[] entries = createSamplesArray();
		entries[getNullLocation()] = entry(null, null);
		resetMultimap(entries);
		expectContents(entries);
	}

	@MultimapFeature.Require(ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require(value = ALLOWS_NULL_KEYS, absent = REJECTS_DUPLICATES_AT_CREATION)
	@CollectionSize.Require(absent = {ZERO, ONE})
	public void testCreateWithDuplicates_nullDuplicatesNotRejected_dupsSupported() {
		Entry<K, V>[] entries = getEntriesMultipleNullKeys();
		resetMultimap(entries);
		expectContents(entries);
	}
	@MultimapFeature.Require(absent=ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require(value = ALLOWS_NULL_KEYS, absent = REJECTS_DUPLICATES_AT_CREATION)
	@CollectionSize.Require(absent = {ZERO, ONE})
	public void testCreateWithDuplicates_nullDuplicatesNotRejected_dupsUnsupported() {
		Entry<K, V>[] entries = getEntriesMultipleNullKeys();
		resetMultimap(entries);
		expectContentsExceptDuplicate(entries);
	}

	@MultimapFeature.Require(ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require(absent = REJECTS_DUPLICATES_AT_CREATION)
	@CollectionSize.Require(absent = {ZERO, ONE})
	public void testCreateWithDuplicates_nonNullDuplicatesNotRejected_dupsSupported() {
		Entry<K, V>[] entries = getEntriesMultipleNonNullKeys();
		resetMultimap(entries);
		expectContents(entries);
	}
	@MultimapFeature.Require(absent=ALLOWS_DUPLICATE_VALUES)
	@MapFeature.Require(absent = REJECTS_DUPLICATES_AT_CREATION)
	@CollectionSize.Require(absent = {ZERO, ONE})
	public void testCreateWithDuplicates_nonNullDuplicatesNotRejected_dupsUnsupported() {
		Entry<K, V>[] entries = getEntriesMultipleNonNullKeys();
		resetMultimap(entries);
		expectContentsExceptDuplicate(entries);
	}

	@MapFeature.Require({ALLOWS_NULL_KEYS, REJECTS_DUPLICATES_AT_CREATION})
	@CollectionSize.Require(absent = {ZERO, ONE})
	public void testCreateWithDuplicates_nullDuplicatesRejected() {
		Entry<K, V>[] entries = getEntriesMultipleNullKeys();
		try {
			resetMultimap(entries);
			fail("Should reject duplicate null elements at creation");
		} catch (IllegalArgumentException expected) {}
	}

	@MapFeature.Require(REJECTS_DUPLICATES_AT_CREATION)
	@CollectionSize.Require(absent = {ZERO, ONE})
	public void testCreateWithDuplicates_nonNullDuplicatesRejected() {
		Entry<K, V>[] entries = getEntriesMultipleNonNullKeys();
		try {
			resetMultimap(entries);
			fail("Should reject duplicate non-null elements at creation");
		} catch (IllegalArgumentException expected) {}
	}

	/*
	 * the below assume that the entry in array index 0 is duplicated
	 */
	private Entry<K, V>[] getEntriesMultipleNullKeys() {
		Entry<K, V>[] entries = createArrayWithNullKey();
		entries[0] = entries[getNullLocation()];
		return entries;
	}
	private Entry<K, V>[] getEntriesMultipleNonNullKeys() {
		Entry<K, V>[] entries = createSamplesArray();
		entries[0] = samples.e1;
		return entries;
	}
	private void expectContentsExceptDuplicate(Entry<K, V>[] entries) {
		Entry<K, V>[] deDup = Arrays.copyOfRange(entries, 1, entries.length);
		expectContents(deDup);
	}
}
