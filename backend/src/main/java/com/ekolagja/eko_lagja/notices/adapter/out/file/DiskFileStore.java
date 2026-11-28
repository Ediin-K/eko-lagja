package com.ekolagja.eko_lagja.notices.adapter.out.file;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ekolagja.eko_lagja.notices.application.port.out.FileStore;

@Component
class DiskFileStore implements FileStore {

	private static final Pattern STORAGE_KEY = Pattern.compile("[A-Za-z0-9-]{1,64}");

	private final Path folder;

	DiskFileStore(@Value("${app.notices.files-dir:${user.home}/.ekolagja/notice-files}") String folder) {
		this.folder = Path.of(folder).toAbsolutePath().normalize();
	}

	@Override
	public void write(String storageKey, byte[] content) {
		Path file = fileFor(storageKey);
		try {
			Files.createDirectories(folder);
			Files.write(file, content, StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			throw new UncheckedIOException("could not store file " + storageKey, e);
		}
	}

	@Override
	public byte[] read(String storageKey) {
		try {
			return Files.readAllBytes(fileFor(storageKey));
		} catch (IOException e) {
			throw new UncheckedIOException("could not read file " + storageKey, e);
		}
	}

	private Path fileFor(String storageKey) {
		if (storageKey == null || !STORAGE_KEY.matcher(storageKey).matches()) {
			throw new IllegalArgumentException("invalid storage key");
		}
		return folder.resolve(storageKey);
	}
}
