package com.tlcn.sportsnet_backend.util;

import com.tlcn.sportsnet_backend.entity.ClubEvent;
import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;

public class ClubEventSpecification {

    public static Specification<ClubEvent> baseSpec() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("openForOutside")),
                cb.equal(root.get("status"), EventStatusEnum.OPEN),
                cb.greaterThan(root.get("deadline"), LocalDateTime.now())
        );
    }

    public static Specification<ClubEvent> matchesSearch(String search) {
        if (search == null || search.isBlank()) return null;
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%");
    }

    public static Specification<ClubEvent> matchesProvince(String province) {
        return matchesWard(province);
    }

    public static Specification<ClubEvent> matchesWard(String ward) {
        if (ward == null || ward.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("location")), "%" + ward.toLowerCase() + "%");
    }

    public static Specification<ClubEvent> matchesFee(Boolean isFree, BigDecimal minFee, BigDecimal maxFee) {
        return (root, query, cb) -> {
            if (Boolean.TRUE.equals(isFree)) {
                return cb.equal(root.get("fee"), BigDecimal.ZERO);
            } else if (minFee != null && maxFee != null) {
                return cb.between(root.get("fee"), minFee, maxFee);
            } else if (minFee != null) {
                return cb.greaterThanOrEqualTo(root.get("fee"), minFee);
            } else if (maxFee != null) {
                return cb.lessThanOrEqualTo(root.get("fee"), maxFee);
            }
            return null;
        };
    }

    public static Specification<ClubEvent> matchesDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) return null;
        return (root, query, cb) -> {
            if (start != null && end != null)
                return cb.between(root.get("startTime"), start, end);
            if (start != null)
                return cb.greaterThanOrEqualTo(root.get("startTime"), start);
            else
                return cb.lessThanOrEqualTo(root.get("startTime"), end);
        };
    }


    public static Specification<ClubEvent> quickTime(String quickTimeFilter) {
        return (root, query, cb) -> {
            if (quickTimeFilter == null || quickTimeFilter.isEmpty()) return null;
            LocalDateTime now = LocalDateTime.now();
            Expression<LocalDateTime> eventStart = root.get("startTime");
            Expression<LocalDateTime> eventDeadline = root.get("deadline");

            switch (quickTimeFilter) {
                case "urgent":
                    return cb.lessThan(eventDeadline, now.plusHours(24));
                case "today":
                    LocalDate today = now.toLocalDate();
                    return cb.and(
                            cb.greaterThanOrEqualTo(eventStart, today.atStartOfDay()),
                            cb.lessThan(eventStart, today.plusDays(1).atStartOfDay())
                    );
                case "weekend":
                    // SQL không hỗ trợ dễ dàng weekday check, nên lọc tại Java sau query hoặc bỏ nếu cần hiệu năng
                    return null;
                case "week":
                    LocalDate startOfWeek = now.toLocalDate().with(DayOfWeek.MONDAY);
                    LocalDate endOfWeek = startOfWeek.plusDays(6);
                    return cb.between(eventStart, startOfWeek.atStartOfDay(), endOfWeek.plusDays(1).atStartOfDay());
                default:
                    return null;
            }
        };
    }

    public static Specification<ClubEvent> hasStatuses(List<EventStatusEnum> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return null;
            return root.get("status").in(statuses);
        };
    }

    public static Specification<ClubEvent> hasClubNames(List<String> clubNames) {
        return (root, query, cb) -> {
            if (clubNames == null || clubNames.isEmpty()) return null;
            Join<Object, Object> clubJoin = root.join("club", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();
            for (String name : clubNames) {
                predicates.add(cb.like(cb.lower(clubJoin.get("name")), "%" + name.toLowerCase() + "%"));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<ClubEvent> hasCategories(List<BadmintonCategoryEnum> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) return null;
            Join<Object, Object> catJoin = root.join("categories", JoinType.LEFT);
            return catJoin.in(categories);
        };
    }

    public static Specification<ClubEvent> hasLevels(List<String> levels) {
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

                // Bao trọn: event.min <= levelMin && event.max >= levelMax
                predicates.add(cb.and(
                        cb.lessThanOrEqualTo(root.get("minLevel"), levelMin),
                        cb.greaterThanOrEqualTo(root.get("maxLevel"), levelMax)
                ));
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<ClubEvent> minRating(Double minRating) {
        return (root, query, cb) -> {
            if (minRating == null) return null;
            assert query != null;
            query.distinct(true);
            Join<Object, Object> ratingJoin = root.join("clubEventRatings", JoinType.LEFT);
            query.groupBy(root.get("id"));
            return cb.greaterThanOrEqualTo(cb.avg(ratingJoin.get("rating")), minRating);
        };
    }

    public static Specification<ClubEvent> participantSize(String size) {
        return (root, query, cb) -> {
            if (size == null || size.isEmpty()) return null;
            assert query != null;
            query.distinct(true);
            Join<Object, Object> participants = root.join("participants", JoinType.LEFT);
            query.groupBy(root.get("id"));
            Expression<Long> count = cb.count(participants);
            switch (size) {
                case "NHO": return cb.lessThan(count, 10L);
                case "VUA": return cb.between(count, 10L, 20L);
                case "DONG": return cb.greaterThan(count, 20L);
                default: return null;
            }
        };
    }
}
