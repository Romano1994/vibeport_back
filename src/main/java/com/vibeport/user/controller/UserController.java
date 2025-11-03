package com.vibeport.user.controller;

import com.vibeport.user.service.UserService;
import com.vibeport.user.vo.RatingVo;
import com.vibeport.user.vo.UserProfileVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/vibeport/user/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("getUserProfile")
    public UserProfileVo getUserProfile(HttpServletRequest request, HttpServletResponse response) throws Exception{
        // 유저 세션 체크하는 로직

        // 유저 이메일 추출

        String userId = "exampleUserId"; // 예시로 고정된 사용자 ID 사용
        return this.userService.getUserProfile(userId);
    }

    @GetMapping("getUserRatings")
    public RatingVo getUserRatings(HttpServletRequest request, HttpServletResponse response) throws Exception{
        // 유저 세션 체크하는 로직

        // 유저 이메일 추출

        String userId = "exampleUserId"; // 예시로 고정된 사용자 ID 사용
        return this.userService.getUserRatings(userId);
    }
}
