package com.vibeport.ai.client;

import com.google.genai.Client;
import com.google.genai.types.*;
import com.vibeport.ai.vo.ArtistMsgVo;
import com.vibeport.ai.vo.ConcertInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GeminiClient {

    private final Client client;
    private final Tool googleSearchTool;

    @Value("${serpapi.api-key}")
    private String serpApiKey;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    private static final String SAVE_DIRECTORY = "./src/main/resources/concert_images/";

    public GeminiClient() {
        this.client = Client.builder()
                .build();

        this.googleSearchTool = Tool.builder()
                .googleSearch(GoogleSearch.builder().build())
                .build();
    }

    public List<ConcertInfoVo> getConcertInfos(int year, int month) throws Exception{
        List<ConcertInfoVo> resultList;

        String systemPrompt = """
            ### 역할: SYSTEM
            당신은 내한 콘서트 전문 데이터 분석가입니다.
            아래 규칙을 절대적으로 준수하여 결과를 제공합니다.
            당신은 추론은 사용하지 않고 검색 내용을 정리하기만 합니다.
            
            참고 사이트: 나무위키의 2026년 내한
           
            규칙:
            0) **반드시 정확한 정보만 답변**
            1) 답변 형식은 반드시 다음 순서만 사용:
               아티스트 한글명 / 아티스트 외국명 / 콘서트일자 및 시간 / 콘서트장소 / 예매처 / 예매시간 / 인기도 점수
            2) 아직 정해지지 않은 정보는 '미정'으로 표기
            3) 위 형식에 포함되지 않는 다른 설명, 해석, 불필요한 문장은 절대 포함하지 않기
            4) 응답의 key는 '아티스트', '공연 일자' 두 개만 사용 - 같은 아티스트더라도 공연 일자가 다르면 다른 행으로 표기
            5) 여러 결과가 있을 경우 줄바꿈으로 구분
            6) 아티스트의 인기 순으로 정렬
            7) 발표된 일정이 없으면 '-' 만 출력
            8) 유명하지 않은 가수의 내한 정보도 출력
            9) 가수명은 가급적 한글, 영문 병행 표기
            10) 콘서트장소, 예매처 등의 나머지 항목들을 한글 표기
            11) 응답 이외의 문자([1][2][3], ** 등)는 답변에 포함하지 않음
            12) 인기도를 0 ~ 100으로 정해줘
            13) 공연 날짜는 알지만 공연 시간을 특정할 수 없을 경우 00:00으로 표기
       
            규칙:
            - 출력은 반드시 텍스트만 사용하며 어떤 추가 문장도 포함하지 않는다.
            - 출력 예시는 다음과 같다:
              아티스트 한글명 / 아티스트 외국명 / 2026-01-10 19:00 / 장소 / 예매처 / 예매시간 / 인기도 점수
       \s""";

        Content systemInstruction = Content.builder()
                .parts(List.of(Part.builder().text(systemPrompt).build()))
                .build();

        GenerateContentConfig config = GenerateContentConfig.builder()
                .tools(List.of(googleSearchTool))
                .systemInstruction(systemInstruction)
                .temperature(0.0f)
                .build();

        String userPrompt = "### 역할 : USER\n" +
                year + "년 " + month + "월에 내한이 확정된 가수와 콘서트 정보를 알려줘.";

        log.info(year + "=============" + month);
        GenerateContentResponse response = client.models.generateContent(
                "gemini-3-pro-preview",
                Content.builder().parts(List.of(Part.builder().text(userPrompt).build())).build(),
                config
        );

        log.info("response==========" + response.text());

        resultList = this.concertInfoReprocess(response.text());
        return resultList;
    }

    private List<ConcertInfoVo> concertInfoReprocess(String answer) throws Exception {
        List<ConcertInfoVo> resultList = new ArrayList<>();

        if (answer == null || answer.isEmpty()) {
            return resultList;
        }

        String[] arrResult = answer.split("\n");

        for (String data : arrResult) {
            if (data == null) continue;
            data = data.trim();
            if (data.isEmpty()) continue;

            String[] tmp = data.split("/");

            // 최소한 아티스트와 날짜/시간은 있어야 함
            if (tmp.length < 2) continue;

            ConcertInfoVo vo = new ConcertInfoVo();

            // 0: 아티스트명
            vo.setArtistNmKor(tmp[0].trim());
            vo.setArtistNmFor(tmp[1].trim());

            // 1: 공연일시 yyyy-MM-dd HH:mm
            String dateTimeStr = tmp[2].trim();
            try {
                java.time.LocalDateTime dt = java.time.LocalDateTime.parse(
                        dateTimeStr,
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                );
                vo.setConcertYear(dt.getYear());
                vo.setConcertMonth(dt.getMonthValue());
                vo.setConcertDate(dt.getDayOfMonth());
                vo.setConcertTime(String.format("%02d%02d", dt.getHour(), dt.getMinute()));
            } catch (Exception e) {
                log.error(dateTimeStr + "====날짜 파싱 실패");
            }

            // 2: 공연장소
            if (tmp.length > 3) {
                vo.setVenue(tmp[3].trim());
            }

            // 3: 예매처
            if (tmp.length > 4) {
                vo.setTctSite(tmp[4].trim());
            }

            // 4: 예매시간
            if (tmp.length > 5) {
                vo.setTctOpenAt(tmp[5].trim());
            }

            // 5: 인기도 점수 (0~100 사이 INT 예상)
            if (tmp.length > 6) {
                try {
                    int score = Integer.parseInt(tmp[6].trim());
                    vo.setPopScore(score);
                } catch (NumberFormatException ignore) {
                    // 파싱 실패 시 기본값(0.0) 유지
                }
            }

            vo.setEmailYn("N");

            resultList.add(vo);
        }

        return resultList;
    }

    public ArtistMsgVo getArtistInfo(ConcertInfoVo concertInfoVo) {
        String artistNm = concertInfoVo.getArtistNmKor() + " (" + concertInfoVo.getArtistNmFor() + ")";

        StringBuilder sysSb = new StringBuilder();
        sysSb.append("너는 재치있는 20대 음악 지식에 해박한 음악 평론가야. 그리고 대중들이 알기 쉽게 아티스트와 공연에 대한 설명을 뉴스레터로 전달할거야.");
        sysSb.append("뉴스레터의 제목을 뽑고 'subject-'라고 붙여줘");
        sysSb.append("이모티콘을 넣을 땐 마침표를 생략해.");
        sysSb.append("출처 표시 [1][2]...는 하지마.");
        sysSb.append("뉴스레터는 안녕하세요 여러분! VIBEPORT입니다!로 시작해.");
        sysSb.append("답변은 존댓말로 해.");
        sysSb.append("최종적으로 답변이 몇 자인지는 안 알려줘도 돼.");
        sysSb.append("이모티콘은 한 주제가 끝 날때, 노래 추천할 때 제목에만 사용하고 '**'는 시용하지마.");
        sysSb.append("한 주제 내에서 문장의 끝 마다 줄 바꿈은 하지마.");
        sysSb.append("내용이 html을 통해서 표현 될 수 있도록 줄바꿈은 <br/><br/>으로 표현해.");
        sysSb.append("내용은 세 단락으로 나눠서 첫번째 단락에서는 가수 소개, 두번째에서는 공연 소개, 세번째에서는 해당 가수의 추천곡으로 구성해.");
        sysSb.append("추천곡을 얘기 할 때는 한 곡을 얘기하고 줄 바꿈해.");

        StringBuilder userSb = new StringBuilder();
        userSb.append("가수 ").append(artistNm).append("와 새로 예정된 공연에 대해서 1,000글자 이내로 소개하고 3개의 대표곡, 뽑은 대표곡들에 대한 설명도 덧 붙여줘.");

        Content systemInstruction = Content.builder()
                .parts(List.of(Part.builder().text(sysSb.toString()).build()))
                .build();

        GenerateContentConfig config = GenerateContentConfig.builder()
                .tools(List.of(googleSearchTool))
                .systemInstruction(systemInstruction)
                .temperature(0.0f)
                .build();

        GenerateContentResponse response = client.models.generateContent(
                "gemini-3-pro-preview",
                Content.builder().parts(List.of(Part.builder().text(userSb.toString()).build())).build(),
                config
        );

        log.info(response.text());

        // 답변을 제목과 본문으로 재가공
        ArtistMsgVo artistMsgVo = this.artistMsgProcess(response.text());
        artistMsgVo.setArtistNmKor(concertInfoVo.getArtistNmKor());
        artistMsgVo.setConcertYear(concertInfoVo.getConcertYear());
        artistMsgVo.setConcertMonth(concertInfoVo.getConcertMonth());
        artistMsgVo.setConcertDate(concertInfoVo.getConcertDate());
        return artistMsgVo;
    }

    private ArtistMsgVo artistMsgProcess(String answer) {
        ArtistMsgVo artistMsgVo = new ArtistMsgVo();

        String subject = "";
        String content = "";
        int subjectIdx = answer.indexOf("subject-");
        if (subjectIdx != -1) {
            int lineEndIdx = answer.indexOf('\n', subjectIdx);
            if (lineEndIdx == -1) {
                lineEndIdx = answer.length();
            }

            // 'subject-' 이후부터 줄 끝까지를 제목으로 사용 (접두어는 제거)
            int titleStart = subjectIdx + "subject-".length();
            if (titleStart < lineEndIdx) {
                subject = answer.substring(titleStart, lineEndIdx).trim();
            }

            // 줄바꿈 뒤부터 끝까지를 본문으로 사용
            if (lineEndIdx < answer.length()) {
                content = answer.substring(lineEndIdx + 1).trim();
            }

            artistMsgVo.setSubject(subject);
            artistMsgVo.setContent(content);
        }

        return artistMsgVo;
    }

    public void getArtistPicture(ArtistMsgVo artistMsgVo) throws Exception {
        String artistNmKor = artistMsgVo.getArtistNmKor();

        String imgUrl = getImageUrl(artistNmKor);

        if (imgUrl != null && imgUrl.startsWith("http")) {
            // 2. 이미지 다운로드 및 저장
            String fileName = artistNmKor + "_" + System.currentTimeMillis() + ".jpg";
            saveImageFromServer(imgUrl, fileName);
            artistMsgVo.setArtistImageUrl(buildPublicImageUrl("/concert_images/" + fileName));
            System.out.println("저장 완료: " + SAVE_DIRECTORY + fileName);
        } else {
            System.out.println("이미지 URL을 찾지 못했습니다.");
        }
    }

    private String getImageUrl(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String safeApiKey = (serpApiKey != null) ? serpApiKey.trim() : "";

        String urlString = String.format(
                "https://serpapi.com/search.json?q=%s&tbm=isch&api_key=%s&num=1",
                encodedQuery, safeApiKey
        );

        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            try (InputStream es = conn.getErrorStream();
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(es))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                log.error("SerpAPI Error (Code {}): {}", responseCode, errorResponse);
                return null;
            }
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);

            JSONObject jsonResponse = new JSONObject(response.toString());
            if (jsonResponse.has("images_results")) {
                return jsonResponse.getJSONArray("images_results").getJSONObject(0).getString("original");
            }
        }
        return null;
    }

    // URL의 이미지를 서버 파일로 저장
    public void saveImageFromServer(String imageUrl, String fileName) throws IOException {
        // 저장할 폴더가 없으면 생성
        File dir = new File(SAVE_DIRECTORY);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + SAVE_DIRECTORY);
        }

        URL url = URI.create(imageUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        try (InputStream in = connection.getInputStream()) {
            // Files.copy를 이용해 입력 스트림을 파일로 저장
            Files.copy(in, Paths.get(SAVE_DIRECTORY + fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String buildPublicImageUrl(String path) {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            return path;
        }
        String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        String cleanPath = path.startsWith("/") ? path : "/" + path;
        return base + cleanPath;
    }
}
