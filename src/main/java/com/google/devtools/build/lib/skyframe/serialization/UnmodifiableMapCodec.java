// Copyright 2018 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.skyframe.serialization;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** {@link ObjectCodec} for {@link java.util.Collections.UnmodifiableMap}. */
final class UnmodifiableMapCodec<K, V> implements ObjectCodec<Map<K, V>> {
  private final Map<K, V> empty = Collections.unmodifiableMap(new HashMap<>());

  @SuppressWarnings("unchecked")
  @Override
  public Class<Map<K, V>> getEncodedClass() {
    return (Class<Map<K, V>>) empty.getClass();
  }

  @Override
  public void serialize(SerializationContext context, Map<K, V> obj, CodedOutputStream codedOut)
      throws SerializationException, IOException {
    codedOut.writeInt32NoTag(obj.size());
    for (Map.Entry<K, V> entry : obj.entrySet()) {
      context.serialize(entry.getKey(), codedOut);
      context.serialize(entry.getValue(), codedOut);
    }
  }

  @Override
  public Map<K, V> deserialize(DeserializationContext context, CodedInputStream codedIn)
      throws SerializationException, IOException {
    int size = codedIn.readInt32();
    if (size == 0) {
      return empty;
    }
    // Load factor is 0.75, so we need an initial capacity of 4/3 actual size to avoid rehashing.
    LinkedHashMap<K, V> result = new LinkedHashMap<>(4 * size / 3);
    for (int i = 0; i < size; i++) {
      result.put(context.deserialize(codedIn), context.deserialize(codedIn));
    }
    return Collections.unmodifiableMap(result);
  }

}
