package com.mungtrainer.mtserver.order.dao;

import com.mungtrainer.mtserver.order.dto.response.WishlistResponse;
import com.mungtrainer.mtserver.order.entity.Wishlist;
import com.mungtrainer.mtserver.order.entity.WishlistDetail;
import com.mungtrainer.mtserver.order.entity.WishlistDetailDog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WishlistDao {
    // response 용
    List<WishlistResponse> findWishlistResponsesByUserId(@Param("userId") Long userId);

    // 장바구니 추가
    int insertWishList(WishlistDetail wishlistDetail);

    // userId로 wishlistId 가져오기
    List<Long> findByUserId(@Param("userId")Long userId);

    // wishlistItemId로 wishlistId 가져오기
    WishlistDetail findWishlistIdByItemId(@Param("wishlistItemId")Long wishlistItemId);
    //  wishlistItemId로 detail_dog entity가져오기
    WishlistDetailDog findByWishlistDetailDog(@Param("wishlistItemId")long wishlistItemId);

    //장바구니 수정
    void updateDog(@Param("wishlistItemId") Long wishlistItemId,@Param("dogId") Long dogId);

    // 장바구니 삭제
    void updateStatus(@Param("wishlistItemId") Long wishlistItemId,@Param("status") String status);

}
