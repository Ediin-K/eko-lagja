package com.ekolagja.eko_lagja.notices.application.port.out;

public interface FileStore {

	void write(String storageKey, byte[] content);

	byte[] read(String storageKey);
}
