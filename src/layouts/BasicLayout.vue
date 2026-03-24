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
        <a href="https://github.com/suny1798" target="_blank"> 智能云图库 </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
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
  padding: 0 10px;
  box-shadow: 0 2px 8px #f0f1f2;
}
.sider {
  background: #fff;
  box-shadow: 0 2px 8px #f0f1f2;
  padding-top: 20px;
}
</style>
