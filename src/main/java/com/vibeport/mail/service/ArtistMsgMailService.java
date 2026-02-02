package com.vibeport.mail.service;

import com.vibeport.ai.vo.ArtistMsgVo;
import com.vibeport.mail.vo.EmailVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.SesClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArtistMsgMailService {

    @Value("${aws.ses.send-mail-from}")
    private String sender;
    private final SesClient sesClient;
    private final TemplateEngine templateEngine;

    public void artistMsgEmailSend(List<String> toList, ArtistMsgVo letterVo) {
        Context context = new Context();
        context.setVariable("artistInfo", letterVo.getContent());

        String content = templateEngine.process("artistInfo", context);

        EmailVo emailVo = EmailVo.builder()
                .from(sender)
                .to(toList)
                .subject(letterVo.getSubject())
                .content(content)
                .build();

        // 이메일 발송
        sesClient.sendEmail(emailVo.toSendEmailRequest());
    }

    public void sendOnboardingEmail(Map<String, Object> param) {
        Context context = new Context();
        context.setVariable("message",
                "구독해 주셔서 감사합니다! <br/>" +
                        "매일 아침 8시 30분, 당신을 위한 음악 소식이 발송됩니다. <br/>" +
                        "VibePort가 엄선한 내한 예정 아티스트의 소식과 콘서트 정보를 놓치지 마세요.");

        String content = templateEngine.process("onboardingMail", context);
        String recipientEmail = (String) param.get("email");

        EmailVo emailVo = EmailVo.builder()
                .from(sender)
                .to(List.of(recipientEmail))
                .subject("환영합니다! VibePort 뉴스레터 구독이 시작되었습니다.")
                .content(content)
                .build();

        // 이메일 발송
        sesClient.sendEmail(emailVo.toSendEmailRequest());
    }
}
