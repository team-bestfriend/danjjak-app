<script setup>
import { onBeforeUnmount, onMounted } from "vue";

const emit = defineEmits(["finished"]);

let timer;

onMounted(() => {
  timer = window.setTimeout(() => {
    emit("finished");
  }, 1800);
});

onBeforeUnmount(() => {
  window.clearTimeout(timer);
});
</script>

<template>
  <div
    class="splash-screen"
    role="status"
    aria-label="단짝 앱을 시작하고 있습니다"
  >
    <img src="../../assets/danjjak-logo.png" alt="단짝" class="splash-logo" />
  </div>
</template>

<style scoped>
.splash-screen {
  position: absolute;
  inset: 0;
  z-index: 100;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: #ffbc00;
  animation: splash-fade 1.8s ease forwards;
}

.splash-logo {
  width: 144px;
  height: 144px;
  object-fit: contain;
  border-radius: 32px;
}

@keyframes splash-fade {
  0%,
  75% {
    opacity: 1;
  }

  100% {
    opacity: 0;
  }
}
</style>
