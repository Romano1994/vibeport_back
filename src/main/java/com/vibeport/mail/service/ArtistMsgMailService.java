package com.vibeport.mail.service;

import com.vibeport.ai.vo.NewsLetterVo;
import com.vibeport.mail.vo.EmailVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.SesClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistMsgMailService {

    @Value("${aws.ses.send-mail-from}")
    private String sender;
    private final SesClient sesClient;
    private final TemplateEngine templateEngine;

    public void artistMsgEmailSend(List<String> toList, NewsLetterVo letterVo) {
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
}
