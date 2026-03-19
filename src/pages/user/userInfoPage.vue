<template>
  <div id="userInfoPage">
    <a-form :model="userData" name="nest-messages" autocomplete="off" @finish="onFinish">
      <a-avatar :src="userData.userAvatar" name="userAvatar" class="avatar"></a-avatar>
      <a-form-item
        label="昵称"
        name="userName"
        :rules="[
          { required: true, message: '请输入昵称!' },
          { max: 6, message: '昵称长度不能超过6位' },
        ]"
      >
        <a-input v-model:value="userData.userName" />
      </a-form-item>
      <a-form-item label="账户" name="userAccount">
        <a-input :disabled="true" v-model:value="userData.userAccount" />
      </a-form-item>
      <a-form-item
        label="密码"
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码!' },
          { min: 8, message: '密码长度不能少于8位' },
        ]"
      >
        <a-input-password v-model:value="userData.userPassword" />
      </a-form-item>
      <a-form-item
        label="确认密码"
        name="userPassword"
        :rules="[
          { required: true, message: '请输入确认密码!' },
          { min: 8, message: '密码长度不能少于8位' },
        ]"
      >
        <a-input-password v-model:value="userData.userPassword" />
      </a-form-item>
      <a-form-item label="简介" name="userProfile">
        <a-textarea v-model:value="userData.userProfile" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">提交</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { useLoginUserStore } from '@/stores/user.ts'
import { userEditInfoUsingPost, userLogoutUsingPost } from '@/api/yonghuguanlijiekou.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

const userData = reactive<API.UserEditRequest>({
  id: 0,
  userName: '',
  userAccount: '',
  userPassword: '',
  userProfile: '',
  userAvatar: '',
})
const loginUserStore = useLoginUserStore()
onMounted(async () => {
  await loginUserStore.fetchLoginUser()
  console.log(loginUserStore.loginUser.id)
  userData.id = loginUserStore.loginUser.id
  userData.userName = loginUserStore.loginUser.userName
  userData.userAccount = loginUserStore.loginUser.userAccount
  userData.userProfile = loginUserStore.loginUser.userProfile
  userData.userAvatar = loginUserStore.loginUser.userAvatar
})

const onFinish = async (values: any) => {
  console.log('userData:', userData)
  const res = await userEditInfoUsingPost(userData)
  if (res.data.code === 0) {
    message.success('修改成功! 请重新登录')
    await userLogoutUsingPost(values)
    await loginUserStore.fetchLoginUser()
    await router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('修改失败' + res.data.message)
  }
}
</script>

<style scoped>
#userInfoPage {
  width: 500px;
  margin: 0 auto;
}

.avatar {
  width: 100px;
  height: 100px;
  margin: 0 auto;
  display: block;
}
</style>
