package com.bestfriend.danjjak.pattern.service;

import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternTemplateResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternType;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.TemplateStepResponse;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PatternCatalog {

    private final Map<PatternType, PatternTemplateResponse> templates;

    public PatternCatalog() {
        templates =
            Map.ofEntries(
                available(
                    PatternType.TRANSFER,
                    "등록한 사람에게 송금",
                    "등록한 사람의 계좌로 송금하는 업무입니다.",
                    true,
                    transferSteps()),
                available(
                    PatternType.PENSION_CHECK,
                    "연금 입금 확인",
                    "최근 연금 입금 내역을 확인하는 업무입니다.",
                    false,
                    oneStep("CHECK_RESULT", "연금 내역 확인", "연금 입금 내역을 확인해 주세요.", "task-2")),
                available(
                    PatternType.MANAGEMENT_FEE_CHECK,
                    "관리비 확인",
                    "선택한 계좌의 전체 기간 관리비 거래를 확인하는 업무입니다.",
                    false,
                    oneStep("CHECK_RESULT", "관리비 확인", "관리비 내역을 확인해 주세요.", "task-3")),
                available(
                    PatternType.BALANCE_CHECK,
                    "잔액 확인",
                    "내 계좌의 현재 잔액을 확인하는 업무입니다.",
                    false,
                    oneStep("CHECK_RESULT", "잔액 확인", "계좌 잔액을 확인해 주세요.", "task-4")),
                available(
                    PatternType.TRANSACTION_HISTORY,
                    "거래내역 조회",
                    "최근 거래 내역을 확인하는 업무입니다.",
                    false,
                    oneStep("CHECK_RESULT", "거래내역 확인", "최근 거래 내역을 확인해 주세요.", "task-5")),
                available(
                    PatternType.CUSTOMER_CENTER,
                    "고객센터 연결",
                    "고객센터 전화번호를 확인하고 연결하는 업무입니다.",
                    false,
                    oneStep("CALL_SUPPORT", "고객센터 연결", "도움이 필요하면 전화 연결하기를 눌러 주세요.", "task-6")),
                available(
                    PatternType.UTILITY_BILL_CHECK,
                    "공과금 확인",
                    "최근 공과금 납부 내역을 확인하는 업무입니다.",
                    false,
                    oneStep("CHECK_RESULT", "공과금 확인", "최근 공과금 내역을 확인해 주세요.", "task-8")),
                unavailable(PatternType.AUTO_TRANSFER_CHECK, "자동이체 확인"),
                unavailable(PatternType.CARD_HISTORY, "카드 이용내역"),
                unavailable(PatternType.DEPOSIT_MATURITY_CHECK, "예금 만기 확인"));
    }

    public List<PatternTemplateResponse> getTemplates() {
        return List.of(PatternType.values()).stream().map(templates::get).toList();
    }

    public PatternTemplateResponse get(PatternType type) {
        return templates.get(type);
    }

    private Map.Entry<PatternType, PatternTemplateResponse> available(
        PatternType type,
        String title,
        String description,
        boolean requiresLinkedAccount,
        List<TemplateStepResponse> steps) {
        return Map.entry(
            type,
            new PatternTemplateResponse(
                type, title, description, requiresLinkedAccount, true, steps));
    }

    private Map.Entry<PatternType, PatternTemplateResponse> unavailable(
        PatternType type, String title) {
        return Map.entry(
            type,
            new PatternTemplateResponse(
                type, title, "현재 시연에서는 제공하지 않는 업무입니다.", false, false, List.of()));
    }

    private List<TemplateStepResponse> transferSteps() {
        return List.of(
            step(1, "SELECT_SOURCE", "출금 계좌 선택", "송금할 본인 계좌를 선택해 주세요.", "transfer-source", "source-account-list"),
            step(2, "SELECT_PERSON", "받는 사람 선택", "등록된 받는 사람을 선택해 주세요.", "guide-person", "registered-person-list"),
            step(3, "SELECT_ACCOUNT", "받는 계좌 선택", "받는 계좌를 선택해 주세요.", "guide-account", "recipient-account-list"),
            step(4, "INPUT_AMOUNT", "금액 입력", "보낼 금액을 입력해 주세요.", "amount-input", "amount-keypad"),
            step(5, "CONFIRM_TRANSFER", "송금 내용 확인", "받는 분과 금액이 맞는지 확인해 주세요.", "final-confirm", "transfer-summary"),
            step(6, "ENTER_PIN", "PIN 입력", "계좌 비밀번호 네 자리를 입력해 주세요.", "pin-entry", "pin-keypad"));
    }

    private List<TemplateStepResponse> oneStep(
        String code, String name, String instruction, String screen) {
        return List.of(step(1, code, name, instruction, screen, null));
    }

    private TemplateStepResponse step(
        int order,
        String code,
        String name,
        String instruction,
        String screen,
        String target) {
        return new TemplateStepResponse(order, code, name, instruction, screen, target);
    }
}
