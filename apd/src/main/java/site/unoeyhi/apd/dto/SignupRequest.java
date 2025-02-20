package site.unoeyhi.apd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 6, message = "비밀번호는 최소 6자리 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phoneNumber;

    @NotBlank(message = "주소를 입력해주세요.")
    private String address;

    @NotBlank(message = "상세 주소를 입력해주세요.")
    private String detailAddress;
}
