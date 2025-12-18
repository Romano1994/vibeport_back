package com.vibeport.ai.mapper;

import com.vibeport.ai.vo.ConcertInfoVo;
import com.vibeport.ai.vo.NewsLetterVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AiMapper {
    void mergeConcertInfo(ConcertInfoVo data);

    void insertMailLog(NewsLetterVo letterVo);

    void insertArtistMsg(NewsLetterVo letterVo);

    List<ConcertInfoVo> selectSavedList(Map<String, Object> paramMap);
}
