<template>
  <div class="flex flex-col h-full bg-white">
    <SafeArea />
    <TopBar :title="existingPerson ? '등록 정보 수정' : '새로운 사람 등록'" :onBack="store.goBack" />
    <div class="flex-1 overflow-y-auto px-4 pt-4 pb-6">
      <AddPersonForm :existingPerson="existingPerson" @saved="onSaved" @cancel="store.goBack" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useAppStore } from '../stores/appStore';
import SafeArea from '../components/common/SafeArea.vue';
import TopBar from '../components/common/TopBar.vue';
import AddPersonForm from '../components/common/AddPersonForm.vue';

const store = useAppStore();
const existingPerson = computed(() => {
  const person = store.people.find((item) => item.id === store.editingPersonId);
  if (!person) return null;
  return { ...person, account: store.accountsByPerson[person.id]?.[0] ?? null };
});

onMounted(() => store.loadFinancialData());

function onSaved() {
  store.editingPersonId = null;
  store.goBack();
}
</script>
