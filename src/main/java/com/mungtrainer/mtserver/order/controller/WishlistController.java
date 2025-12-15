package com.mungtrainer.mtserver.order.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.order.dto.request.WishlistCreateRequest;
import com.mungtrainer.mtserver.order.dto.request.WishlistDeleteRequest;
import com.mungtrainer.mtserver.order.dto.request.WishlistUpdateRequest;
import com.mungtrainer.mtserver.order.dto.response.WishlistResponse;
import com.mungtrainer.mtserver.order.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // 전체 위시리스트 조회
    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getAllWishlist( @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        List<WishlistResponse> wishlist = wishlistService.getWishLists(userId);
        return ResponseEntity.ok(wishlist);
    }

    // 위시리스트 생성
    @PostMapping
    public ResponseEntity<Void> addWishlist(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody WishlistCreateRequest request) {
        Long userId = userDetails.getUserId();
        wishlistService.addWishlist(request, userId); // 서비스에 userId 전달
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 위시리스트 삭제 (여러 개)
    @DeleteMapping
    public ResponseEntity<Void> deleteWishlist( @AuthenticationPrincipal CustomUserDetails userDetails,@RequestBody WishlistDeleteRequest request) {
        Long userId = userDetails.getUserId();
        wishlistService.deleteWishlist(userId, request); // 서비스 메서드와 맞춤
        return ResponseEntity.ok().build();
    }

    // 위시리스트 수정 (강아지 변경 등)
    @PatchMapping("/{wishlistItemId}")
    public ResponseEntity<Void> updateWishlist(
            @PathVariable Long wishlistItemId,
            @RequestBody WishlistUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        wishlistService.updateWishlist(userId, wishlistItemId, request);
        return ResponseEntity.ok().build();
    }
}
