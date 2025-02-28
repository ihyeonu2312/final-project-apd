package site.unoeyhi.apd.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDto {
    private String email;  
    private String newPassword; // 새 비밀번호
}
