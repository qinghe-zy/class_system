<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="title"
    class="app-dialog"
    modal-class="app-dialog-overlay"
    :width="width"
    align-center
    destroy-on-close
    append-to-body
  >
    <slot />
    <template #footer>
      <slot name="footer" />
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  modelValue: Boolean,
  title: {
    type: String,
    default: ''
  },
  width: {
    type: [String, Number],
    default: 760
  }
})

defineEmits(['update:modelValue'])
</script>