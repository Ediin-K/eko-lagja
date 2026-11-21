package com.ekolagja.eko_lagja.complaints;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class DomainIsPlainJavaTest {

	private static final Path DOMAIN_SOURCES =
			Path.of("src/main/java/com/ekolagja/eko_lagja/complaints/domain");

	private static final List<String> ALLOWED_IMPORTS = List.of(
			"java.",
			"com.ekolagja.eko_lagja.complaints.domain.");

	@Test
	void domainOnlyImportsPlainJava() throws IOException {
		assertThat(DOMAIN_SOURCES).as("domain source folder").isDirectory();

		List<String> forbidden;
		try (Stream<Path> files = Files.walk(DOMAIN_SOURCES)) {
			forbidden = files
					.filter(path -> path.toString().endsWith(".java"))
					.flatMap(DomainIsPlainJavaTest::forbiddenImports)
					.toList();
		}

		assertThat(forbidden).as("imports that are not plain Java").isEmpty();
	}

	private static Stream<String> forbiddenImports(Path file) {
		try {
			return Files.readAllLines(file).stream()
					.map(String::strip)
					.filter(line -> line.startsWith("import "))
					.map(line -> line.replaceFirst("^import\\s+(static\\s+)?", "").replace(";", "").strip())
					.filter(name -> ALLOWED_IMPORTS.stream().noneMatch(name::startsWith))
					.map(name -> DOMAIN_SOURCES.relativize(file) + ": " + name);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
