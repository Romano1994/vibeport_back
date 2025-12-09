package com.vibeport.mail.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.ses.model.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVo {

    private String from;
    private List<String> to;
    private String subject;
    private String content;

    @Builder
    public EmailVo(String from, List<String> to, String subject, String content) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
    }

    public SendEmailRequest toSendEmailRequest() {
        Destination destination = Destination.builder()
                .toAddresses(this.to)
                .build();

        Message message = Message.builder()
                .subject(this.createContent(this.subject))
                .body(Body.builder().html(this.createContent(this.content)).build())
                .build();

        return SendEmailRequest.builder()
                .source(this.from)
                .destination(destination)
                .message(message)
                .build();
    }

    private Content createContent(String content) {
        return Content.builder()
                .charset("UTF-8")
                .data(content)
                .build();
    }
}
