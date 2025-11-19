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

@RestController
@RequestMapping(value = "/vibeport/")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("login")
    public void login(HttpServletRequest request, HttpServletResponse response, @RequestBody UserVo userVo) throws Exception{
        this.loginService.login(userVo);
    }
}
