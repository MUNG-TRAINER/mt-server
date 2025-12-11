package com.mungtrainer.mtserver.auth.service;

import com.mungtrainer.mtserver.auth.dao.AuthDAO;
import com.mungtrainer.mtserver.auth.dto.request.AuthTrainerJoinRequest;
import com.mungtrainer.mtserver.auth.dto.request.AuthUserJoinRequest;
import com.mungtrainer.mtserver.auth.dto.response.AuthJoinResponse;
import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.trainer.entity.TrainerProfile;
import com.mungtrainer.mtserver.trainer.entity.TrainerUser;
import com.mungtrainer.mtserver.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final AuthDAO authDAO;
  private final PasswordEncoder passwordEncoder;

  public enum Role {
    USER, TRAINER, ADMIN
  }

  @Transactional
  public AuthJoinResponse userJoin(AuthUserJoinRequest req) {
    // 비밀번호 암호화
    String encodePassword = passwordEncoder.encode(req.getPassword());

    // trainer_id 검색
    if (!authDAO.existsRegistCode(req.getRegistCode())){
      throw new CustomException(ErrorCode.INVALID_REGIST_CODE);
    }
    long trainerId = authDAO.findTrainerIdByRegistCode(req.getRegistCode());

    // userName 중복 확인
    if (!authDAO.existsUsername(req.getUserName())) {
      throw new CustomException(ErrorCode.USER_USERNAME_DUPLICATE);
    }
    // Email 중복 확인
    if (authDAO.existsEmail(req.getEmail())) {
      throw new CustomException(ErrorCode.USER_EMAIL_DUPLICATE);
    }

    User user = User.builder()
                    .userName(req.getUserName())
                    .name(req.getName())
                    .birth(req.getBirth())
                    .email(req.getEmail())
                    .phone(req.getPhone())
                    .password(encodePassword)
                    .role(Role.USER.name())
                    .isAgree(req.getIsAgree())
                    .sido(req.getSido())
                    .sigungu(req.getSigungu())
                    .roadname(req.getRoadname())
                    .restAddress(req.getRestAddress())
                    .postcode(req.getPostcode())
                    .createdBy(1L)
                    .updatedBy(1L)
                    .build();

    authDAO.insertUser(user);

    authDAO.insertTrainerUser(TrainerUser.builder()
                                  .userId(user.getUserId())
                                  .trainerId(trainerId)
                                  .createdBy(user.getUserId())
                                  .updatedBy(user.getUserId())
                                  .build());

    return AuthJoinResponse.builder()
                           .userId(user.getUserId())
                           .userName(user.getUserName())
                           .email(user.getEmail())
                           .build();
  }

  @Transactional
  public AuthJoinResponse trainerJoin(AuthTrainerJoinRequest req) {

    // 비밀번호 암호화
    String encodePassword = passwordEncoder.encode(req.getPassword());

    // 가입 코드 생성
    String registCode;
    do {
      registCode = generateRegistCode(8);
    } while (authDAO.existsRegistCode((registCode)));


    User user = User.builder()
        .userName(req.getUserName())
        .name(req.getName())
        .birth(req.getBirth())
        .email(req.getEmail())
        .phone(req.getPhone())
        .password(encodePassword)
        .role(Role.TRAINER.name())
        .isAgree(req.getIsAgree())
        .sido(req.getSido())
        .sigungu(req.getSigungu())
        .roadname(req.getRoadname())
        .restAddress(req.getRestAddress())
        .postcode(req.getPostcode())
        .createdBy(1L)
        .updatedBy(1L)
        .build();

    authDAO.insertUser(user);

    TrainerProfile profile = TrainerProfile.builder()
        .trainerId(user.getUserId())
        .careerInfo(req.getCareerInfo())
        .introduce(req.getIntroduce())
        .description(req.getDescription())
        .style(req.getStyle())
        .tag(req.getTag())
        .registCode(registCode)
        .certificationImageUrl(req.getCertificationImageUrl())
        .createdBy(user.getUserId())
        .updatedBy(user.getUserId())
        .build();

    authDAO.insertTrainerProfile(profile);

    return AuthJoinResponse.builder()
        .userId(user.getUserId())
        .userName(user.getUserName())
        .email(user.getEmail())
        .build();
  }

  public void updateRefreshToken(Long userId, String refreshToken) {
    authDAO.updateRefreshToken(userId, refreshToken);
  }

  // TODO: User에 옮겨야 함.
  public User findByUserName(String userName) {
    return authDAO.findByUserName(userName);
  }

  private String generateRegistCode(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder sb = new StringBuilder();

    Random random = new SecureRandom();
    for (int i = 0; i < length; i++) {
      sb.append(chars.charAt(random.nextInt(chars.length())));
    }
    return sb.toString();
  }
}

