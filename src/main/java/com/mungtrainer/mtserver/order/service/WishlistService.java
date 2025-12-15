package com.mungtrainer.mtserver.order.service;

import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.order.dao.WishlistDAO;
import com.mungtrainer.mtserver.order.dto.request.WishlistCreateRequest;
import com.mungtrainer.mtserver.order.dto.request.WishlistDeleteRequest;
import com.mungtrainer.mtserver.order.dto.request.WishlistUpdateRequest;
import com.mungtrainer.mtserver.order.dto.response.WishlistResponse;
import com.mungtrainer.mtserver.order.entity.Wishlist;
import com.mungtrainer.mtserver.order.entity.WishlistDetail;
import com.mungtrainer.mtserver.order.entity.WishlistDetailDog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {
    private final WishlistDAO wishlistDao;

    // 장바구니 리스트 목록 조회
    @Transactional(readOnly = true)
    public List<WishlistResponse> getWishLists(Long userId) {
        return wishlistDao.findWishlistResponsesByUserId(userId);
    }

    // 장바구니 생성
    public void addWishlist(WishlistCreateRequest request, Long userId) {

        // 1. 활성 Wishlist 확인
        List<Long> wishlistIds = wishlistDao.findActiveWishlistByUserId(userId);
        Long wishlistId;

        if (wishlistIds.isEmpty()) {
            // 없으면 새 Wishlist 생성
            Wishlist wishlist = Wishlist.builder()
                    .userId(userId)
                    .status("ACTIVE")
                    .createdBy(userId)
                    .updatedBy(userId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            wishlistDao.insertWishlist(wishlist);
            wishlistId = wishlist.getWishlistId();
        } else {
            wishlistId = wishlistIds.get(0);
        }

        boolean exists = wishlistDao.existsCourseInWishlist(userId, request.getCourseId());
        if (exists) {
            throw new CustomException(ErrorCode.COURSE_DUPLICATE);
        }

        // 2. wishlist_detail 생성
        Integer price = wishlistDao.findCoursePriceById(request.getCourseId());
        if (price == null) {
            throw new CustomException(ErrorCode.COURSE_PRICE_NOT_FOUND);
        }

        WishlistDetail detail = WishlistDetail.builder()
                .wishlistId(wishlistId)
                .courseId(request.getCourseId())
                .price(price)
                .status("ACTIVE")
                .createdBy(userId)
                .updatedBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        wishlistDao.insertWishListDetail(detail);


        // 3. wishlist_detail_dog 생성
        WishlistDetailDog detailDog = WishlistDetailDog.builder()
                .wishlistItemId(detail.getWishlistItemId())
                .dogId(request.getDogId())
                .createdBy(userId)
                .updatedBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        wishlistDao.insertWishListDetailDog(detailDog);
    }

    // 장바구니 강아지 수정
    public void updateWishlist(Long userId, Long wishlistItemId, WishlistUpdateRequest request){
        List<Long> userWishlistIds = wishlistDao.findByUserId(userId);
        WishlistDetail wishlistDetail = wishlistDao.findWishlistDetailByItemId(wishlistItemId);
        if(wishlistDetail == null){
            throw new CustomException(ErrorCode.WISHLIST_NOT_FOUND);
        }
        if (!userWishlistIds.contains(wishlistDetail.getWishlistId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_WISHLIST);
        }
        if(!wishlistDetail.getStatus().equals("ACTIVE")){
            throw new CustomException(ErrorCode.INVALID_WISHLIST_STATUS);
        }

        WishlistDetailDog detailDog = wishlistDao.findWishlistDetailDogByItemId(wishlistItemId);
        if (detailDog == null) {
            throw new CustomException(ErrorCode.WISHLIST_NOT_FOUND);
        }
        if(detailDog.getDogId().equals(request.getDogId())) return;

        wishlistDao.updateDog(wishlistItemId, request.getDogId());
    }

    //장바구니 삭제
    public void deleteWishlist(Long userId, WishlistDeleteRequest request) {
        List<Long> requestIds = request.getWishlistItemId();
        List<Long> userWishlistIds = wishlistDao.findByUserId(userId);
        List<Long> ids = new ArrayList<>();

        for(Long id : requestIds){
            WishlistDetail wishlistDetail = wishlistDao.findWishlistDetailByItemId(id);
            if(wishlistDetail == null || !userWishlistIds.contains(wishlistDetail.getWishlistId())) {
                ids.add(id);
            }
        }
        if(!ids.isEmpty()){
            throw new CustomException(ErrorCode.UNAUTHORIZED_WISHLIST);
        }

        wishlistDao.deleteWishlistItemDog(requestIds);
        wishlistDao.deleteWishlistItem(requestIds);
    }
}
