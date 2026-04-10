<template>
  <div id="userLoginPage">
    <h1 class="tittle">欢迎回到 视觉档案馆</h1>
    <div class="desc">在这里，持续记录与展示你的每一份作品</div>
    <a-form
      :model="formState"
      name="basic"
      autocomplete="off"
      @finish="handleSubmit"
      @finishFailed="onFinishFailed"
    >
      <a-form-item name="userAccount" :rules="[{ required: true, message: '账号不能为空!' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '密码不能为空!' },
          { min: 8, message: '密码长度不能少于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>

      <div class="tips">
        没有账号？
        <router-link to="/user/register">立即注册</router-link>
      </div>

      <a-form-item style="text-align: center">
        <a-button :loading="loading" type="primary" html-type="submit">进入空间</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { userLoginUsingPost } from '@/api/yonghuxiangguanjiekou.ts'
import { useLoginUserStore } from '@/stores/user.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useRoute } from 'vue-router'

const loginUserStore = useLoginUserStore()
const loading = ref(false)

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const route = useRoute()

const handleSubmit = async (values: any) => {
  loading.value = true
  try {
    const res = await userLoginUsingPost(values)
    if (res.data.code === 0 && res.data.data) {
      await loginUserStore.fetchLoginUser()
      message.success('登录成功')
      loading.value = false
      const redirect = route.query.redirect as string
      if (redirect) {
        await router.push(redirect)
      } else {
        await router.push({
          path: '/',
          replace: true,
        }) // 默认首页
      }
    } else {
      message.error('登录失败，' + res.data.message)
    }
  } catch (e) {
    message.error('登录失败，' + e.message)
  }
  loading.value = false
}

const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo)
}
</script>

<style scoped>
#userLoginPage {
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
.loading {
  text-align: center;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
  padding: 20px 30px;
  margin: 20px 0;
  max-width: 200px;
}
</style>
