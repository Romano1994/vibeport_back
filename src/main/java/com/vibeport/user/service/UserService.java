package com.vibeport.user.service;

import com.vibeport.mail.MailSMTP;
import com.vibeport.user.mapper.UserMapper;
import com.vibeport.user.vo.RatingVo;
import com.vibeport.user.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    private final MailSMTP mailSMTP;

    public UserVo getUserProfile(String userId) {
        // 사용자 프로필 정보를 조회하는 로직 구현
        return this.userMapper.selectUserProfile(userId);
    }

    public RatingVo getUserRatings(String userId) {
        // 사용자 평점 정보를 조회하는 로직 구현
        return this.userMapper.selectUserRatings(userId);
    }

    /*
     * 인증 코드 전송
     */
    public void sendVerificationCode(UserVo userVo) {

        String email = userVo.getEmail();

        if(email == null || email.isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수 항목입니다.");
        }

        // 존재하는 이메일인지 확인
        boolean isExistEmail = this.userMapper.checkEmailExists(email);

        if(isExistEmail) {
            // TODO: BuisinessException으로 변경 필요
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 인증 코드 생성
        String verificationCode = this.generateVerificationCode();

        // 인증코드 이메일 전송
        Map<String, String> resultMap = this.mailSMTP.sendVerificationEmail(email, verificationCode);

         //인증 코드 저장
        this.userMapper.insertEmailVerificationCodes(resultMap);

        System.out.println("verificationCode==========" + verificationCode);
    }

    /*
     * 인증 코드 생성
     */
    private String generateVerificationCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        int codeLength = 6;
        SecureRandom random = new SecureRandom();

        StringBuilder code = new StringBuilder(codeLength);

        for(int i=0; i < codeLength; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }

        return code.toString();
    }

    /*
     * 입력한 코드와 발송한 인증 코드 확인
     */
    public void verifyCode(Map<String, Object> param) {
        // 이메일, 입력한 코드로 올바른 인증 코드인지 확인
        boolean isRightCode = this.userMapper.selectIsRightCode(param);

        if(!isRightCode) {
            // TODO: BuisinessException으로 변경 필요
            throw new IllegalArgumentException("인증 코드가 올바르지 않습니다.");
        }
    }
}
