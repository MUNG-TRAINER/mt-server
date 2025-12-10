package com.mungtrainer.mtserver.trainer.dto.request;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrainerProfileUpdateRequest {

    private String careerInfo;
    private String introduce;
    private String description;
    private String style;
    private String tag;
    private String certificationImageUrl;
}
