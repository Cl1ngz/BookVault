<script setup lang="ts">
defineProps<{
  title: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
}>()

const emit = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()
</script>

<template>
  <Teleport to="body">
    <div class="modal-backdrop" @click.self="emit('cancel')">
      <div class="modal-box">
        <div class="modal-icon">🎉</div>
        <h2 class="modal-title">{{ title }}</h2>
        <p class="modal-message">{{ message }}</p>
        <div class="modal-actions">
          <button class="modal-btn modal-btn--confirm" @click="emit('confirm')">
            {{ confirmLabel ?? 'Yes, mark as Finished' }}
          </button>
          <button class="modal-btn modal-btn--cancel" @click="emit('cancel')">
            {{ cancelLabel ?? 'Keep Reading' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.65);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-box {
  background: #3c3836;
  border: 1px solid #fabd2f;
  border-radius: 14px;
  padding: 2rem 2.5rem;
  max-width: 420px;
  width: 90%;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.55);
  animation: pop-in 0.18s ease;
}

@keyframes pop-in {
  from { transform: scale(0.88); opacity: 0; }
  to   { transform: scale(1);    opacity: 1; }
}

.modal-icon {
  font-size: 3rem;
  margin-bottom: 0.5rem;
}

.modal-title {
  color: #fabd2f;
  font-size: 1.25rem;
  margin: 0 0 0.75rem;
  font-weight: 700;
}

.modal-message {
  color: #d5c4a1;
  font-size: 0.95rem;
  margin: 0 0 1.75rem;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.modal-btn {
  padding: 10px 0;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: filter 0.15s;
}

.modal-btn:hover { filter: brightness(1.15); }

.modal-btn--confirm {
  background: #b8bb26;
  color: #1d2021;
}

.modal-btn--cancel {
  background: #504945;
  color: #d5c4a1;
}
</style>

