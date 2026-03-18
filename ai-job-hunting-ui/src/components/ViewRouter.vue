<script setup lang="ts">
import {inject, shallowRef} from "vue";
import {Platform} from "../platform/platform.js";

const platform = inject('$platform') as Platform;

// 获取平台对应组件
const renderComponent = shallowRef(null)
platform.getRenderComponent().then((data: any) => {
    renderComponent.value = data
})

</script>

<template>
    <component :is="renderComponent"></component>
</template>

<style scoped>

</style>
