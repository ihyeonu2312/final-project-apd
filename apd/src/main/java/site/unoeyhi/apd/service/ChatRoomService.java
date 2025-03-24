package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.ChatRoom;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.UsedProduct;
import site.unoeyhi.apd.repository.ChatRoomRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    // ✅ 채팅방 생성 or 기존 반환
    public ChatRoom createOrGetRoom(Member buyer, Member seller, UsedProduct product) {
        return chatRoomRepository
                .findByBuyerAndSellerAndUsedProduct(buyer, seller, product)
                .orElseGet(() -> {
                    ChatRoom room = ChatRoom.builder()
                            .buyer(buyer)
                            .seller(seller)
                            .usedProduct(product)
                            .build();
                    return chatRoomRepository.save(room);
                });
    }

    // ✅ 내가 참여한 채팅방 리스트 조회
    public List<ChatRoom> getChatRoomsForMember(Member member) {
        return chatRoomRepository.findByBuyerOrSeller(member, member);
    }

    // ✅ ID로 채팅방 조회
    public ChatRoom getRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }
}
