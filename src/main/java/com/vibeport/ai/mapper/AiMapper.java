package com.vibeport.ai.mapper;

import com.vibeport.ai.vo.ConcertInfoVo;
import com.vibeport.ai.vo.ArtistMsgVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AiMapper {
    void mergeConcertInfo(ConcertInfoVo data);

    void insertMailLog(Map<String, Object> paramMap);

    void insertArtistMsg(ArtistMsgVo artistMsgVo);

    List<ConcertInfoVo> selectSavedList(Map<String, Object> paramMap);

    List<String> selectEmailList();
}
