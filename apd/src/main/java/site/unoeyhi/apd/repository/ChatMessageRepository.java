package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.ChatMessage;
import site.unoeyhi.apd.entity.ChatRoom;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅방 내 메시지 전체 (보낸 시간 순 정렬)
    List<ChatMessage> findByChatRoomOrderBySentAtAsc(ChatRoom chatRoom);
}
