package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.ChatRoom;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.UsedProduct;

import java.util.Optional;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 이미 존재하는 채팅방 조회 (중복 방지용)
    Optional<ChatRoom> findByBuyerAndSellerAndUsedProduct(Member buyer, Member seller, UsedProduct usedProduct);

    // 특정 회원이 참여한 채팅방 목록
    List<ChatRoom> findByBuyerOrSeller(Member buyer, Member seller);
}
