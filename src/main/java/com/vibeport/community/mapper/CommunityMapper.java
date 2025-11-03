package com.vibeport.community.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface CommunityMapper {
    void newpost(Map<String, Object> params);
}
