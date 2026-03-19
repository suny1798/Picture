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
      <!-- 用户信息展示-->
      <a-col flex="150px">
        <div class="user-login-state">
          <div v-if="userLoginStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="userLoginStore.loginUser.userAvatar">
                  {{ (userLoginStore.loginUser.userName ?? '匿名').charAt(0) }}
                </a-avatar>

                {{ userLoginStore.loginUser.userName ?? '匿名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doUserInfo"> <UserOutlined /> 个人中心</a-menu-item>
                  <a-menu-item @click="doLogout" style="color: red">
                    <ApiTwoTone /> 退出登录</a-menu-item
                  >
                </a-menu>
              </template>
            </a-dropdown>
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
import { h, onMounted, ref } from 'vue'
import { HomeOutlined, UserOutlined, ApiTwoTone } from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/user.ts'
import { userLogoutUsingPost } from '@/api/yonghuguanlijiekou.ts'

const userLoginStore = useLoginUserStore()

//用户个人中心
const doUserInfo = async () => {
  await router.push({
    path: '/user/userInfo',
  })
}

//用户注销
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    userLoginStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push({
      path: '/user/login',
    })
  } else {
    message.error('退出登录失败' + res.data.message)
  }
}
const items = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
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
