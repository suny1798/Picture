<template>
  <div id="homePage">
    <!-- 搜索框 -->
    <div class="search-section">
      <div class="search-bar">
        <a-input-search
          placeholder="从海量作品中搜索"
          v-model:value="searchParams.searchText"
          size="small"
          @search="doSearch"
        >
          <template #enterButton>
            <span>
              <SearchOutlined />
              探索
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
      <span style="margin-right: 8px">热门标签：</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in topTagList"
          :key="tag"
          v-model:checked="selectTag[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>

    <!--  图片列表-->
    <PictureListPage :data-list="dataList" :loading="loading"></PictureListPage>

    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    />
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
  listPictureVoCacheByPageUsingPost,
} from '@/api/tupianxiangguanjiekou.ts'
import { message } from 'ant-design-vue'
import PictureListPage from '@/components/PictureListPage.vue'
import ImageCropper from '@/components/ImageCropper.vue'

//定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

const topTagList = computed(() => {
  return tagList.value.slice(0, 20)
})

//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

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
const categoryList = ref<string[]>([])
const tagList = ref<string[]>([])
const selectCategory = ref<string>('all')
const selectTag = ref<string[]>([])

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

onMounted(() => {
  getTagAndCategory()
})
</script>
<style scoped>
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
</style>
