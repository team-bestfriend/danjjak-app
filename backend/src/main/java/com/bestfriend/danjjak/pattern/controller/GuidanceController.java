package com.bestfriend.danjjak.pattern.controller;

import com.bestfriend.danjjak.common.session.DemoSessionUserResolver;
import com.bestfriend.danjjak.pattern.dto.GuidanceDtos.GuidanceResponse;
import com.bestfriend.danjjak.pattern.dto.GuidanceDtos.GuidanceUpdateRequest;
import com.bestfriend.danjjak.pattern.service.GuidanceService;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/patterns/{patternId}/guidance")
public class GuidanceController {
    private final GuidanceService service;
    private final DemoSessionUserResolver users;

    public GuidanceController(GuidanceService service, DemoSessionUserResolver users) {
        this.service = service;
        this.users = users;
    }

    @GetMapping
    public List<GuidanceResponse> getAll(@PathVariable long patternId, HttpSession session) {
        return service.getAll(users.resolveUserId(session), patternId);
    }

    @PutMapping("/{target}")
    public GuidanceResponse update(@PathVariable long patternId, @PathVariable String target,
                                   @Valid @RequestBody GuidanceUpdateRequest request, HttpSession session) {
        return service.update(users.resolveUserId(session), patternId, target, request);
    }

    @PostMapping(value = "/{target}/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public GuidanceResponse upload(@PathVariable long patternId, @PathVariable String target,
                                   @RequestPart("file") MultipartFile file, HttpSession session) {
        return service.upload(users.resolveUserId(session), patternId, target, file);
    }

    @GetMapping("/{target}/audio")
    public ResponseEntity<Resource> audio(@PathVariable long patternId, @PathVariable String target,
                                          HttpSession session) {
        GuidanceService.AudioResponse audio = service.audio(users.resolveUserId(session), patternId, target);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .contentType(MediaType.parseMediaType(audio.contentType())).body(audio.resource());
    }
}
