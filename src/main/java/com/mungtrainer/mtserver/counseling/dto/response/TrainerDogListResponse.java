package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrainerDogListResponse {
    private Long dogId;
    private String name;
    private String breed;
    private Integer age;
    private String gender;
    private String profileImage;
}
