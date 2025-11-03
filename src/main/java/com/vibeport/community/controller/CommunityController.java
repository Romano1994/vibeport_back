package com.vibeport.community.controller;

import com.vibeport.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/vibeport/community/")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("posts")
    public List<Map<String, Object>> posts(Map<String, Object> params) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        return resultList;
    }

    @PostMapping("newPost")
    public void newPost(Map<String, Object> params) {
        // 새로운 글 등록
        this.communityService.newPost(params);
    }


}
