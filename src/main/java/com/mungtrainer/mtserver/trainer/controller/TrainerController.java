package com.mungtrainer.mtserver.trainer.controller;

import com.mungtrainer.mtserver.trainer.dto.request.TrainerProfileUpdateRequest;
import com.mungtrainer.mtserver.trainer.dto.response.TrainerResponse;
import com.mungtrainer.mtserver.trainer.service.TrainerService;
import com.mungtrainer.mtserver.user.entity.User;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class TrainerController {
    private final TrainerService trainerService;

    //프로필 조회
    @GetMapping("/{trainerId}")
    public ResponseEntity<TrainerResponse> getTrainerProfileById(@PathVariable Long trainerId){
        TrainerResponse profile = trainerService.getTrainerProfileById(trainerId);
        return ResponseEntity.ok(profile);
    }

    //프로필 수정
    @PatchMapping("/me")
    public ResponseEntity<TrainerResponse> updateTrainerProfile(@RequestBody TrainerProfileUpdateRequest request, @RequestAttribute("user")User user){
        TrainerResponse profile = trainerService.updateTrainerProfile(request, user);
        return ResponseEntity.ok(profile);
    }
}