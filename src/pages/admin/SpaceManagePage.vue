<template>
  <div id="spaceManagePage">
    <div class="container">
      <div class="title" style="display: flex; justify-content: space-between; align-items: center">
        <!-- 左边 -->
        <div>
          <a-button type="primary" href="/add_space" target="_blank"> + 创建空间 </a-button>
        </div>
        <!-- 右边 -->
        <a-space>
          <a-button type="primary" ghost href="/space_analyze?queryPublic=1" target="_blank">
            分析公共图库
          </a-button>
          <a-button type="primary" ghost href="/space_analyze?queryAll=1" target="_blank">
            分析全空间
          </a-button>
        </a-space>
      </div>
      <div class="searchTable">
        <!--搜索表单-->
        <a-form layout="inline" :model="searchParams" @finish="doSearch">
          <a-form-item label="空间名称">
            <a-input
              style="min-width: 300px"
              allow-clear
              v-model:value="searchParams.spaceName"
              placeholder="请输入空间名称"
            />
          </a-form-item>
          <a-form-item label="标签">
            <a-input
              v-model:value="searchParams.userId"
              placeholder="请输入用户ID"
              style="min-width: 180px"
              allow-clear
            >
            </a-input>
          </a-form-item>
          <a-form-item label="空间类型">
            <a-select
              v-model:value="searchParams.spaceType"
              placeholder="请选择"
              allow-clear
              style="min-width: 180px"
              :options="SPACE_TYPE_OPTIONS"
            >
            </a-select>
          </a-form-item>
          <a-form-item label="空间级别">
            <a-select
              v-model:value="searchParams.spaceLevel"
              placeholder="请选择"
              allow-clear
              style="min-width: 180px"
              :options="SPACE_LEVEL_OPTIONS"
            >
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit"><SearchOutlined />搜索</a-button>
          </a-form-item>
        </a-form>
      </div>

      <div style="margin-bottom: 16px"></div>
      <!--表格-->
      <a-table
        :columns="columns"
        :data-source="dataList"
        :pagination="pagination"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'id'">
            <span>
              <a-tag color="default">{{ record.id }} </a-tag>
            </span>
          </template>
          <template v-if="column.dataIndex === 'spaceLevel'">
            <a-tag :color="getReviewColor(record.spaceLevel)">
              {{ SPACE_LEVEL_MAP[record.spaceLevel] }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'spaceType'">
            <a-tag>
              {{ SPACE_TYPE_MAP[record.spaceType] }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'spaceUseInfo'">
            <div>大小：{{ formatSize(record.totalSize) }} / {{ formatSize(record.maxSize) }}</div>
            <div>数量：{{ record.totalCount }} / {{ record.maxCount }}</div>
          </template>
          <template v-if="column.dataIndex === 'userId'">
            <span>
              <a-tag>{{ record.userId }} </a-tag>
            </span>
          </template>
          <template v-if="column.dataIndex === 'createTime'">
            {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm') }}
          </template>
          <template v-if="column.dataIndex === 'editTime'">
            {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm') }}
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" :href="`/space_analyze?spaceId=${record.id}`" target="_blank">
              分析
            </a-button>

            <a-button type="link" :href="`/add_space?id=` + record.id" target="_blank">
              编辑
            </a-button>
            <a-popconfirm
              placement="left"
              ok-text="确定"
              cancel-text="取消"
              @confirm="doDelete(record.id)"
            >
              <template #title>
                <p>确定删除该条信息吗？</p>
              </template>
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { deleteSpaceUsingPost, listSpaceByPageUsingPost } from '@/api/kongjianxiangguanjiekou'
import { type FormInstance, message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getUserVoByIdUsingGet } from '@/api/yonghuxiangguanjiekou.ts'
import router from '@/router'
import {
  SPACE_LEVEL_ENUM,
  SPACE_LEVEL_MAP,
  SPACE_LEVEL_OPTIONS,
  SPACE_TYPE_MAP,
  SPACE_TYPE_OPTIONS,
} from '@/constants/space/space.ts'
import { formatSize } from '@/utils'

const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
  },
  {
    title: '空间类型',
    dataIndex: 'spaceType',
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUseInfo',
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

//定义数据
const dataList = ref<API.Space[]>([])
const total = ref(0)

//搜索条件
const searchParams = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: any) => `共${total}条`,
  }
})

const fetchData = async () => {
  const res = await listSpaceByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取信息失败，' + res.data.message)
  }
  const user = await getUserVoByIdUsingGet({
    id: dataList.value.userId,
  })
}

onMounted(() => {
  fetchData()
})

//搜索
const doSearch = () => {
  //重置页码
  searchParams.current = 1
  fetchData()
}
//删除操作
const doDelete = async (id: number) => {
  const res = await deleteSpaceUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败' + res.data.message)
  }
}

const getReviewColor = (status: number) => {
  switch (status) {
    case SPACE_LEVEL_ENUM.COMMON:
      return 'blue' // 普通
    case SPACE_LEVEL_ENUM.PROFESSIONAL:
      return 'green' // 专业
    case SPACE_LEVEL_ENUM.FLAGSHIP:
      return 'red' // 旗舰
    default:
      return 'default'
  }
}
</script>

<style scoped>
#spaceManagePage {
  padding: 24px;
}

/* 整体布局 */
.container {
  gap: 24px;
  margin: 0 auto;
}

.title {
  display: flex;
  align-items: center; /* 垂直居中 */
  margin-bottom: 16px; /* 底部间距 */
  text-align: right;
}
</style>
