// package site.unoeyhi.apd;

// import jakarta.mail.internet.MimeMessage;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.mail.javamail.MimeMessageHelper;

// @SpringBootTest
// public class EmailTest {

//     @Autowired
//     private JavaMailSender mailSender;

//     @Test
//     public void testEmailSending() {
//         try {
//             MimeMessage message = mailSender.createMimeMessage();
//             MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

//             helper.setTo("dlgusdn2312@gmail.com");  // ✅ 본인 이메일 주소로 변경
//             helper.setSubject("이메일 테스트");
//             helper.setText("이메일 전송이 정상적으로 작동합니다.", true);

//             mailSender.send(message);
//             System.out.println("✅ 이메일이 성공적으로 전송되었습니다!");
//         } catch (Exception e) {
//             System.err.println("❌ 이메일 전송 실패: " + e.getMessage());
//         }
//     }
// }
