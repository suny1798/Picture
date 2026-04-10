<template>
  <div class="PictureListPage">
    <!--  图片列表-->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 3, xl: 4, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <a-card hoverable class="picture-card" @click="doClickPicture(picture)">
            <template #cover>
              <div class="img-wrapper">
                <img
                  class="img"
                  :alt="picture.name"
                  :src="picture.thumbnailUrl ?? picture.url"
                  loading="lazy"
                />

                <!-- 悬浮层 -->
                <div class="overlay">
                  <div class="content">
                    <!-- 标题 -->
                    <div class="title">
                      {{ picture.name }}
                    </div>

                    <!-- 标签 -->
                    <div class="tag-container">
                      <a-tag color="green">
                        {{ picture.category ?? '默认' }}
                      </a-tag>
                      <a-tag v-for="tag in picture.tags" :key="tag" color="blue">
                        {{ tag }}
                      </a-tag>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>
<script lang="ts" setup>
import router from '@/router'
import ShareModal from '@/components/ShareModal.vue'

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  canEdit?: boolean
  canDelete?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  canEdit: false,
  canDelete: false,
})
//点击图片函数
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}
</script>
<style scoped>

.picture-card {
  overflow: hidden;
}

.img-wrapper {
  position: relative;
}

.img {
  width: 100%;
  height: 250px;
  object-fit: cover;
  display: block;
  transition: transform 0.3s;
}

/* hover 放大 */
.img-wrapper:hover .img {
  transform: scale(1.05);
}

/* 遮罩层 */
.overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(150, 131, 131, 0.75), rgba(0, 0, 0, 0.1));
  opacity: 0;
  transition: opacity 0.3s ease;

  display: flex;
  align-items: flex-end;
}

/* hover 显示 */
.img-wrapper:hover .overlay {
  opacity: 1;
}

/* 内容区 */
.content {
  padding: 10px;
  width: 100%;
  color: #fff;

  transform: translateY(20px);
  transition: all 0.3s;
}

/* hover 上浮 */
.img-wrapper:hover .content {
  transform: translateY(0);
}

/* 标题 */
.title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;

  /* 超出省略 */
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 标签 */
.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

#homePage :deep(.ant-list-item) {
  margin-bottom: 32px; /* 控制纵向间距 */
}
</style>
