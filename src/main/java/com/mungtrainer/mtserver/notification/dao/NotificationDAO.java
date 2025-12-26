package com.mungtrainer.mtserver.notification.dao;

import com.mungtrainer.mtserver.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationDAO {

    void insert(Notification notification);

    Notification findById(@Param("notificationId") Long notificationId);

    int markAsRead(@Param("notificationId") Long notificationId,
                    @Param("userId") Long userId);

    List<Notification> findAfterId(
            @Param("userId") Long userId,
            @Param("lastEventId") Long lastEventId
    );

}
