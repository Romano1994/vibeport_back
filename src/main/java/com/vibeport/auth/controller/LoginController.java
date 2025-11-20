package com.vibeport.auth.controller;


import com.vibeport.auth.service.LoginService;
import com.vibeport.user.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/vibeport/")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("login")
    public void login(HttpServletRequest request, HttpServletResponse response, @RequestBody UserVo userVo) throws Exception{
        Map<String, String> resultMap = this.loginService.login(userVo);

        // Authorization 헤더에 Bearer 접두어 추가
        response.setHeader("Authorization", "Bearer " + resultMap.get("access"));

        // ResponseCookie -> Set-Cookie 헤더로 추가 (ResponseCookie는 Cookie 객체가 아니므로 addCookie가 아님)
        response.addHeader("Set-Cookie", resultMap.get("refresh"));
    }
}
