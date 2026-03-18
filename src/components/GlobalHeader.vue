<template>
  <div id="globalheader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <router-link to="/">
          <div class="tittle-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="tittle">Suny云图库</div>
          </div>
        </router-link>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <a-col flex="60px">
        <div class="user-login-state">
          <div v-if="userLoginStore.loginUser.id">
            {{ userLoginStore.loginUser.userName ?? '无名' }}
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { h, ref } from 'vue'
import { HomeOutlined, AppstoreOutlined, SettingOutlined } from '@ant-design/icons-vue'
import { MenuProps } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/user.ts'

const userLoginStore = useLoginUserStore()

const items = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/about',
    label: '关于',
    title: '关于',
  },
  {
    key: 'other',
    label: h('a', { href: 'https://github.com/suny1798', target: '_black' }, 'Github'),
    title: 'Github',
  },
])

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
.tittle-bar {
  display: flex;
  align-items: center;
}
.tittle {
  font-size: 18px;
  color: black;
  margin-left: 16px;
}

.logo {
  height: 35px;
}
</style>
