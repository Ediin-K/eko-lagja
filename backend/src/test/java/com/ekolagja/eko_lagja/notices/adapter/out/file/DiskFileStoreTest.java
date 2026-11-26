package com.ekolagja.eko_lagja.notices.adapter.out.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DiskFileStoreTest {

	private static final byte[] PDF = "%PDF-1.7\n".getBytes();

	@TempDir
	Path folder;

	@Test
	void writtenFileReadsBackFromItsFolder() {
		DiskFileStore store = new DiskFileStore(folder.resolve("files").toString());

		store.write("0f3a9c-11", PDF);

		assertThat(store.read("0f3a9c-11")).isEqualTo(PDF);
		assertThat(folder.resolve("files").resolve("0f3a9c-11")).exists();
	}

	@Test
	void existingFileIsNeverOverwritten() {
		DiskFileStore store = new DiskFileStore(folder.toString());
		store.write("same-key", PDF);

		assertThatThrownBy(() -> store.write("same-key", new byte[] { 1, 2, 3 }))
				.isInstanceOf(UncheckedIOException.class);
		assertThat(store.read("same-key")).isEqualTo(PDF);
	}

	@Test
	void keysThatCouldLeaveTheFolderAreRefused() throws Exception {
		Files.writeString(folder.resolve("secret.txt"), "secret");
		DiskFileStore store = new DiskFileStore(folder.resolve("files").toString());

		assertThatIllegalArgumentException().isThrownBy(() -> store.read("../secret.txt"));
		assertThatIllegalArgumentException().isThrownBy(() -> store.write("..\\evil", PDF));
		assertThatIllegalArgumentException().isThrownBy(() -> store.write("a/b", PDF));
		assertThatIllegalArgumentException().isThrownBy(() -> store.read(""));
		assertThatIllegalArgumentException().isThrownBy(() -> store.read(null));
	}

	@Test
	void missingFileCannotBeRead() {
		DiskFileStore store = new DiskFileStore(folder.toString());

		assertThatThrownBy(() -> store.read("missing")).isInstanceOf(UncheckedIOException.class);
	}
}
