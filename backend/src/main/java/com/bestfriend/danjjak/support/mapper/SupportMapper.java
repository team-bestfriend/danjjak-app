package com.bestfriend.danjjak.support.mapper;

import com.bestfriend.danjjak.support.model.GuardianContactRecord;
import com.bestfriend.danjjak.support.model.NotificationAnomalyRecord;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SupportMapper {

    boolean hasGuardianShareConsent(long userId);

    GuardianContactRecord findGuardian(long userId);

    int upsertGuardian(
            @Param("userId") long userId, @Param("phoneNumber") String phoneNumber);

    NotificationAnomalyRecord findHighAnomaly(
            @Param("userId") long userId, @Param("anomalyEventId") long anomalyEventId);

    int markGuardianNotified(
            @Param("userId") long userId,
            @Param("anomalyEventId") long anomalyEventId,
            @Param("notifiedAt") LocalDateTime notifiedAt);
}
