package com.mungtrainer.mtserver.user.mapper;

import com.mungtrainer.mtserver.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    /**
     * 사용자 ID로 조회
     */
    Optional<User> findById(@Param("userId") Long userId);

    /**
     * 사용자 프로필 수정
     */
    int updateUserProfile(User user);
}