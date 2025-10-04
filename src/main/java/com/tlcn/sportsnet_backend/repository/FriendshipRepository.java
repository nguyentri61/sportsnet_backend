package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Friendship;
import com.tlcn.sportsnet_backend.enums.FriendStatusEnum;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :a AND f.receiver = :b) OR (f.requester = :b AND f.receiver = :a)")
    Optional<Friendship> findBetween(@Param("a") Account a, @Param("b") Account b);

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :account OR f.receiver = :account) " +
            "AND f.status = 'ACCEPTED'")
    List<Friendship> findAllFriends(@Param("account") Account account);

    List<Friendship> findAllFriendsByReceiverAndStatus(Account receiver, FriendStatusEnum status, Sort sort);

}
