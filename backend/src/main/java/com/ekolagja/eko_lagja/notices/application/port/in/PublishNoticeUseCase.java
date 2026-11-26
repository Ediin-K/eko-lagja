package com.ekolagja.eko_lagja.notices.application.port.in;

public interface PublishNoticeUseCase {

	Long publish(PublishNoticeCommand command);
}
