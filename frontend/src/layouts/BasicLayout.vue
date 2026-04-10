<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout>
        <GlobalSider class="sider" />
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>
      <a-layout-footer class="footer">
        VisionHub · 记录创作 · 展示灵感 ·
        <a href="https://github.com/suny1798" target="_blank">GitHub</a>
      </a-layout-footer>
    </a-layout>
  </div>

  <a-float-button-group trigger="click" type="primary" :style="{ left: '30px' }">
    <template #icon>
      <QuestionCircleOutlined />
    </template>

    <a-float-button href="https://github.com/suny1798" target="_blank" tooltip="联系站长">
      <template #icon>
        <CommentOutlined />
      </template>
    </a-float-button>
  </a-float-button-group>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
import {
  QuestionCircleOutlined,
  CustomerServiceOutlined,
  CommentOutlined,
} from '@ant-design/icons-vue'
</script>

<style scoped>
#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}

#basicLayout .content {
  padding: 10px 28px;
  background: linear-gradient(to right, #efefef, #fff);
  margin-bottom: 30px;
}

#basicLayout .header {
  background: #fff;
  padding: 0 20px;
  box-shadow: 0 2px 8px #f0f1f2;
}
.sider {
  background: #fff;
  box-shadow: 0 2px 8px #f0f1f2;
  padding-top: 20px;
}
</style>
