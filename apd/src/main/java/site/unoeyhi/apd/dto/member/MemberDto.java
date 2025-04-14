package site.unoeyhi.apd.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MemberDto {
    private Long memberId;
    private String email;
    private String role;
}
