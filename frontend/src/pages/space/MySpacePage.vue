<template>
  <div id="addSpacePage">
    <p2>正在跳转中......</p2>
  </div>
</template>

<script setup lang="ts">
import { useLoginUserStore } from '@/stores/user.ts'
import { useRouter } from 'vue-router'
import { listSpaceVoByPageUsingPost } from '@/api/kongjianxiangguanjiekou.ts'
import { message } from 'ant-design-vue'
import { onMounted } from 'vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const checkUserSpace = async () => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser?.id) {
    await router.replace('/user/login')
    return
  }
  //获取用户已经创建的空间
  const res = await listSpaceVoByPageUsingPost({
    userId: loginUser.id,
    current: 1,
    pageSize: 1,
  })
  if (res.data.code === 0) {
    if (res.data.data?.records?.length > 0) {
      const space = res.data.data.records[0]
      await router.replace(`/space/${space.id}`)
    } else {
      await router.replace('/add_space')
      message.warn('您还没有创建空间，请先创建一个空间')
    }
  } else {
    message.error('加载我的空间失败，' + res.data.message)
  }
}
onMounted(() => {
  checkUserSpace()
})
</script>

<style scoped></style>
