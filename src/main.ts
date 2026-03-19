import { createApp } from 'vue'
import { createPinia } from 'pinia'
// @ts-ignore
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import '@/access.ts'

import App from './App.vue'
import router from './router'

const app = createApp(App)


app.use(createPinia())
app.use(router)
app.use(Antd)

app.mount('#app')
