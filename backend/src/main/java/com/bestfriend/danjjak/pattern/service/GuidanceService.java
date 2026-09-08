package com.bestfriend.danjjak.pattern.service;

import com.bestfriend.danjjak.common.error.ApiException;
import com.bestfriend.danjjak.pattern.dto.GuidanceDtos.GuidanceResponse;
import com.bestfriend.danjjak.pattern.dto.GuidanceDtos.GuidanceUpdateRequest;
import com.bestfriend.danjjak.pattern.mapper.GuidanceMapper;
import com.bestfriend.danjjak.pattern.model.GuidanceRecord;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GuidanceService {
    private final GuidanceMapper mapper;
    private final VoiceFileStore files;

    public GuidanceService(GuidanceMapper mapper, VoiceFileStore files) {
        this.mapper = mapper;
        this.files = files;
    }

    public List<GuidanceResponse> getAll(long userId, long patternId) {
        List<GuidanceRecord> records = mapper.findAll(userId, patternId);
        if (records.isEmpty()) throw missing();
        return records.stream().map(record -> response(patternId, record)).toList();
    }

    @Transactional
    public GuidanceResponse update(long userId, long patternId, String target, GuidanceUpdateRequest request) {
        find(userId, patternId, target);
        if (mapper.updateText(userId, patternId, target, request.text().strip(),
            request.voiceMode() == null ? null : request.voiceMode().name()) != 1) throw missing();
        return response(patternId, find(userId, patternId, target));
    }

    @Transactional
    public GuidanceResponse upload(long userId, long patternId, String target, MultipartFile file) {
        GuidanceRecord previous = find(userId, patternId, target);
        VoiceFileStore.StoredVoice stored = files.save(file);
        boolean synchronizedTransaction = TransactionSynchronizationManager.isSynchronizationActive();
        if (synchronizedTransaction) {
            // DB 커밋 전에는 이전 파일을 지우지 않고, 롤백되면 새 파일만 정리한다.
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    files.delete(status == STATUS_COMMITTED ? previous.getVoiceFilePath() : stored.filePath());
                }
            });
        }
        try {
            if (mapper.updateAudio(userId, patternId, target, stored.filePath(), stored.contentType()) != 1) throw missing();
            GuidanceResponse result = response(patternId, find(userId, patternId, target));
            if (!synchronizedTransaction) files.delete(previous.getVoiceFilePath());
            return result;
        } catch (RuntimeException exception) {
            if (!synchronizedTransaction) files.delete(stored.filePath());
            throw exception;
        }
    }

    public record AudioResponse(Resource resource, String contentType) {}

    public AudioResponse audio(long userId, long patternId, String target) {
        GuidanceRecord record = find(userId, patternId, target);
        return new AudioResponse(files.read(record.getVoiceFilePath()), record.getVoiceContentType());
    }

    private GuidanceRecord find(long userId, long patternId, String target) {
        return mapper.findAll(userId, patternId).stream()
            .filter(record -> record.getTarget().equals(target)).findFirst().orElseThrow(GuidanceService::missing);
    }

    private static ApiException missing() {
        return new ApiException(HttpStatus.NOT_FOUND, "GUIDANCE_NOT_FOUND", "안내를 찾을 수 없습니다.");
    }

    private GuidanceResponse response(long patternId, GuidanceRecord record) {
        String url = record.getVoiceFilePath() == null ? null
            : "/api/patterns/" + patternId + "/guidance/" + record.getTarget() + "/audio";
        return new GuidanceResponse(record.getTarget(), record.getText(), record.getVoiceMode(), url,
            record.getVoiceContentType(), record.isVoiceScriptOutdated());
    }
}
