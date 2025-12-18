package com.vibeport.ai.service;

import com.google.genai.Client;
import com.google.genai.types.*;
import com.vibeport.ai.vo.ConcertInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GeminiService {

    @Value("${spring.ai.gemini.api-key}")
    private String apiKey;

    public void fetchAndNotifyNewConcerts() throws Exception {

        // openAi의 API를 통해서 업데이트 된 콘서트 정보를 가져온다.
        List<ConcertInfoVo> resultList = this.getConcerInfos();

        if(!resultList.isEmpty()) {
            // 새로 추가된 콘서트 정보 DB에 저장
//            this.saveConcertInfos(resultList);
//
//            // 새로 추가된 공연의 아티스트 설명
//            String artistInfo = this.getArtistInfo(resultList.getFirst().getArtistNm());
//
//            // 아티스트 정보 메일 발송
//            this.sendArtistInfoMail(artistInfo);
        }
    }

    private List<ConcertInfoVo> getConcerInfos() {
        List<ConcertInfoVo> resultList = new ArrayList<>();

        Client client = Client.builder()
                .apiKey(System.getenv(apiKey))
                .build();

        String systemRules = """
                당신은 내한 공연 전문 데이터 분석가입니다.
                아래 규칙을 절대적으로 준수하여 결과를 제공합니다.

                규칙:
                1) 답변 형식은 반드시 다음 순서만 사용:
                   가수 / 공연일자 및 시간 / 공연장소 / 예매처 / 예매시간
                2) 아직 정해지지 않은 정보는 '미정'으로 표기
                3) 다른 설명, 해석, 불필요한 문장은 절대 포함하지 않기
                4) 응답의 key는 '가수', '공연 일자' 두 개만 사용
                5) 여러 결과가 있을 경우 줄바꿈으로 구분
                6) 가수의 인기 순으로 정렬
                7) 아무 내한 일정이 없으면 '-' 만 출력

                규칙:
                - 출력은 반드시 텍스트만 사용하며 어떤 추가 문장도 포함하지 않는다.
                - 출력 예시는 다음과 같다:
                  가수명 / 2026-01-10 19:00 / 장소 / 예매처 / 예매시간
                  
                  예시:
                  Weeknd / 2025-12-20 18:00 / Tokyo Dome / Ticketmaster / 2025-10-01 12:00
                """;

        Content systemInstruction = Content.builder()
                .parts(List.of(Part.builder().text(systemRules).build()))
                .build();

        Tool googleSearchTool = Tool.builder()
                .googleSearch(GoogleSearch.builder().build())
                .build();

        GenerateContentConfig config = GenerateContentConfig.builder()
                .tools(List.of(googleSearchTool))
                .systemInstruction(systemInstruction)
                .temperature(0.0f)
                .build();

        String userPrompt = """
            ### 역할 : USER
            2026년 1월에 내한이 확정된 국적이 한국 이외의 가수 목록과\s
            공연 일자, 장소, 예매처를 알려줘.
        \s""";

        GenerateContentResponse response = client.models.generateContent(
                "gemini-3-pro-preview",
                Content.builder().parts(List.of(Part.builder().text(userPrompt).build())).build(),
                config
        );

        System.out.println(response.text());

        return resultList;
    }
}
