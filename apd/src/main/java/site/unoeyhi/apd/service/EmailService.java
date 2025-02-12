package site.unoeyhi.apd.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.EmailVerification;
import site.unoeyhi.apd.repository.EmailVerificationRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    // 랜덤 인증 코드 생성
    public String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000)); // 6자리 숫자 코드 생성
    }

    // ✅ 이메일 인증 요청 (이메일 기준 저장)
    @Transactional
    public String sendVerificationEmail(String email) throws MessagingException {
        String verificationCode = generateVerificationCode();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(10); // 10분 후 만료

        // 기존 인증 요청 삭제 후 새로 저장
        emailVerificationRepository.findByEmail(email)
                .ifPresent(emailVerificationRepository::delete);

        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setEmail(email); // ✅ 이메일 저장
        emailVerification.setVerificationToken(verificationCode);
        emailVerification.setExpiredAt(expiredAt);
        emailVerificationRepository.save(emailVerification);

        // 이메일 전송
        sendEmail(email, verificationCode);

        return verificationCode;
    }

    // 이메일 전송
    private void sendEmail(String toEmail, String verificationCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("회원가입 이메일 인증 코드");
        helper.setText(
                "<h2>이메일 인증 코드</h2>" +
                "<p>아래 인증 코드를 입력하세요:</p>" +
                "<h3>" + verificationCode + "</h3>", true);

        mailSender.send(message);
    }

    // ✅ 이메일 인증 코드 검증 (이메일 없이 토큰으로 확인)
    public boolean verifyEmail(String token) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findByVerificationToken(token);

        if (verificationOpt.isPresent()) {
            EmailVerification verification = verificationOpt.get();

            if (verification.getExpiredAt().isAfter(LocalDateTime.now())) {
                emailVerificationRepository.delete(verification); // 인증 완료 후 코드 삭제
                return true;
            }
        }
        return false;
    }
}
