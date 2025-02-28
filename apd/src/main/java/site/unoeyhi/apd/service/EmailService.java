package site.unoeyhi.apd.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.EmailVerification;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.EmailVerification.EmailVerificationStatus;
import site.unoeyhi.apd.entity.Member.MemberStatus;
import site.unoeyhi.apd.repository.EmailVerificationRepository;
import site.unoeyhi.apd.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor  // ✅ Lombok이 자동으로 생성자 생성 (직접 생성자 필요 없음)
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;


    // ✅ 랜덤 인증 코드 생성
    public String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000)); // 6자리 숫자 코드 생성
    }

        
        // ✅ 이메일 존재 여부 확인 (가입된 이메일인지 체크)
    public boolean checkEmailExists(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }


    @Scheduled(fixedRate = 3600000) // 1시간마다 이메일인증테이블 데이터 정리 실행 (3600000ms = 1시간)
    @Transactional
    public void deleteExpiredEmailVerifications() {
        int deletedCount = emailVerificationRepository.deleteByExpiredAtBefore(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("🗑 삭제된 만료된 이메일 인증 데이터: {}개", deletedCount);
        }
    }


    // ✅ 이메일 인증 요청 (이메일 기준 저장)
    @Transactional
    public String sendVerificationEmail(String email) {
        String verificationCode = generateVerificationCode();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(3);
    
        // ✅ 기존 인증 정보 조회
        Optional<EmailVerification> existingOpt = emailVerificationRepository.findByEmail(email);
    
        if (existingOpt.isPresent()) {
            // ✅ 기존 데이터가 있으면 업데이트
            EmailVerification existing = existingOpt.get();
            existing.setVerificationToken(verificationCode);
            existing.setExpiredAt(expiredAt);
            existing.setStatus(EmailVerificationStatus.PENDING);
            emailVerificationRepository.save(existing); // ✅ 업데이트 후 저장
            log.info("🔄 기존 인증 정보 업데이트 - 이메일: {}", email);
        } else {
            // ✅ 기존 데이터가 없으면 새로 생성
            EmailVerification emailVerification = new EmailVerification();
            emailVerification.setEmail(email);
            emailVerification.setVerificationToken(verificationCode);
            emailVerification.setExpiredAt(expiredAt);
            emailVerification.setStatus(EmailVerificationStatus.PENDING);
            emailVerificationRepository.save(emailVerification);
            log.info("🆕 새로운 인증 정보 생성 - 이메일: {}", email);
        }
    
        // ✅ 이메일 전송
        try {
            sendEmail(email, verificationCode);
            log.info("✅ 이메일 전송 성공 - 받는 사람: {}", email);
            return verificationCode;
        } catch (MessagingException e) {
            log.error("❌ 이메일 전송 실패 - 받는 사람: {}, 오류: {}", email, e.getMessage(), e);
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }
    

    private void sendEmail(String toEmail, String verificationCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    
        helper.setTo(toEmail);
        helper.setSubject("알팡당 이메일 인증 코드");
        helper.setText("<h2>이메일 인증 코드</h2>" +
                "<p>아래 인증 코드를 입력하세요:</p>" +
                "<h3>" + verificationCode + "</h3>", true);
    
        mailSender.send(message);
    }
    

   //  ✅ 이메일 인증 코드 검증 (토큰 확인)
    public boolean verifyEmail(String token) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findByVerificationToken(token);

        if (verificationOpt.isPresent()) {
            EmailVerification verification = verificationOpt.get();

            if (verification.getExpiredAt().isBefore(LocalDateTime.now())) {
                verification.setStatus(EmailVerificationStatus.EXPIRED); // ✅ 만료 상태 변경
                emailVerificationRepository.save(verification); // ✅ DB 업데이트
                log.warn("⚠ 인증 코드 만료 - 코드: {}", token);
                return false; // ❌ 만료된 코드
            }

            // ✅ 인증 성공
            verification.setStatus(EmailVerificationStatus.VERIFIED);
            emailVerificationRepository.save(verification);
            log.info("✅ 이메일 인증 성공 - 인증 코드: {}", token);
            return true;
        } else {
            log.warn("⚠ 유효하지 않은 인증 코드 - 코드: {}", token);
        }
        return false;
    }

    
}
