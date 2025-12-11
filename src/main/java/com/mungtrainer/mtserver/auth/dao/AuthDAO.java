package com.mungtrainer.mtserver.auth.dao;

import com.mungtrainer.mtserver.trainer.entity.TrainerProfile;
import com.mungtrainer.mtserver.trainer.entity.TrainerUser;
import com.mungtrainer.mtserver.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthDAO {
  boolean existsUsername(String userName);
  boolean existsEmail(String email);
  void insertUser(User user);
  void insertTrainerProfile(TrainerProfile trainerProfile);
  void insertTrainerUser(TrainerUser trainerUser);
  void updateRefreshToken(Long userId, String refreshToken);

  // TODO: Trainer DAO로 옮기던가 해야 함.
  long findTrainerIdByRegistCode(String registCode);
  boolean existsRegistCode(String registCode);

  // TODO: User DAO로 옮기던가 해야 함.
  User findByUserName(String userName);
}
