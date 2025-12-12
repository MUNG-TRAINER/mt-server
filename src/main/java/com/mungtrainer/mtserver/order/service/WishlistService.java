package com.mungtrainer.mtserver.order.service;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.order.dao.WishlistDao;
import com.mungtrainer.mtserver.order.dto.request.WishlistCreateRequest;
import com.mungtrainer.mtserver.order.dto.request.WishlistUpdateRequest;
import com.mungtrainer.mtserver.order.dto.response.WishlistResponse;
import com.mungtrainer.mtserver.order.entity.Wishlist;
import com.mungtrainer.mtserver.order.entity.WishlistDetail;
import com.mungtrainer.mtserver.order.entity.WishlistDetailDog;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistDao wishlistDao;

    // 장바구니 리스트 목록 조회
    public List<WishlistResponse> getWishLists(Long userId) {
        return wishlistDao.findWishlistResponsesByUserId(userId);
    }

    // 장바구니 생성
    public int addWishlist(WishlistCreateRequest request){
        WishlistDetail detail = WishlistDetail.builder()
                .wishlistId(request.getWishlistId())
                .courseId(request.getCourseId())
                .status("ACTIVE")
                .build();

        return wishlistDao.insertWishList(detail);
    }

    // 장바구니 강아지 수정
    public void updateWishlist(Long userId,Long wishlistItemId, WishlistUpdateRequest request){
        // 유저 소유 장바구니인지 확인
        List<Long> userWishlistIds = wishlistDao.findByUserId(userId);
        WishlistDetail wishlistDetail = wishlistDao.findWishlistIdByItemId(wishlistItemId);
        // 해당 내역이 존재하는지 확인
        if(wishlistDetail == null){
            throw new RuntimeException("존재하지 않는 내역입니다.");
        }
        // 사용자 소유의 장바구니만 수정 가능
        if (!userWishlistIds.contains(wishlistDetail.getWishlistId())) {
            throw new RuntimeException("사용자 소유의 장바구니 아이템이 아닙니다.");
        }

       // status가 active인 경우만 가능
       if(!wishlistDetail.getStatus().equals("ACTIVE")){
           throw new RuntimeException("수정 불가능한 내역입니다.");
       }

        // dogId 검증
//        if(!userDogService.isUserDog(userId, request.getDogId()))
//            throw new RuntimeException("사용자 소유의 반려견이 아닙니다.");

// 이미 같은 dogId면 업데이트 안함
        WishlistDetailDog detailDog = wishlistDao.findByWishlistDetailDog(wishlistItemId);
        if(detailDog.getDogId().equals(request.getDogId())) return;

       // 아닐경우 수정 가능
        wishlistDao.updateDog(wishlistItemId, request.getDogId());
    }

    //장바구니 삭제
    public void deleteWishlist(Long userId,Long wishlistItemId,WishlistUpdateRequest request){
        // 유저 소유 장바구니인지 확인
        List<Long> userWishlistIds = wishlistDao.findByUserId(userId);
        WishlistDetail wishlistDetail = wishlistDao.findWishlistIdByItemId(wishlistItemId);
        // 해당 내역이 존재하는지 확인
        if(wishlistDetail == null){
            throw new RuntimeException("존재하지 않는 내역입니다.");
        }
        // 사용자 소유의 장바구니만 수정 가능
        if (!userWishlistIds.contains(wishlistDetail.getWishlistId())) {
            throw new RuntimeException("사용자 소유의 장바구니 아이템이 아닙니다.");
        }

        // status가 active인 경우만 가능
        if(!wishlistDetail.getStatus().equals("ACTIVE")){
            throw new RuntimeException("삭제 불가능한 내역입니다.");
        }
        wishlistDao.updateStatus(wishlistItemId, wishlistDetail.getStatus());
    }
}
