package com.mungtrainer.mtserver.auth.dao;

import org.apache.ibatis.annotations.Mapper;

import com.mungtrainer.mtserver.trainer.entity.TrainerProfile;
import com.mungtrainer.mtserver.trainer.entity.TrainerUser;
import com.mungtrainer.mtserver.user.entity.User;

@Mapper
public interface AuthDAO {
	boolean existsUsername(String userName);

	boolean existsEmail(String email);

	void insertUser(User user);

	void insertTrainerProfile(TrainerProfile trainerProfile);

	void insertTrainerUser(TrainerUser trainerUser);

	void updateRefreshToken(Long userId, String refreshToken);

	void updateFcmToken(Long userId, String fcmToken);

	void updatePassword(Long userId, String password);

	// TODO: Trainer DAO로 옮기던가 해야 함.
	long findTrainerIdByRegistCode(String registCode);

	boolean existsRegistCode(String registCode);

	// TODO: User DAO로 옮기던가 해야 함.
	User findByUserName(String userName);
}
