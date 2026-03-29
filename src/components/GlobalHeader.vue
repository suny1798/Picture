<template>
  <div id="globalheader">
    <a-row :wrap="false">
      <a-col flex="300px">
        <router-link to="/">
          <div class="tittle-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="tittle">Suny智能协同云图库</div>
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
      <a-col flex="200px">
        <div class="user-login-state">
          <div v-if="loginUserStore.loginUser.id" style="text-align: right">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar">
                  {{ (loginUserStore.loginUser.userName ?? '匿名').charAt(0) }}
                </a-avatar>
                <span class="username">
                  {{ loginUserStore.loginUser.userName ?? '匿名' }}
                </span>
                <a-tag :color="getReviewColor(<string>loginUserStore.loginUser.userRole)">
                  <div v-if="loginUserStore.loginUser.userRole == 'admin'">管理员</div>
                  <div v-else-if="loginUserStore.loginUser.userRole == 'svip'">超级会员</div>
                  <div v-else-if="loginUserStore.loginUser.userRole == 'fvip'">临时会员</div>
                  <div v-else>普通用户</div>
                </a-tag>
              </a-space>
              <template #overlay>
                <a-menu class="my-custom-menu" style="text-align: center">
                  <a-menu-item @click="doUserInfo"> <UserOutlined /> 个人中心</a-menu-item>
                  <a-menu-item>
                    <router-link to="/my_space"> <LaptopOutlined /> 我的空间 </router-link>
                  </a-menu-item>
                  <a-menu-item @click="doLogout" style="color: red">
                    <LogoutOutlined /> 退出登录</a-menu-item
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
import { computed, h, onMounted, ref } from 'vue'
import {
  HomeOutlined,
  UserOutlined,
  LogoutOutlined,
  GithubOutlined,
  PictureFilled,
  FileImageOutlined,
  WindowsOutlined,
  LaptopOutlined,
} from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/user.ts'
import {} from '@/api/yonghuxiangguanjiekou.ts'
import { userLogoutUsingPost } from '@/api/yonghuxiangguanjiekou.ts'
import { PIC_REVIEW_STATUS_ENUM, USER_ROLE } from '@/constants/picture/picture.ts'

const loginUserStore = useLoginUserStore()

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
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('退出登录失败' + res.data.message)
  }
}

// 菜单列表
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/add_picture',
    icon: () => h(PictureFilled),
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/userManage',
    icon: () => h(UserOutlined),
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    icon: () => h(FileImageOutlined),
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    icon: () => h(WindowsOutlined),
    label: '空间管理',
    title: '空间管理',
  },
  {
    key: 'other',
    icon: () => h(GithubOutlined),
    label: h('a', { href: 'https://github.com/suny1798', target: '_black' }, 'Github'),
    title: 'Github',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    if (menu?.key?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const items = computed<MenuProps['items']>(() => filterMenus(originItems))

const router = useRouter()

//菜单点击时间
const doMenuClick = ({ key }) => {
  router.push({
    path: key,
  })
}

const getReviewColor = (status: string) => {
  switch (status) {
    case USER_ROLE.ADMIN_ROLE:
      return 'red' // 管理员
    case USER_ROLE.SUPER_ROLE:
      return 'green' // 超级会员
    case USER_ROLE.F_ROLE:
      return 'green' // 临时会员
    default:
      return 'default' // 普通用户
  }
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

.my-custom-menu :deep(.ant-dropdown-menu-item) {
  min-height: 40px;
  line-height: 40px; /* 建议同时设置行高以垂直居中文字 */
  padding: 0 20px; /* 可选：调整内边距 */
}

.logout-item :deep(.ant-dropdown-menu-item) {
  color: red;
}

.username {
  max-width: 80px; /* 控制最大宽度 */
  display: inline-block;
  white-space: nowrap; /* 不换行 */
  overflow: hidden;
  text-overflow: ellipsis; /* 省略号 */
  vertical-align: middle;
}
</style>
