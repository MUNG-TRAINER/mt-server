package com.mungtrainer.mtserver.order.dao;

import com.mungtrainer.mtserver.order.dto.response.WishlistResponse;
import com.mungtrainer.mtserver.order.entity.Wishlist;
import com.mungtrainer.mtserver.order.entity.WishlistDetail;
import com.mungtrainer.mtserver.order.entity.WishlistDetailDog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WishlistDAO {

    // 장바구니 리스트 조회 (Response용)
    List<WishlistResponse> findWishlistResponsesByUserId(@Param("userId") Long userId);

    // 활성 Wishlist 조회
    List<Long> findActiveWishlistByUserId(@Param("userId") Long userId);

    Integer findCoursePriceById(Long courseId);
    // Wishlist insert
    int insertWishlist(Wishlist wishlist);

    // WishlistDetail insert
    int insertWishListDetail(WishlistDetail detail);

    // WishlistDetailDog insert
    int insertWishListDetailDog(WishlistDetailDog detailDog);

    // userId + courseId + dogId로 이미 존재하는지 확인
    boolean existsCourseInWishlist(@Param("userId") Long userId,@Param("dogId") Long dogId,@Param("courseId") Long courseId);
    // 수정하려는 강아지와 같은 강아지가 이미 내역에 존재하는지 여부
    boolean existsCourseInWishlistExcludeItem(@Param("userId") Long userId,@Param("dogId") Long dogId,@Param("courseId") Long courseId, @Param("wishlistItemId") Long  wishlistItemId);

    // userId로 wishlistId 리스트 가져오기
    List<Long> findByUserId(@Param("userId") Long userId);

    // wishlistItemId로 WishlistDetail 가져오기
    WishlistDetail findWishlistDetailByItemId(@Param("wishlistItemId") Long wishlistItemId);

    // wishlistItemId로 WishlistDetailDog 가져오기
    WishlistDetailDog findWishlistDetailDogByItemId(@Param("wishlistItemId") Long wishlistItemId);

    // 장바구니 강아지 수정
    void updateDog(@Param("wishlistItemId") Long wishlistItemId, @Param("dogId") Long dogId);

    // 장바구니 상태 업데이트 (삭제 시 ABANDONED)
    void updateStatus(@Param("wishlistItemId") Long wishlistItemId, @Param("status") String status);

    // 장바구니 상세 강아지 삭제 (여러 아이템)
    void deleteWishlistItemDog(@Param("wishlistItemIds") List<Long> wishlistItemIds);

    // 장바구니 상세 삭제 (여러 아이템)
    void deleteWishlistItem(@Param("wishlistItemIds") List<Long> wishlistItemIds);

    // 반려견 ID로 위시리스트 상세 하드 삭제
    int deleteByDogId(@Param("dogId") Long dogId);
}
