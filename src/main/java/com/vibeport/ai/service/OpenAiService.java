package com.vibeport.ai.service;

import com.vibeport.ai.mapper.AiMapper;
import com.vibeport.ai.vo.ConcertVo;
import com.vibeport.mail.MailSMTP;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final AiMapper aiMapper;
    private final MailSMTP mailSMTP;
    private final ChatModel chatModel;

    public void fetchAndNotifyNewConcerts() {

        // openAi의 API를 통해서 업데이트 된 콘서트 정보를 가져온다.
        List<ConcertVo> resultList = this.getConcerInfos();

        System.out.println(resultList);

        if(!resultList.isEmpty()) {
            // 새로 추가된 콘서트 정보 DB에 저장
            this.saveConcertInfos(resultList);

            // 새로 추가된 공연의 아티스트 설명
            String artistInfo = this.getArtistInfo(resultList.getFirst().getArtistNm());

            // 아티스트 정보 메일 발송
            this.sendArtistInfoMail(artistInfo);
        }
    }

    private List<ConcertVo> getConcerInfos() {
        List<ConcertVo> resultList = new ArrayList<>();

        StringBuffer sysSb = new StringBuffer();
        sysSb.append("답변은 가수/공연일자 및 시간/공연장소/예매처/예매시간의 형식으로 알려주고 아직 정해지지 않았을 경우 미정으로 알려줘.");
        sysSb.append("답변의 시간이 걸려도 괜찮으니까 정확하게 대답해줘.");
        sysSb.append("다른 것은 첨언 할 필요 없이 위의 얘기한 형식으로만 답해줘.");
        sysSb.append("응답의 key는 가수, 공연 일자 두개야.");
        sysSb.append("응답이 2개 이상일 때는 줄바꿈으로 구분해줘.");
        sysSb.append("응답의 정렬은 가수의 인기 순으로 해줘.");
        SystemPromptTemplate promptTemplate = new SystemPromptTemplate(sysSb.toString());
        Message sysMessage = promptTemplate.createMessage();

        StringBuffer userSb = new StringBuffer();
        userSb.append("2026년 1월에 내한이 확정된 국적이 한국 이외의 가수 목록과 공연 일자, 장소, 예매처를 알려줘.");

        PromptTemplate userPromptTamplate = new PromptTemplate(userSb.toString());
        Message userMessage = userPromptTamplate.createMessage();

        Prompt prompt = new Prompt(List.of(sysMessage, userMessage));
        Generation answer = chatModel.call(prompt).getResult();
        String result = answer.getOutput().getText();

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
        userSb.append("사진, 영상, 이모티콘이 포함되어 있지 않은 글자로만 응답해.");
        PromptTemplate userPromptTamplate = new PromptTemplate(userSb.toString());
        Message userMessage = userPromptTamplate.createMessage();

        Prompt prompt = new Prompt(List.of(sysMessage, userMessage));
        Generation answer = chatModel.call(prompt).getResult();
        return answer.getOutput().getText();
    }

    private void sendArtistInfoMail(String artistInfo) {
        this.mailSMTP.sendArtistInfoMail(artistInfo);
    }
}
