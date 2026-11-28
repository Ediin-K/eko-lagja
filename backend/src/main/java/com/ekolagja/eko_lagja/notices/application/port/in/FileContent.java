package com.ekolagja.eko_lagja.notices.application.port.in;

public record FileContent(String fileName, String contentType, byte[] content) {
}
