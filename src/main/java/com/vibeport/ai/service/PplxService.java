package com.vibeport.ai.service;

import com.vibeport.ai.mapper.AiMapper;
import com.vibeport.ai.vo.ConcertVo;
import com.vibeport.mail.MailSMTP;
import com.vibeport.mail.service.TestEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PplxService {

    private final OpenAiChatModel chatModel;
    private final AiMapper aiMapper;
    private final TestEmailService emailService;

    public void fetchAndNotifyNewConcerts() throws Exception{

        // openAi의 API를 통해서 업데이트 된 콘서트 정보를 가져온다.
//        List<ConcertVo> resultList = this.getConcerInfos();

//        System.out.println(resultList);
//
//        if(!resultList.isEmpty()) {
//            // 새로 추가된 콘서트 정보 DB에 저장
//            this.saveConcertInfos(resultList);
//
//            // 새로 추가된 공연의 아티스트 설명
//            String artistInfo = this.getArtistInfo(resultList.getFirst().getArtistNm());
//
//            System.out.println(artistInfo);
//            // 아티스트 정보 메일 발송
//            this.sendArtistInfoMail(artistInfo);
//        }
        String artistInfo = "123";
        this.sendArtistInfoMail(artistInfo);
    }

    private List<ConcertVo> getConcerInfos() throws Exception{
        List<ConcertVo> resultList = new ArrayList<>();

        String systemPrompt = """
            ### 역할: SYSTEM
            당신은 내한 공연 전문 데이터 분석가입니다.
            아래 규칙을 절대적으로 준수하여 결과를 제공합니다.
           \n
            규칙:
            1) 답변 형식은 반드시 다음 순서만 사용:
               가수 / 공연일자 및 시간 / 공연장소 / 예매처 / 예매시간
            2) 아직 정해지지 않은 정보는 '미정'으로 표기
            3) 다른 설명, 해석, 불필요한 문장은 절대 포함하지 않기
            4) 응답의 key는 '가수', '공연 일자' 두 개만 사용
            5) 여러 결과가 있을 경우 줄바꿈으로 구분
            6) 가수의 인기 순으로 정렬
            7) 아무 내한 일정이 없으면 '-' 만 출력
            8) 유명하지 않은 가수의 내한 정보도 출력
            9) 가수명은 가급적 한글, 영문 병행 표기
            10) 공연장소, 예매처 등의 나머지 항목들을 한글 표기
       \n
            규칙:
            - 출력은 반드시 텍스트만 사용하며 어떤 추가 문장도 포함하지 않는다.
            - 출력 예시는 다음과 같다:
              가수명 / 2026-01-10 19:00 / 장소 / 예매처 / 예매시간
             \s
              예시:
              Weeknd / 2025-12-20 18:00 / Tokyo Dome / Ticketmaster / 2025-10-01 12:00
       \s""";
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
        Message systemMessage = systemPromptTemplate.createMessage();

        String userPrompt = """
            ### 역할 : USER
            2026년 1월에 내한이 확정된 국적이 한국 이외의 가수 목록과\s
            공연 일자, 장소, 예매처를 알려줘.
       \s""";
        PromptTemplate userPromptTamplate = new PromptTemplate(userPrompt);
        Message userMessage = userPromptTamplate.createMessage();

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        Generation result = chatModel.call(prompt).getResult();
        String answer = result.getOutput().getText();

        if(answer != null && answer.equals("-")) {
            return resultList;
        } else {
            answer = java.util.Arrays.stream(answer.split("\\R"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.joining("\n"));
        }

        // AI 응답 재가공
        resultList = this.answerReprocess(answer);
        return resultList;
    }

    private List<ConcertVo> answerReprocess(String answer) throws Exception {
        List<ConcertVo> resultList = new ArrayList<>();

        String[] arrResult = answer.split("\n");

        for(String data : arrResult) {
            String[] tmp  = data.split("/");
            ConcertVo tmpVo = new ConcertVo();

            Field[] field = ConcertVo.class.getDeclaredFields();
            for(int i = 0; i < field.length; i++) {
                String element = tmp[i];
                if(element == null || element.isEmpty() || "미정".equals(element)) {
                    continue;
                }

                Field f = field[i];
                f.setAccessible(true);
                f.set(tmpVo, element);
            }

            resultList.add(tmpVo);
        }

        return resultList;
    }

    private void saveConcertInfos(List<ConcertVo> concertVoList) {
        concertVoList.stream().filter(data -> {
            return !data.getArtistNm().isEmpty();
        }).forEach(this.aiMapper::insertConcertInfo);
    }

    private String getArtistInfo(String artistNm) {
        StringBuffer sysSb = new StringBuffer();
        sysSb.append("너는 음악 지식에 해박한 음악 평론가야. 그리고 대중들이 알기 쉽게 아티스트에 대해서 설명해주면서 긍정적인 이야기만 해.");
        SystemPromptTemplate promptTemplate = new SystemPromptTemplate(sysSb.toString());
        Message sysMessage = promptTemplate.createMessage();

        StringBuffer userSb = new StringBuffer();
        userSb.append("가수 " + artistNm + "에 대해서 1,000글자 이내로 소개하고 3개의 대표곡, 뽑은 대표곡들에 대한 설명도 덧 붙여줘.");
        userSb.append("마침표를 찍을 때 마다 행을 바꿔줘.");
        userSb.append("출처 표시, 사진, 영상, 이모티콘이 포함되어 있지 않은 글자로만 응답해.");
        PromptTemplate userPromptTamplate = new PromptTemplate(userSb.toString());
        Message userMessage = userPromptTamplate.createMessage();

        Prompt prompt = new Prompt(List.of(sysMessage, userMessage));
        Generation answer = chatModel.call(prompt).getResult();
        return answer.getOutput().getText();
    }

    private void sendArtistInfoMail(String artistInfo) {
        this.emailService.emailVerifSend(Arrays.asList("vibeport.user@gmail.com"));
    }
}
