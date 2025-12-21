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

        // 같은 강아지로 중복담기 시 에러처리
        boolean exists = wishlistDao.existsCourseInWishlist(userId, request.getDogId(), request.getCourseId());
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

        // wishlistDetail 없으면 에러처리
        if(wishlistDetail == null){
            throw new CustomException(ErrorCode.WISHLIST_NOT_FOUND);
        }
        // 본인소유 장바구니 아닐경우 에러처리
        if (!userWishlistIds.contains(wishlistDetail.getWishlistId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_WISHLIST);
        }
        // 상태가 active가 아니면 에러처리
        if(!wishlistDetail.getStatus().equals("ACTIVE")){
            throw new CustomException(ErrorCode.INVALID_WISHLIST_STATUS);
        }

        // 같은 강아지가 이미 내역에 존재하면 에러처리
        boolean exists = wishlistDao.existsCourseInWishlistExcludeItem(
                userId,
                request.getDogId(),
                wishlistDetail.getCourseId(),
                wishlistItemId
        );
        if (exists) {
            throw new CustomException(ErrorCode.COURSE_DUPLICATE);
        }

        WishlistDetailDog detailDog = wishlistDao.findWishlistDetailDogByItemId(wishlistItemId);
        //detailDog가 없으면 에러처리
        if (detailDog == null) {
            throw new CustomException(ErrorCode.WISHLIST_NOT_FOUND);
        }
        //같은 강아지로 수정요청이 들어오면 return
        if(detailDog.getDogId().equals(request.getDogId())) return;
        // 아니면 수정 완료
        wishlistDao.updateDog(wishlistItemId, request.getDogId());
    }

    //장바구니 삭제
    public void deleteWishlist(Long userId, WishlistDeleteRequest request) {
        List<Long> requestIds = request.getWishlistItemId();
        List<Long> userWishlistIds = wishlistDao.findByUserId(userId);
        List<Long> ids = new ArrayList<>();

        // 권한이 없거나 존재하지 않는 항목의 ID를 수집
        for(Long id : requestIds){
            WishlistDetail wishlistDetail = wishlistDao.findWishlistDetailByItemId(id);
            if(wishlistDetail == null || !userWishlistIds.contains(wishlistDetail.getWishlistId())) {
                ids.add(id);
            }
        }
        // ids가 비지않았다면 에러처리
        if(!ids.isEmpty()){
            throw new CustomException(ErrorCode.UNAUTHORIZED_WISHLIST);
        }

        // detailDog -> detail 순으로 지우기
        wishlistDao.deleteWishlistItemDog(requestIds);
        wishlistDao.deleteWishlistItem(requestIds);
    }
}
