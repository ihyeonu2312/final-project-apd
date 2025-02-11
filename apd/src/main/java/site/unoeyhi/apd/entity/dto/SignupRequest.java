package site.unoeyhi.apd.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String nickname;
    private String phoneNumber;
    private String address;
    private String detailAdd;
}
