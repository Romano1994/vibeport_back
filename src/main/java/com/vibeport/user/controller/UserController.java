package com.vibeport.user.controller;

import com.vibeport.user.service.UserService;
import com.vibeport.user.vo.RatingVo;
import com.vibeport.user.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/vibeport/user/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("getUserProfile")
    public UserVo getUserProfile(HttpServletRequest request, HttpServletResponse response) throws Exception{
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

    /*
     * 인증 코드 전송
     */
    @PostMapping("sendVerificationCode")
    public ResponseEntity sendVerificationCode(@RequestBody UserVo userVo) throws Exception{
        // TODO : 추후 실행 정도에 따라 다른 쓰레드에서 실행되도록 변경 필요
        // 인증 코드 전송 로직
        this.userService.sendVerificationCode(userVo);

        return ResponseEntity.ok().build();
    }

    /*
     * 인증 코드 확인
     */
    @PostMapping("verifyCode")
    public ResponseEntity verifyCode(@RequestBody Map<String, Object> param) throws Exception{
        // 인증 코드 확인
        this.userService.verifyCode(param);

        return ResponseEntity.ok().build();
    }
}
