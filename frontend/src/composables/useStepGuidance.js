import { onScopeDispose, ref, toValue, watch } from 'vue';

export function stepInstruction(step) {
  return step?.instructionText?.trim() || '화면의 안내를 확인하고 원하는 작업을 선택해 주세요.';
}

export function isWrongGuidanceControl(control, target) {
  return Boolean(control && target
    && !control.matches(':disabled, [aria-disabled="true"]')
    && !control.closest('[data-guide-exempt]')
    && control !== target && !target.contains(control));
}

export function useStepGuidance(root, step, ready, onWrongTouch) {
  const notice = ref('');
  let target = null;
  let observer = null;
  let previousDescription = null;
  let lastDiagnostic = '';

  function clearTarget() {
    if (!target) return;
    target.classList.remove('step-guide-target');
    if (previousDescription === null) target.removeAttribute('aria-describedby');
    else target.setAttribute('aria-describedby', previousDescription);
    target = null;
  }

  function updateTarget() {
    const area = toValue(root);
    const current = toValue(step);
    const targetId = current?.targetElementId;
    if (!area || !current || !toValue(ready) || !targetId) {
      clearTarget();
      notice.value = '';
      return;
    }
    // 서버 식별자는 선택자나 진단 로그에 삽입하지 않고 지정된 조작 영역과 비교한다.
    const matches = [...area.querySelectorAll('[data-guide-target]')]
      .filter((element) => element.dataset.guideTarget === targetId && element.getClientRects().length > 0);
    const next = matches.length === 1 ? matches[0] : null;
    if (target !== next) {
      clearTarget();
      target = next;
      if (target) {
        previousDescription = target.getAttribute('aria-describedby');
        target.setAttribute('aria-describedby', [previousDescription, 'step-guidance-caption'].filter(Boolean).join(' '));
        // 금액·비밀번호 입력은 자막과 음성으로 안내하고 키패드는 강조하지 않는다.
        if (!['amount-keypad', 'pin-keypad'].includes(targetId)) target.classList.add('step-guide-target');
      }
    }
    notice.value = target ? '' : '강조할 대상을 확인하지 못했어요. 화면 안내에 따라 직접 선택해 주세요.';
    const diagnostic = target ? '' : matches.length > 1 ? 'DUPLICATE_TARGET' : 'MISSING_TARGET';
    if (diagnostic && diagnostic !== lastDiagnostic) console.warn(`[step-guidance] ${diagnostic}`);
    lastDiagnostic = diagnostic;
  }

  function handleClick(event) {
    if (!toValue(step) || !toValue(ready)) return;
    updateTarget();
    const control = event.target?.closest?.('button, a[href], input, select, textarea, [role="button"]');
    if (!control || !toValue(root)?.contains(control) || !isWrongGuidanceControl(control, target)) return;
    // 취소·뒤로가기·오류 복구는 허용하고, 다른 조작으로 단계가 진행되는 것만 막는다.
    event.preventDefault();
    event.stopImmediatePropagation();
    notice.value = '테두리로 표시한 곳을 눌러 주세요. 뒤로 가기와 취소도 사용할 수 있어요.';
    onWrongTouch();
  }

  watch(
    [() => toValue(root), () => toValue(step), () => toValue(ready)],
    () => {
      observer?.disconnect();
      clearTarget();
      lastDiagnostic = '';
      if (toValue(step) && !toValue(step).instructionText?.trim()) console.warn('[step-guidance] MISSING_INSTRUCTION');
      updateTarget();
      const area = toValue(root);
      if (!area || !toValue(step)?.targetElementId) return;
      observer = new MutationObserver(updateTarget);
      observer.observe(area, { childList: true, subtree: true, attributes: true, attributeFilter: ['data-guide-target', 'disabled'] });
    },
    { immediate: true, flush: 'post' },
  );

  onScopeDispose(() => {
    observer?.disconnect();
    clearTarget();
  });
  return { notice, handleClick };
}
