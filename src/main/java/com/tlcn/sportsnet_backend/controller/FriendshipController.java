package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.friend.FriendRequest;
import com.tlcn.sportsnet_backend.dto.friend.FriendResponse;
import com.tlcn.sportsnet_backend.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;

    @GetMapping("/relationship/{accountId}")
    public ResponseEntity<?> getRelationShip(@PathVariable String accountId) {
        return ResponseEntity.ok(friendshipService.getRelationship(accountId));
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@RequestBody FriendRequest request) {
        FriendResponse response = friendshipService.sendFriendRequest(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{requesterId}/accept")
    public ResponseEntity<?> acceptFriend(@PathVariable String requesterId) {
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(requesterId));
    }

    @PostMapping("/{requesterId}/reject")
    public ResponseEntity<?> rejectFriend(@PathVariable String requesterId) {
        return ResponseEntity.ok(friendshipService.rejectFriendRequest(requesterId));
    }
}
