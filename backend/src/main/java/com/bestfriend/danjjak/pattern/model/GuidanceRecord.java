package com.bestfriend.danjjak.pattern.model;

import lombok.Data;

@Data
public class GuidanceRecord {
    private String target;
    private String text;
    private String voiceMode;
    private String voiceFilePath;
    private String voiceContentType;
    private boolean voiceScriptOutdated;
}
