package site.unoeyhi.apd.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberProfileDto {
    private Long memberId;
    private String email;
    private String name;
    private String nickname;
    private String phoneNumber;
    private String address;
    private String detailAddress;
    private String role;
    private String status;
    private String authType;
}
