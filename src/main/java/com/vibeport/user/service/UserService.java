package com.vibeport.user.service;

import com.vibeport.user.mapper.UserMapper;
import com.vibeport.user.vo.RatingVo;
import com.vibeport.user.vo.UserProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper UserMapper;

    public UserProfileVo getUserProfile(String userId) {
        // 사용자 프로필 정보를 조회하는 로직 구현
        return this.UserMapper.selectUserProfile(userId);
    }

    public RatingVo getUserRatings(String userId) {
        // 사용자 평점 정보를 조회하는 로직 구현
        return this.UserMapper.selectUserRatings(userId);
    }
}
