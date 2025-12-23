package com.mungtrainer.mtserver.dog.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DogsCounselingResponse {
    private Long dogId;
    private String name;
    private String breed;
    private boolean hasCounseling;
}
