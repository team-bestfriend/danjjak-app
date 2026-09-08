package com.bestfriend.danjjak.pattern.mapper;

import com.bestfriend.danjjak.pattern.model.GuidanceRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GuidanceMapper {
    List<GuidanceRecord> findAll(@Param("userId") long userId, @Param("patternId") long patternId);

    int updateText(@Param("userId") long userId, @Param("patternId") long patternId,
            @Param("target") String target, @Param("text") String text, @Param("voiceMode") String voiceMode);

    int updateAudio(@Param("userId") long userId, @Param("patternId") long patternId,
            @Param("target") String target, @Param("filePath") String filePath,
            @Param("contentType") String contentType);
}
