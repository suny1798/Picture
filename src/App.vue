<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { health } from '@/api/mainController.ts'
import { useLoginUserStore } from '@/stores/user.ts'

const userLoginSrote = useLoginUserStore()
userLoginSrote.fetchLoginUser()


health().then((res) => {
  console.log(res)
})
</script>

<style>
#app {
}
</style>
