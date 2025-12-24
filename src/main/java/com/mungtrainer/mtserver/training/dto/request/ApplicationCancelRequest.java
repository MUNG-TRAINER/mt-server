package com.mungtrainer.mtserver.training.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApplicationCancelRequest {
    private List<Long> courseIds;

}
