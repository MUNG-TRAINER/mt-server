package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrainerUserListResponseDto {
    private Long userId;
    private String name;
    private String phone;
    private String email;
}
