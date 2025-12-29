package com.mungtrainer.mtserver.user.dao;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mungtrainer.mtserver.user.entity.User;

@Mapper
public interface UserDAO {

	/**
	 * 사용자 ID로 조회
	 */
	Optional<User> findById(@Param("userId")
	Long userId);

	/**
	 * 사용자명으로 조회
	 */
	Optional<User> findByUserName(@Param("userName")
	String userName);

	/**
	 * 사용자 프로필 수정
	 */
	int updateUserProfile(User user);

	/**
	 * 트레이너와 회원의 연결 여부 확인
	 */
	boolean isConnectedToTrainer(@Param("userId")
	Long userId, @Param("trainerId")
	Long trainerId);

	/**
	 * 사용자 반려견 프로필 공개 여부 변경
	 */
	int updatePublicStatus(@Param("userId")
	Long userId, @Param("isPublic")
	Boolean isPublic);

	/** 
	 *  사용자의 FCM 토큰 조회
	 */
	Optional<String> findUserFCMToken(@Param("userId")
	Long userId);

	/** 
	 *  사용자의 FCM 토큰 조회
	 */
	Optional<String> findUserFCMTokenByUserName(@Param("userName")
	String userName);

}