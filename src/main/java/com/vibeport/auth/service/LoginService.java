package com.vibeport.auth.service;

import com.vibeport.auth.mapper.LoginMapper;
import com.vibeport.auth.utils.JwtUtil;
import com.vibeport.user.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginMapper loginMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, String> login(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        Map<String, String> param = Map.of("email", email
                , "password", passwordEncoder.encode(password));

        // DB에서 사용자 정보 조회
        UserVo user = loginMapper.findByEmail(email);

        if(ObjectUtils.isEmpty(user)) {
            // TODO - business exception 처리
            throw new RuntimeException("가입되지 않은 이메일입니다.");
        }

        if(passwordEncoder.matches(password, user.getPassword())) {
            // TODO - business exception 처리
            throw new RuntimeException("비밀번호를 잘못 입력하셨습니다.");
        }

        if(user.getLockYn().equals("Y")) {
            // TODO - business exception 처리
            throw new RuntimeException("사용할 수 없는 계정입니다.");
        }

        String role = user.getAdminYn().equals("Y") ? "Admin" : "User";

        Map<String, String> tokens = Map.of(
                "access", this.jwtUtil.createToken("access", role),
                "refresh", this.jwtUtil.createToken("refresh", role)
        );

        return tokens;
    }
}
