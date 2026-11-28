package com.ekolagja.eko_lagja.notices.application.port.in;

import java.util.List;

public record MyNotifications(List<NotificationView> items, long unreadCount) {
}
