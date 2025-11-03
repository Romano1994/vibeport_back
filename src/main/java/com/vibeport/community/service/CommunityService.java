package com.vibeport.community.service;

import com.vibeport.community.mapper.CommunityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityMapper communityMapper;

    public void newPost(Map<String, Object> params) {
        // 새로운 글 등록 로직
        this.communityMapper.newpost(params);
    }
}
