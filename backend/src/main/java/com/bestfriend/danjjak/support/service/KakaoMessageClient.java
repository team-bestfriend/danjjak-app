package com.bestfriend.danjjak.support.service;

public interface KakaoMessageClient {

    String RESULT_UNKNOWN = "KAKAO_RESULT_UNKNOWN";
    String NOT_CONFIGURED = "KAKAO_NOT_CONFIGURED";
    String REQUEST_NOT_SENT = "KAKAO_REQUEST_NOT_SENT";

    KakaoSendResult sendToMe(String accessToken, String message);

    record KakaoSendResult(boolean success, Integer httpStatus, String detail) {}
}
