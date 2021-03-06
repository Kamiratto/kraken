package org.krakenapps.filemon.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.krakenapps.filemon.FileMonitorEventListener;

public class FileWatcher {
	private Set<FileMonitorEventListener> callbacks;

	public FileWatcher() {
		callbacks = new HashSet<FileMonitorEventListener>();
	}

	public void register(FileMonitorEventListener callback) {
		callbacks.add(callback);
	}

	public void unregister(FileMonitorEventListener callback) {
		callbacks.remove(callback);
	}

	public void watch(String directoryPath) throws Exception {
		Path path = Paths.get(directoryPath);
		WatchService watcher = path.getFileSystem().newWatchService();
		path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

		WatchKey watchKey = watcher.take();

		List<WatchEvent<?>> events = watchKey.pollEvents();
		for (WatchEvent<?> event : events) {
			if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
				for (FileMonitorEventListener callback : callbacks) {
					callback.onCreated(new File(event.context().toString()));
				}
			}
			if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
				for (FileMonitorEventListener callback : callbacks) {
					callback.onDeleted(new File(event.context().toString()));
				}
			}

			if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
				for (FileMonitorEventListener callback : callbacks) {
					callback.onModified(new File(event.context().toString()));
				}
			}
		}
	}
}