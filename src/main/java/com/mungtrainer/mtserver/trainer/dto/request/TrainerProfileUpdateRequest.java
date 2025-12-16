package com.mungtrainer.mtserver.trainer.dto.request;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrainerProfileUpdateRequest {
  // User 영역
  private String name;
  private String phone;
  private String profileImage;

  // TrainerProfile 영역
  private String careerInfo;
  private String introduce;
  private String description;
  private String style;
  private String tag;
  private String certificationImageUrl;
}
