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
import { computed, h, onMounted, ref, watchEffect } from 'vue'
import { UserOutlined, PictureOutlined, LaptopOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/user.ts'
import { SPACE_TYPE_ENUM } from '@/constants/space/space.ts'
import { listMyTeamSpaceUsingPost } from '@/api/kongjianyonghuxiangguanjiekou.ts'
import { message } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()

//用户个人中心
const doUserInfo = async () => {
  await router.push({
    path: '/user/userInfo',
  })
}

// 原始固定列表
const fixedMenuItems = [
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
  {
    key: '/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    icon: () => h(TeamOutlined),
    label: '创建团队',
    title: '创建团队',
  },
]

const teamSpaceList = ref<API.SpaceUserVO[]>([])

const muneItems = computed(() => {
  //如果没有团队空间  只展示固定菜单
  if (teamSpaceList.value.length === 0) {
    return fixedMenuItems
  }
  //如果有团队空间  团队空间菜单放在固定菜单后面
  const teamSpaceSubMenuItems = teamSpaceList.value.map((spaceUser) => {
    const space = spaceUser.space
    return {
      key: '/space/' + spaceUser.spaceId,
      label: space?.spaceName,
    }
  })
  const  teamSpaceMenuGroup = {
    type: 'group',
    key: 'teamSpace',
    label: '团队空间',
    children: teamSpaceSubMenuItems,
  }
  return [...fixedMenuItems, teamSpaceMenuGroup]

})

//加载空间团队列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data){
    teamSpaceList.value = res.data.data
  }else {
    message.error('加载我的团队空间失败，' + res.data.message)
  }
}

watchEffect(()=>{
  if (loginUserStore.loginUser.id){
    fetchTeamSpaceList()
  }
})


const router = useRouter()

//菜单点击时间
const doMenuClick = ({ key }) => {
  router.push(key)
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
