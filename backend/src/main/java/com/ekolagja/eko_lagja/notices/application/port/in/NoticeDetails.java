package com.ekolagja.eko_lagja.notices.application.port.in;

import java.util.List;

public record NoticeDetails(NoticeView notice, List<NoticeFileView> files) {
}
