<template>
  <el-tag :type="resolvedType" effect="light" round>{{ text }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'
import { pickTagType } from '../utils/format'

const props = defineProps({
  text: { type: String, default: '—' },
  type: { type: String, default: '' }
})

const resolvedType = computed(() => props.type || pickTagType(props.text))
</script>
