package com.vibeport.ai.mapper;

import com.vibeport.ai.vo.ConcertVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiMapper {
    void insertConcertInfo(ConcertVo data);
}
