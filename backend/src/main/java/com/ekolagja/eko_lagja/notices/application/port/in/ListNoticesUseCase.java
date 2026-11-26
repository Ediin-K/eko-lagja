package com.ekolagja.eko_lagja.notices.application.port.in;

import java.util.List;

public interface ListNoticesUseCase {

	List<NoticeView> list(Long neighbourhoodId);
}
