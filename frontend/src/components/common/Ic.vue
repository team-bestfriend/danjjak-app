<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="strokeWidth" stroke-linecap="round" stroke-linejoin="round">
    <component :is="iconSvg" />
  </svg>
</template>

<script setup>
import { computed, h } from 'vue';

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 22 },
  strokeWidth: { type: [Number, String], default: 2 }
});

const icons = {
  Transfer: () => [h('path', { d: 'M5 12h14M12 5l7 7-7 7' })],
  Pension: () => [h('rect', { x: '2', y: '5', width: '20', height: '14', rx: '2' }), h('path', { d: 'M2 10h20' })],
  Building: () => [h('path', { d: 'M3 21V7l9-4 9 4v14' }), h('path', { d: 'M9 21v-6h6v6' }), h('rect', { x: '9', y: '9', width: '2', height: '2' }), h('rect', { x: '13', y: '9', width: '2', height: '2' })],
  Wallet: () => [h('path', { d: 'M21 12V7H5a2 2 0 0 1 0-4h14v4' }), h('path', { d: 'M3 7v14a2 2 0 0 0 2 2h16v-5' }), h('circle', { cx: '18', cy: '12', r: '1', fill: 'currentColor' })],
  History: () => [h('path', { d: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z' }), h('polyline', { points: '14 2 14 8 20 8' }), h('line', { x1: '16', y1: '13', x2: '8', y2: '13' }), h('line', { x1: '16', y1: '17', x2: '8', y2: '17' })],
  Headset: () => [h('path', { d: 'M3 18v-6a9 9 0 0 1 18 0v6' }), h('path', { d: 'M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3z' }), h('path', { d: 'M3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z' })],
  CreditCard: () => [h('rect', { x: '1', y: '4', width: '22', height: '16', rx: '2' }), h('line', { x1: '1', y1: '10', x2: '23', y2: '10' })],
  Repeat: () => [h('polyline', { points: '17 1 21 5 17 9' }), h('path', { d: 'M3 11V9a4 4 0 0 1 4-4h14' }), h('polyline', { points: '7 23 3 19 7 15' }), h('path', { d: 'M21 13v2a4 4 0 0 1-4 4H3' })],
  Safe: () => [h('rect', { x: '3', y: '3', width: '18', height: '18', rx: '2' }), h('circle', { cx: '12', cy: '12', r: '3' }), h('path', { d: 'M12 9v-3M12 18v-3M9 12H6M18 12h-3' })],
  Globe: () => [h('circle', { cx: '12', cy: '12', r: '10' }), h('line', { x1: '2', y1: '12', x2: '22', y2: '12' }), h('path', { d: 'M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z' })],
  Mic: () => [h('path', { d: 'M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z' }), h('path', { d: 'M19 10v2a7 7 0 0 1-14 0v-2' }), h('line', { x1: '12', y1: '19', x2: '12', y2: '23' }), h('line', { x1: '8', y1: '23', x2: '16', y2: '23' })],
  MicLg: () => [h('path', { d: 'M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z' }), h('path', { d: 'M19 10v2a7 7 0 0 1-14 0v-2' }), h('line', { x1: '12', y1: '19', x2: '12', y2: '23' }), h('line', { x1: '8', y1: '23', x2: '16', y2: '23' })],
  Check: () => [h('polyline', { points: '20 6 9 17 4 12' })],
  ChevR: () => [h('polyline', { points: '9 18 15 12 9 6' })],
  Back: () => [h('polyline', { points: '15 18 9 12 15 6' })],
  Warning: () => [h('path', { d: 'M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z' }), h('line', { x1: '12', y1: '9', x2: '12', y2: '13' }), h('line', { x1: '12', y1: '17', x2: '12.01', y2: '17' })],
  Phone: () => [h('path', { d: 'M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.62 3.35 2 2 0 0 1 3.59 1h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9a16 16 0 0 0 6.72 6.72l1.36-1.36a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z' })],
  Shield: () => [h('path', { d: 'M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z' })],
  Chart: () => [h('line', { x1: '18', y1: '20', x2: '18', y2: '10' }), h('line', { x1: '12', y1: '20', x2: '12', y2: '4' }), h('line', { x1: '6', y1: '20', x2: '6', y2: '14' })],
  Gear: () => [h('circle', { cx: '12', cy: '12', r: '3' }), h('path', { d: 'M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z' })],
  Home: () => [h('path', { d: 'M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z' }), h('polyline', { points: '9 22 9 12 15 12 15 22' })],
  List: () => [h('line', { x1: '8', y1: '6', x2: '21', y2: '6' }), h('line', { x1: '8', y1: '12', x2: '21', y2: '12' }), h('line', { x1: '8', y1: '18', x2: '21', y2: '18' }), h('line', { x1: '3', y1: '6', x2: '3.01', y2: '6' }), h('line', { x1: '3', y1: '12', x2: '3.01', y2: '12' }), h('line', { x1: '3', y1: '18', x2: '3.01', y2: '18' })],
  Voice: () => [h('polygon', { points: '11 5 6 9 2 9 2 15 6 15 11 19 11 5' }), h('path', { d: 'M15.54 8.46a5 5 0 0 1 0 7.07' }), h('path', { d: 'M19.07 4.93a10 10 0 0 1 0 14.14' })],
  Plus: () => [h('line', { x1: '12', y1: '5', x2: '12', y2: '19' }), h('line', { x1: '5', y1: '12', x2: '19', y2: '12' })],
  Del: () => [h('path', { d: 'M21 4H8l-7 8 7 8h13a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z' }), h('line', { x1: '18', y1: '9', x2: '12', y2: '15' }), h('line', { x1: '12', y1: '9', x2: '18', y2: '15' })],
  Bot: () => [h('rect', { x: '3', y: '11', width: '18', height: '11', rx: '2' }), h('circle', { cx: '12', cy: '5', r: '2' }), h('path', { d: 'M12 7v4' }), h('line', { x1: '8', y1: '16', x2: '8', y2: '16' }), h('line', { x1: '16', y1: '16', x2: '16', y2: '16' })]
};

const iconSvg = computed(() => {
  return icons[props.name] || icons.Transfer;
});
</script>
