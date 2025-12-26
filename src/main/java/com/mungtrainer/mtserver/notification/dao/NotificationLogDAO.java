package com.mungtrainer.mtserver.notification.dao;

import com.mungtrainer.mtserver.notification.entity.NotificationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationLogDAO {

    void insertLog(NotificationLog log);


    void updateStatus(
            @Param("logId") Long logId,
            @Param("status") String status
    );
}

