package com.mungtrainer.mtserver.counseling.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCounselingRequestDTO {
    private Long dogId;
    private String phone;
}
