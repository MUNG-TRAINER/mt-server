package com.mungtrainer.mtserver.notification.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationSseClientDAO {

//    사용자가 SSE 연결을 시작할 때
//    notification_sse_client 테이블에
//    없으면 INSERT
//    있으면 UPDATE
void upsertActiveClient(
        @Param("userId") Long userId,
        @Param("isActive") boolean isActive,
        @Param("actorId") Long actorId
);


    //    마지막으로 성공적으로 전송된 알림 ID 저장
//    SSE 끊김 후 재연결 시
//    놓친 알림 재전송 가능
//이 사용자는 notification_id = X 까지 받았다
    void updateLastEventId(
            @Param("userId") Long userId,
            @Param("lastEventId") Long lastEventId
    );

    Long findLastEventId(@Param("userId") Long userId);


//    SSE 연결 종료 시
//    is_active = false로 변경
//    disconnect(userId) 일 때 호출 -> 이 사용자는 현재 실시간 수신 불가 상태”
    void updateActive(
            @Param("userId") Long userId,
            @Param("isActive") boolean isActive
    );
}
