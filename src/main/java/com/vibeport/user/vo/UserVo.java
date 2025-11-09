package com.vibeport.user.vo;

import lombok.Data;

@Data
public class UserVo {
    String userNo;
    String email;
    String userName;
    String password;
    String adminYn;
    String lockYn;
    String lockDate;
    String regDate;
}
