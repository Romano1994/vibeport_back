package com.vibeport.user.mapper;

import com.vibeport.user.vo.RatingVo;
import com.vibeport.user.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    UserVo selectUserProfile(String userId);

    RatingVo selectUserRatings(String userId);

    /*
     * 이메일 존재 여부 확인
     */
    boolean checkEmailExists(String email);
}
