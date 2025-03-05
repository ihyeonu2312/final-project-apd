package site.unoeyhi.apd.dto; // 패키지는 프로젝트 구조에 맞게 수정

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String name;
    private String email;
    private String nickname;
    private String phoneNumber;
    private String address;
    private String detailAddress;
}
