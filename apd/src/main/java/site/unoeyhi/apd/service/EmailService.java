package site.unoeyhi.apd.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.EmailVerification;
import site.unoeyhi.apd.repository.EmailVerificationRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    // ✅ 랜덤 인증 코드 생성
    public String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000)); // 6자리 숫자 코드 생성
    }

    // ✅ 이메일 인증 요청 (이메일 기준 저장)
    @Transactional
    public String sendVerificationEmail(String email) {
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

        // 이메일 전송 시도
        try {
            sendEmail(email, verificationCode);
            log.info("✅ 이메일 전송 성공 - 받는 사람: {}", email);
            return verificationCode;
        } catch (MessagingException e) {
            log.error("❌ 이메일 전송 실패 - 받는 사람: {}, 오류: {}", email, e.getMessage(), e);
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }

    // ✅ 이메일 전송 (예외 처리 추가)
    private void sendEmail(String toEmail, String verificationCode) throws MessagingException {
        log.info("📩 이메일 전송 시작 - 받는 사람: {}, 인증 코드: {}", toEmail, verificationCode);

        if (mailSender == null) {
            log.error("❌ JavaMailSender가 주입되지 않았습니다.");
            throw new RuntimeException("JavaMailSender가 주입되지 않았습니다!");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("회원가입 이메일 인증 코드");
            helper.setText("<h2>이메일 인증 코드</h2>" +
                    "<p>아래 인증 코드를 입력하세요:</p>" +
                    "<h3>" + verificationCode + "</h3>", true);

            mailSender.send(message);
            log.info("✅ 이메일 전송 완료 - 받는 사람: {}", toEmail);
        } catch (MessagingException e) {
            log.error("❌ 이메일 전송 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ✅ 이메일 인증 코드 검증 (토큰 확인)
    public boolean verifyEmail(String token) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findByVerificationToken(token);

        if (verificationOpt.isPresent()) {
            EmailVerification verification = verificationOpt.get();

            if (verification.getExpiredAt().isAfter(LocalDateTime.now())) {
                emailVerificationRepository.delete(verification); // 인증 완료 후 코드 삭제
                log.info("✅ 이메일 인증 성공 - 인증 코드: {}", token);
                return true;
            } else {
                log.warn("⚠ 인증 코드 만료 - 코드: {}", token);
            }
        } else {
            log.warn("⚠ 유효하지 않은 인증 코드 - 코드: {}", token);
        }
        return false;
    }
}
