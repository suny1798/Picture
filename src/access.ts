import router from '@/router'
import { useLoginUserStore } from '@/stores/user.ts'
import { message } from 'ant-design-vue'

/**
 * 全局权限校验
 */
let firstFetchLoginUser = true

router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUSer = loginUserStore.loginUser

  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUSer = loginUserStore.loginUser
    firstFetchLoginUser = false
  }
  const toUrl = to.fullPath
  //自定义权限校验
  if (toUrl.startsWith('/admin')) {
    if (loginUSer.userRole !== 'admin' || !loginUSer) {
      message.error('您没有权限访问该页面! 请先登录')
      next('/user/login')
      return
    }
  }
  next()
})
