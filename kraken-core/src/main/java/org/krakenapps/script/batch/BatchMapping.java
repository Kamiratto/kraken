/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.script.batch;

import java.io.File;

public class BatchMapping {
	private String alias;
	private File scriptFile;

	public BatchMapping(String alias, File scriptFile) {
		this.alias = alias;
		this.scriptFile = scriptFile;
	}

	public String getAlias() {
		return alias;
	}

	public File getScriptFile() {
		return scriptFile;
	}

	@Override
	public String toString() {
		return "[" + alias + "] " + scriptFile;
	}
}