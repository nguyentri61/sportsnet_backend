package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.member_note.ClubMemberNoteRequest;
import com.tlcn.sportsnet_backend.dto.member_note.ClubMemberNoteResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.entity.ClubMember;
import com.tlcn.sportsnet_backend.entity.ClubMemberNote;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ClubMemberNoteRepository;
import com.tlcn.sportsnet_backend.repository.ClubMemberRepository;
import com.tlcn.sportsnet_backend.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubMemberNoteService {
    private final ClubMemberRepository clubMemberRepository;
    private final ClubMemberNoteRepository clubMemberNoteRepository;
    private final AccountRepository accountRepository;
    private final ClubRepository clubRepository;

    public List<ClubMemberNoteResponse> getAllClubMemberNotes(String clubId, String accountId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy CLB"));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));

        ClubMember clubMember = clubMemberRepository.findByClubAndAccount(club, account);

        return clubMemberNoteRepository.findAllByClubMemberOrderByCreatedAtDesc(clubMember)
                .stream()
                .map(note -> ClubMemberNoteResponse.builder()
                        .id(note.getId())
                        .comment(note.getComment())
                        .createdAt(note.getCreatedAt())
                        .updatedAt(note.getUpdatedAt())
                        .createdBy(note.getCreatedBy())
                        .updatedBy(note.getUpdatedBy())
                        .build()
                )
                .toList();
    }

    public ClubMemberNoteResponse createClubMemberNote(ClubMemberNoteRequest request) {
        Club club = clubRepository.findById(request.getClubId())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy CLB"));

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));

        ClubMember clubMember = clubMemberRepository.findByClubAndAccount(club, account);

        ClubMemberNote note = ClubMemberNote.builder()
                .clubMember(clubMember)
                .comment(request.getComment())
                .build();

        note = clubMemberNoteRepository.save(note);

        return ClubMemberNoteResponse.builder()
                .id(note.getId())
                .comment(note.getComment())
                .createdAt(note.getCreatedAt())
                .createdBy(note.getCreatedBy())
                .build();
    }
}
