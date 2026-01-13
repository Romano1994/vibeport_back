package com.vibeport.ai.service;

import com.vibeport.ai.client.GeminiClient;
import com.vibeport.ai.mapper.AiMapper;
import com.vibeport.ai.vo.ConcertInfoVo;
import com.vibeport.ai.vo.ArtistMsgVo;
import com.vibeport.mail.service.ArtistMsgMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final GeminiClient geminiClient;
    private final AiMapper aiMapper;
    private final ArtistMsgMailService emailService;

    public void fetchAndNotifyNewConcerts() throws Exception {

        // 오늘의 년, 월
        LocalDate today = LocalDate.now();
        int beginYear = today.plusMonths(1).getYear();
        int beginMonth = today.plusMonths(1).getMonthValue();
        int endYear = today.plusMonths(2).getYear();
        int endMonth = today.plusMonths(2).getMonthValue();
        int year = 0;
        int month = 0;
        List<ConcertInfoVo> resultList = new ArrayList<>();

        Map<String, Object> paramMap = Map.of("frYear", beginYear, "frMonth", beginMonth,
                "toYear", endYear, "toMonth", endMonth);

        for(int i = 0; i < 1; i++) {
            today = today.plusMonths(1);
            year = today.getYear();
            month = today.getMonthValue();

            // openAi의 API를 통해서 업데이트 된 콘서트 정보를 가져온다.
            resultList.addAll(this.geminiClient.getConcertInfos(year, month));
        }

        log.info("result=========================");
        resultList.forEach(data -> log.info(String.valueOf(data)));

        // 저장된 해당 년월 콘서트 정보 가져옴
        List<ConcertInfoVo> savedConcertList = this.aiMapper.selectSavedList(paramMap);

        // 저장되지 않은 새로운 콘서트 정보
        List<ConcertInfoVo> newConcertList = this.getNewConcertList(resultList, savedConcertList);
        log.info("newConcertList=========================");
        newConcertList.forEach(data -> log.info(String.valueOf(data)));

        if(!newConcertList.isEmpty()) {
            // 새로 추가된 콘서트 정보 DB에 저장
            this.saveConcertInfos(newConcertList);

            String artistNm = newConcertList.getFirst().getArtistNmKor() + " (" + newConcertList.getFirst().getArtistNmFor() + ")";
            // 새로 추가된 공연의 아티스트 설명
            ArtistMsgVo artistMsgVo = this.geminiClient.getArtistInfo(artistNm);

            log.info(String.valueOf(artistMsgVo));

            // 아티스트 설명 DB저장
            this.aiMapper.insertArtistMsg(artistMsgVo);

            // 아티스트 정보 메일 발송
            this.sendArtistInfoMail(artistMsgVo);
        }
    }

    private List<ConcertInfoVo> getNewConcertList(List<ConcertInfoVo> resultList, List<ConcertInfoVo> savedConcertList) {
        List<ConcertInfoVo> newConcertList = new ArrayList<>();

        if (!resultList.isEmpty()) {
            Set<String> savedKeySet = new HashSet<>();
            for (ConcertInfoVo savedVo : savedConcertList) {
                savedKeySet.add(buildConcertKey(savedVo));
            }

            List<ConcertInfoVo> filteredList = new ArrayList<>();
            for (ConcertInfoVo vo : resultList) {
                if (!savedKeySet.contains(buildConcertKey(vo))) {
                    filteredList.add(vo);
                }
            }

            newConcertList = filteredList.stream()
                    .sorted(Comparator.comparing(ConcertInfoVo::getPopScore).reversed())
                    .toList();
        }

        return newConcertList;
    }

    private String buildConcertKey(ConcertInfoVo vo) {
        if (vo == null) {
            return "";
        }

        String artistKor = vo.getArtistNmKor() == null ? "" : vo.getArtistNmKor().trim();
        String artistFor = vo.getArtistNmFor() == null ? "" : vo.getArtistNmFor().trim();
        String year = Objects.toString(vo.getConcertYear(), "0");
        String month = Objects.toString(vo.getConcertMonth(), "0");
        String date = Objects.toString(vo.getConcertDate(), "0");

        return String.join("|", artistKor, artistFor, year, month, date);
    }

    @Transactional
    private void saveConcertInfos(List<ConcertInfoVo> concertVoList) {
        concertVoList.stream()
                .filter(data -> data.getArtistNmKor() != null && !data.getArtistNmKor().isEmpty())
                .filter(data -> data.getArtistNmFor() != null && !data.getArtistNmFor().isEmpty())
                .forEach(this.aiMapper::mergeConcertInfo);
    }

    @Transactional
    private void sendArtistInfoMail(ArtistMsgVo artistMsgVo) {
        // 이메일 발송 목록 조회
        List<String> emailList = this.aiMapper.selectEmailList();

        // 이메일 발송
        this.emailService.artistMsgEmailSend(emailList, artistMsgVo);

        // 메일 로그 저장
        emailList.forEach(data -> {
            Map<String, Object> paramMap = Map.of("email", data
            , "subject", artistMsgVo.getSubject());

            this.aiMapper.insertMailLog(paramMap);
        });
    }
}
