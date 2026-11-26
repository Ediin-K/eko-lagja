package com.ekolagja.eko_lagja.notices.application.port.in;

public record FileUpload(String fileName, String contentType, byte[] content) {
}
