<template>
  <div id="userRegisterPage">
    <h2 class="tittle">Suny智能云图库 - 用户注册</h2>
    <div class="desc">企业级智能云图库</div>
    <a-form
      :model="formState"
      name="basic"
      autocomplete="off"
      @finish="handleSubmit"
      @finishFailed="onFinishFailed"
    >
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号!' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <a-form-item
        name="userName"
        :rules="[
          { required: true, message: '请输入昵称!' },
          { max: 10, message: '昵称长度不能超过10位' },
        ]"
      >
        <a-input v-model:value="formState.userName" placeholder="请输入昵称" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码!' },
          { min: 8, message: '密码长度不能少于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>

      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请输入确认密码!' },
          { min: 8, message: '密码长度不能少于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请再次输入密码" />
      </a-form-item>

      <div class="tips">
        已有账号？
        <router-link to="/user/login">立即登录</router-link>
      </div>

      <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
        <a-button type="primary" html-type="submit">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLoginUsingPost } from '@/api/yonghuxiangguanjiekou.ts'
import { useLoginUserStore } from '@/stores/user.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

const loginUserStore = useLoginUserStore()

const formState = reactive<API.UserRegisterRequest>({
  userName: '',
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})
const handleSubmit = async (values: any) => {
  console.log('Success:', values)
  try {
    if (values.userPassword !== values.checkPassword) {
      message.error('两次密码输入不一致')
      return
    }
    const res = await userLoginUsingPost(values)
    if (res.data.code === 0 && res.data.data) {
      message.success('注册成功')
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      message.error('注册失败' + res.data.message)
    }
  } catch (e) {
    message.error('注册失败' + e.message)
  }
}

const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo)
}
</script>

<style scoped>
#userRegisterPage {
  max-width: 360px;
  margin: 0 auto;
  padding-top: 100px;
}
.tittle {
  text-align: center;
  margin-bottom: 20px;
}

.desc {
  text-align: center;
  margin-bottom: 20px;
  color: #999;
}

.tips {
  text-align: right;
  margin-bottom: 20px;
  color: #999;
}
</style>
