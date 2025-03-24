package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.entity.ChatMessage;
import site.unoeyhi.apd.entity.ChatRoom;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.UsedProduct;
import site.unoeyhi.apd.service.ChatMessageService;
import site.unoeyhi.apd.service.ChatRoomService;
import site.unoeyhi.apd.service.MemberService;
import site.unoeyhi.apd.service.UsedProductService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final MemberService memberService;
    private final UsedProductService usedProductService;

    // ✅ 채팅방 생성 or 기존 반환
    @PostMapping("/room")
    public ResponseEntity<ChatRoom> createRoom(
            @RequestParam Long buyerId,
            @RequestParam Long sellerId,
            @RequestParam Integer productId
    ) {
        Member buyer = memberService.getById(buyerId);
        Member seller = memberService.getById(sellerId);
      
        UsedProduct product = usedProductService.findById(productId);

        ChatRoom room = chatRoomService.createOrGetRoom(buyer, seller, product);
        return ResponseEntity.ok(room);
    }

    // ✅ 내가 참여한 채팅방 목록
    @GetMapping("/rooms/me")
    public ResponseEntity<List<ChatRoom>> myRooms(@RequestParam Long memberId) {
        Member member = memberService.getById(memberId);
        List<ChatRoom> rooms = chatRoomService.getChatRoomsForMember(member);
        return ResponseEntity.ok(rooms);
    }

    // ✅ 특정 채팅방의 메시지 목록
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long roomId) {
        ChatRoom room = chatRoomService.getRoomById(roomId);
        List<ChatMessage> messages = chatMessageService.getMessagesByRoom(room);
        return ResponseEntity.ok(messages);
    }

    // ✅ 메시지 전송
    @PostMapping("/room/{roomId}/message")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable Long roomId,
            @RequestParam Long senderId,
            @RequestParam String message
    ) {
        ChatRoom room = chatRoomService.getRoomById(roomId);
        Member sender = memberService.getById(senderId);

        ChatMessage saved = chatMessageService.saveMessage(room, sender, message);
        return ResponseEntity.ok(saved);
    }
}
