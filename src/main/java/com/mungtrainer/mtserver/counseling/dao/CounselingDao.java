package com.mungtrainer.mtserver.counseling.dao;

import com.mungtrainer.mtserver.counseling.entity.Counseling;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CounselingDao {
    // 상담 등록
    int insertCounseling(Counseling counseling);

    // 상담 취소
    int cancelCounseling(Long counselingId);

//    Counseling selectCounselingById(Long counselingId);
}
