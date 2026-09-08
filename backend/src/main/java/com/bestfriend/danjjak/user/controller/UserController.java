package com.bestfriend.danjjak.user.controller;

import com.bestfriend.danjjak.common.session.DemoSessionUserResolver;
import com.bestfriend.danjjak.user.dto.UserDtos.AccessibilitySettings;
import com.bestfriend.danjjak.user.dto.UserDtos.ConsentSettings;
import com.bestfriend.danjjak.user.dto.UserDtos.ConsentUpdateRequest;
import com.bestfriend.danjjak.user.dto.UserDtos.CurrentUserResponse;
import com.bestfriend.danjjak.user.service.UserService;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;
    private final DemoSessionUserResolver userResolver;

    public UserController(UserService userService, DemoSessionUserResolver userResolver) {
        this.userService = userService;
        this.userResolver = userResolver;
    }

    @GetMapping
    public CurrentUserResponse getCurrentUser(HttpSession session) {
        return userService.getCurrentUser(userResolver.resolveUserId(session));
    }

    @PutMapping("/consents")
    public ConsentSettings updateConsents(
        @Valid @RequestBody ConsentUpdateRequest request, HttpSession session) {
        return userService.updateConsents(userResolver.resolveUserId(session), request);
    }

    @PutMapping("/settings")
    public AccessibilitySettings updateSettings(
        @Valid @RequestBody AccessibilitySettings request, HttpSession session) {
        return userService.updateSettings(userResolver.resolveUserId(session), request);
    }
}
