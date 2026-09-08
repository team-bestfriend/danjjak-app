package com.bestfriend.danjjak.user.mapper;

import com.bestfriend.danjjak.user.model.UserSettingsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    UserSettingsRecord findCurrentUser(long userId);

    UserSettingsRecord findByKakaoUserId(long kakaoUserId);

    UserSettingsRecord findFirstUnlinkedUser();

    int bindKakaoUserId(
            @Param("userId") long userId, @Param("kakaoUserId") long kakaoUserId);

    int updateConsents(
            @Param("userId") long userId,
            @Param("usageLogAgreed") boolean usageLogAgreed,
            @Param("guardianShareAgreed") boolean guardianShareAgreed);

    int updateSettings(
            @Param("userId") long userId,
            @Param("fontSize") String fontSize,
            @Param("voiceSpeed") String voiceSpeed,
            @Param("guideVoiceType") String guideVoiceType);
}
