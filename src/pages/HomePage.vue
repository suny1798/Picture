<template>
  <div id="homePage">
    <!-- 搜索框 -->
    <div class="search-section">
      <div class="search-bar">
        <a-input-search
          placeholder="从海量图片中搜索"
          v-model:value="searchParams.searchText"
          size="small"
          @search="doSearch"
        >
          <template #enterButton>
            <span>
              <SearchOutlined />
              搜索一下
            </span>
          </template>
        </a-input-search>
      </div>
    </div>
    <!--分类和标签筛选-->
    <a-tabs v-model:activeKey="selectCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部"></a-tab-pane>
      <a-tab-pane v-for="category in categoryList" :key="category" :tab="category"></a-tab-pane>
    </a-tabs>

    <div class="tag-bar">
      <span style="margin-right: 8px">标签：</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectTag[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>

    <!--  图片列表-->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 3, xl: 4, xxl: 6 }"
      :data-source="dataList"
      :pagination="pagination"
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
import { computed, onMounted, reactive, ref } from 'vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import {
  listPictureByPageUsingPost,
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
  listPictureVoCacheByPageUsingPost,
} from '@/api/tupianxiangguanjiekou.ts'
import { getUserVoByIdUsingGet } from '@/api/yonghuxiangguanjiekou.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

interface DataItem {
  title: string
}

//定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

//分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    onChange: (page: number, pageSize: number) => {
      searchParams.current = page
      searchParams.pageSize = pageSize
      fetchData()
    },
  }
})

const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    ...searchParams,
    tags: [],
  }
  if (selectCategory.value !== 'all') {
    params.category = selectCategory.value
  }
  selectTag.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagList.value[index])
    }
  })
  // const res = await listPictureVoByPageUsingPost(params)
  //使用缓存
  const res = await listPictureVoCacheByPageUsingPost(params)
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

onMounted(() => {
  fetchData()
})

const doSearch = () => {
  searchParams.current = 1
  fetchData()
}
//标签和分类列表
const categoryList = ref<String[]>([])
const tagList = ref<String[]>([])
const selectCategory = ref<String>('all')
const selectTag = ref<String[]>([])

const getTagAndCategory = async () => {
  try {
    const res = await listPictureTagCategoryUsingGet()
    if (res.data.code === 0 && res.data.data) {
      tagList.value = res.data.data.tagList ?? []
      categoryList.value = res.data.data.categoryList ?? []
    } else {
      message.error('获取标签分类列表失败' + res.data.message)
    }
  } catch (e) {
    console.log('getTagAndCategory error', e)
  }
}

//点击图片函数
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

onMounted(() => {
  getTagAndCategory()
})
</script>
<style scoped>
#homePage {
  margin-bottom: 16px;
}
#homePage .search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
}
/* 搜索背景区 */
.search-section {
  height: 150px;
  border-radius: 12px;
  margin-bottom: 24px;

  /* 背景（可以换图） */
  background: linear-gradient(135deg, #eef2ff, #f8fafc);
  /* 如果你有图片可以用👇 */
  /* background: url('/your-bg.jpg') center/cover no-repeat; */

  display: flex;
  align-items: center;
  justify-content: center;
}

/* 搜索框容器 */
.search-bar {
  width: 100%;
  max-width: 600px;
}

/* 输入框美化 */
.search-bar :deep(.ant-input-search) {
  overflow: hidden;
}

/* 输入框 */
.search-bar :deep(.ant-input) {
  height: 48px;
  font-size: 16px;
  padding-left: 20px;
}

/* 按钮 */
.search-bar :deep(.ant-input-search-button) {
  height: 48px;
  border-radius: 0 999px 999px 0;
}

#homePage .tag-bar {
  margin-bottom: 16px;
}
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
