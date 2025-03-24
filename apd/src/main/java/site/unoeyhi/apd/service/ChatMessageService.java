package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.ChatMessage;
import site.unoeyhi.apd.entity.ChatRoom;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.repository.ChatMessageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    // ✅ 메시지 저장
    public ChatMessage saveMessage(ChatRoom room, Member sender, String content) {
        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .message(content)
                .isRead(false)
                .build();

        return chatMessageRepository.save(message);
    }

    // ✅ 채팅방의 모든 메시지 조회 (시간순)
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesByRoom(ChatRoom room) {
        return chatMessageRepository.findByChatRoomOrderBySentAtAsc(room);
    }

    // ✅ (옵션) 메시지 읽음 처리
    public void markAllAsRead(ChatRoom room, Member reader) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatRoomOrderBySentAtAsc(room)
                .stream()
                .filter(msg -> !msg.getSender().equals(reader) && !msg.isRead())
                .toList();

        unreadMessages.forEach(msg -> msg.setRead(true));
        chatMessageRepository.saveAll(unreadMessages);
    }
}
