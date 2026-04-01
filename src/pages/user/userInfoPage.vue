<template>
  <div id="userInfoPage">
    <div class="container">
      <!-- 左侧：用户信息卡片 -->
      <div class="left">
        <a-card>
          <div class="avatarBox">
            <a-upload name="file" :show-upload-list="false" :custom-request="handleUpload">
              <a-avatar :src="userData.userAvatar" class="avatar" />
            </a-upload>
            <div class="name">{{ userData.userName }}</div>
            <div class="account">
              {{ userData.userAccount }}
              <a-tag v-if="userData.userRole == 'admin'">管理员</a-tag>
              <a-tag v-if="userData.userRole == 'user'">普通用户</a-tag>
              <a-tag v-if="userData.userRole == 'fvip'">临时会员</a-tag>
              <a-tag v-if="userData.userRole == 'svip'">永久会员</a-tag>
            </div>
            <div class="vipNumber" v-if="userData.vipNumber" style="margin-top: 10px">
              会员编号：{{ userData.vipNumber }}
            </div>
            <div class="expireTime" v-if="userData.vipNumber" style="margin-top: 10px">
              到期时间：{{ dayjs(userData.vipExpireTime).format('YYYY-MM-DD HH:mm') }}
            </div>
            <a-button style="margin-top: 10px" @click="openVipModal"> 兑换会员 </a-button>
          </div>
        </a-card>
      </div>

      <!-- 右侧：编辑表单 -->
      <div class="right">
        <a-card title="个人信息">
          <a-form :model="userData" layout="vertical" autocomplete="off" @finish="onFinish">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item
                  label="昵称"
                  name="userName"
                  :rules="[
                    { required: true, message: '请输入昵称!' },
                    { max: 10, message: '昵称长度不能超过10位' },
                  ]"
                >
                  <a-input v-model:value="userData.userName" />
                </a-form-item>
              </a-col>

              <a-col :span="12">
                <a-form-item label="账户">
                  <a-input disabled v-model:value="userData.userAccount" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item
                  label="原密码"
                  name="userOldPassword"
                  :rules="[{ min: 8, message: '密码长度不能少于8位' }]"
                >
                  <a-input-password
                    placeholder="为空则表示不更改密码"
                    v-model:value="userData.userOldPassword"
                  />
                </a-form-item>
              </a-col>

              <a-col :span="12">
                <a-form-item label="新密码" :rules="[{ min: 8, message: '密码长度不能少于8位' }]">
                  <a-input-password v-model:value="userData.userPassword" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item label="简介">
              <a-textarea v-model:value="userData.userProfile" rows="4" />
            </a-form-item>

            <div class="footer">
              <a-button type="primary" html-type="submit" :loading="loading">保存</a-button>
            </div>
          </a-form>
        </a-card>
      </div>
    </div>
  </div>
  <a-modal
    v-model:open="vipModalVisible"
    title="兑换会员（永久会员请联系管理员）"
    @ok="handleVipSubmit"
    :confirm-loading="vipLoading"
  >
    <a-form :model="vipForm" layout="vertical">
      <a-form-item label="兑换码" required>
        <a-input v-model:value="vipForm.vipCode" placeholder="请输入兑换码" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { useLoginUserStore } from '@/stores/user.ts'
import {
  uploadUserAvatarUsingPost,
  userEditInfoUsingPost,
  userLogoutUsingPost,
  userVipUsingPost,
} from '@/api/yonghuxiangguanjiekou.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import dayjs from 'dayjs'

const loading = ref(false)
const userData = reactive<API.UserEditRequest>({
  id: 0,
  userName: '',
  userAccount: '',
  userPassword: '',
  userOldPassword: '',
  userProfile: '',
  userAvatar: '',
  userRole: '',
  vipNumber: 0,
  vipExpireTime: '',
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
  userData.userRole = loginUserStore.loginUser.userRole
  userData.vipNumber = loginUserStore.loginUser.vipNumber
  userData.vipExpireTime = loginUserStore.loginUser.vipExpireTime
})

const onFinish = async (values: any) => {
  console.log('userData:', userData)
  loading.value = true
  const res = await userEditInfoUsingPost(userData)
  if (res.data.code === 0) {
    await loginUserStore.fetchLoginUser()
    if (!userData.userPassword) {
      message.success('修改成功!')
    } else {
      message.success('修改成功! 请重新登录')
      await userLogoutUsingPost(values)
      await router.push({
        path: '/user/login',
        replace: true,
      })
    }
  } else {
    message.error('修改失败，' + res.data.message)
  }
  loading.value = false
}

const handleUpload = async ({ file }: any) => {
  try {
    const res = await uploadUserAvatarUsingPost({}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('头像上传成功')
      // ⭐更新头像
      userData.userAvatar = res.data.data
    } else {
      message.error('上传失败')
    }
  } catch (e) {
    message.error('上传异常，' + e)
  }
}

//兑换VIp
const vipModalVisible = ref(false)
const vipLoading = ref(false)

const vipForm = reactive<API.UserVipRequest>({
  id: 0,
  vipCode: '',
})

const openVipModal = () => {
  vipForm.id = userData.id
  vipForm.vipCode = ''
  vipModalVisible.value = true
}

const handleVipSubmit = async () => {
  if (!vipForm.vipCode) {
    message.warning('请输入兑换码')
    return
  }

  vipLoading.value = true
  try {
    const res = await userVipUsingPost({
      ...vipForm,
    })

    if (res.data.code === 0) {
      message.success('兑换成功')
      vipModalVisible.value = false
      // ⭐刷新用户信息（关键）
      await loginUserStore.fetchLoginUser()
      // ⭐同步到页面
      userData.userRole = loginUserStore.loginUser.userRole
      userData.vipNumber = loginUserStore.loginUser.vipNumber
      userData.vipExpireTime = loginUserStore.loginUser.vipExpireTime
    } else {
      message.error('兑换失败：' + res.data.message)
    }
  } catch (e) {
    message.error('请求异常')
  } finally {
    vipLoading.value = false
  }
}
</script>

<style scoped>
#userInfoPage {
  padding: 24px;
}

/* 整体布局 */
.container {
  display: flex;
  gap: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

/* 左侧卡片 */
.left {
  width: 260px;
}

.avatarBox {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.avatar {
  width: 100px;
  height: 100px;
  margin-bottom: 12px;
}

.name {
  font-size: 18px;
  font-weight: 500;
}

.account {
  font-size: 12px;
  margin-top: 4px;
}

/* 右侧表单 */
.right {
  flex: 1;
}

/* 按钮区域 */
.footer {
  text-align: right;
  margin-top: 16px;
}
</style>
