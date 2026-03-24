<template>
  <div id="globalSider">
    <a-layout-sider v-if="loginUserStore.loginUser.id" breakpoint="lg">
      <a-menu
        v-model:selectedKeys="current"
        mode="inline"
        :items="muneItems"
        @click="doMenuClick"
      />
    </a-layout-sider>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, onMounted, ref } from 'vue'
import { UserOutlined, PictureOutlined, LaptopOutlined } from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/user.ts'
import {} from '@/api/yonghuxiangguanjiekou.ts'
import { userLogoutUsingPost } from '@/api/yonghuxiangguanjiekou.ts'

const loginUserStore = useLoginUserStore()

//用户个人中心
const doUserInfo = async () => {
  await router.push({
    path: '/user/userInfo',
  })
}

// 菜单列表
const muneItems = [
  {
    key: '/',
    icon: () => h(PictureOutlined),
    label: '公共图库',
    title: '公共图库',
  },
  {
    key: '/my_space',
    icon: () => h(LaptopOutlined),
    label: '我的空间',
    title: '我的空间',
  },
]

const router = useRouter()

//菜单点击时间
const doMenuClick = ({ key }) => {
  router.push({
    path: key,
  })
}

//当前要高亮的菜单项
const current = ref<string[]>(['/'])
//监听路由变化，更新菜单项
router.afterEach((to, from, failure) => {
  current.value = [to.path]
})
</script>

<style scoped>
:deep(.ant-layout-sider-children) {
  margin-top: 0;
}
</style>
