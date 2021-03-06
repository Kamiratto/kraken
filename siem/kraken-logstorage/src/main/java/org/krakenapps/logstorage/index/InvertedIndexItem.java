/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logstorage.index;

import java.util.Map;

public class InvertedIndexItem implements Comparable<InvertedIndexItem> {
	public long timestamp;
	public long id;
	public String[] tokens;
	public Map<String, String> keyTokens;

	public InvertedIndexItem(long timestamp, long id, String[] tokens, Map<String, String> keyTokens) {
		this.timestamp = timestamp;
		this.id = id;
		this.tokens = tokens;
		this.keyTokens = keyTokens;
	}

	@Override
	public int compareTo(InvertedIndexItem o) {
		return (int) (o.id - id);
	}

}
