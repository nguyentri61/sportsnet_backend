package com.tlcn.sportsnet_backend.util;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.enums.ClubStatusEnum;
import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ClubSpecification {
    public static Specification<Club> hasVisibilityAndStatus(ClubVisibilityEnum visibility, ClubStatusEnum status) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("visibility"), visibility),
                cb.equal(root.get("status"), status)
        );
    }

    public static Specification<Club> matchesProvince(String province) {
        return (root, query, cb) -> {
            if (province == null || province.isBlank()) return null;
            return cb.like(cb.lower(root.get("location")), "%" + province.toLowerCase() + "%");
        };
    }

    public static Specification<Club> matchesWard(String ward) {
        return (root, query, cb) -> {
            if (ward == null || ward.isBlank()) return null;
            return cb.like(cb.lower(root.get("location")), "%" + ward.toLowerCase() + "%");
        };
    }

    public static Specification<Club> matchesLevels(List<String> levels) {
        return (root, query, cb) -> {
            if (levels == null || levels.isEmpty()) return null;

            List<Predicate> predicates = new ArrayList<>();

            for (String level : levels) {
                double levelMin, levelMax;

                switch (level) {
                    case "Mới tập chơi":
                        levelMin = 0.0; levelMax = 1.5; break;
                    case "Cơ bản":
                        levelMin = 1.0; levelMax = 2.5; break;
                    case "Trung bình":
                        levelMin = 2.0; levelMax = 3.5; break;
                    case "Trung bình khá":
                        levelMin = 3.0; levelMax = 4.0; break;
                    case "Khá":
                        levelMin = 3.5; levelMax = 4.5; break;
                    case "Bán chuyên":
                        levelMin = 4.0; levelMax = 5.0; break;
                    default:
                        continue;
                }

                predicates.add(cb.and(
                        cb.lessThanOrEqualTo(root.get("minLevel"), levelMin),
                        cb.greaterThanOrEqualTo(root.get("maxLevel"), levelMax)
                ));
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Club> matchesClubNames(List<String> clubNames) {
        return (root, query, cb) -> {
            if (clubNames == null || clubNames.isEmpty()) return null;
            return root.get("name").in(clubNames);
        };
    }

    public static Specification<Club> notJoinedBy(Account account) {
        return (root, query, cb) -> {
            if (account == null) return null;

            // Subquery để tìm các club mà user đã tham gia
            Subquery<String> subquery = query.subquery(String.class);
            Root<Club> subRoot = subquery.from(Club.class);
            Join<Object, Object> subMembers = subRoot.join("members");
            subquery.select(subRoot.get("id"))
                    .where(cb.equal(subMembers.get("account").get("id"), account.getId()));

            // Loại bỏ các club có id nằm trong danh sách đó
            return cb.not(root.get("id").in(subquery));
        };
    }

    public static Specification<Club> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) return null;
            String likePattern = "%" + search.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("location")), likePattern)
            );
        };
    }
}
