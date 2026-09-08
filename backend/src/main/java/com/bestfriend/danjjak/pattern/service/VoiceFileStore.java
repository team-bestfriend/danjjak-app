package com.bestfriend.danjjak.pattern.service;

import com.bestfriend.danjjak.common.error.ApiException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class VoiceFileStore {
    public static final long MAX_BYTES = 10 * 1024 * 1024;
    private static final Set<String> TYPES = Set.of(
        "audio/webm", "audio/ogg", "audio/mp4", "audio/mpeg", "audio/wav", "audio/x-wav");
    private final Path directory;

    @Autowired
    public VoiceFileStore(@Value("${DANJJAK_VOICE_DIR:${user.home}/.danjjak/voices}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public record StoredVoice(String filePath, String contentType) {}

    public StoredVoice save(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VOICE_EMPTY", "녹음 파일이 비어 있습니다.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "VOICE_TOO_LARGE", "녹음 파일은 10MB 이하로 올려 주세요.");
        }
        String type = file.getContentType() == null ? ""
            : file.getContentType().split(";", 2)[0].strip().toLowerCase(Locale.ROOT);
        if (!TYPES.contains(type)) {
            throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "VOICE_TYPE_UNSUPPORTED",
                "WebM, OGG, MP4, MP3 또는 WAV 음성 파일을 선택해 주세요.");
        }
        String name = UUID.randomUUID().toString();
        try {
            Files.createDirectories(directory);
            // 원래 파일명은 경로로 사용하지 않는다. WAR 바깥에 저장해 재배포 때 녹음을 유지한다.
            file.transferTo(resolve(name));
            return new StoredVoice(name, type);
        } catch (IOException | IllegalStateException exception) {
            delete(name);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "VOICE_STORAGE_UNAVAILABLE",
                "녹음을 저장하지 못했습니다. 기존 녹음은 유지됩니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    public Resource read(String name) {
        if (name == null || !Files.isRegularFile(resolve(name))) {
            throw new ApiException(HttpStatus.NOT_FOUND, "VOICE_NOT_FOUND", "저장된 녹음이 없습니다.");
        }
        return new FileSystemResource(resolve(name));
    }

    public void delete(String name) {
        if (name == null) return;
        try {
            Files.deleteIfExists(resolve(name));
        } catch (IOException exception) {
            // 파일 정리 실패가 이미 완료된 음성 교체를 실패로 바꾸지 않도록 한다.
            org.apache.logging.log4j.LogManager.getLogger(VoiceFileStore.class)
                .warn("사용하지 않는 녹음 파일을 정리하지 못했습니다.");
        }
    }

    private Path resolve(String name) {
        Path path = directory.resolve(name).normalize();
        if (!path.getParent().equals(directory)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "VOICE_NOT_FOUND", "저장된 녹음이 없습니다.");
        }
        return path;
    }
}
