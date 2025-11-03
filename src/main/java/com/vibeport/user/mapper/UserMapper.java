package com.vibeport.user.mapper;

import com.vibeport.user.vo.RatingVo;
import com.vibeport.user.vo.UserProfileVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    UserProfileVo selectUserProfile(String userId);

    RatingVo selectUserRatings(String userId);
}
