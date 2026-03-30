<template>
  <div id="pictureManagePage">
    <div class="container">
      <div class="title">
        <a-space>
          <a-button type="primary" href="/add_picture" target="_blank"> + 创建图片 </a-button>
          <a-button type="primary" href="/add_picture/batch" target="_blank" ghost>
            + 批量创建图片
          </a-button>
        </a-space>
      </div>
      <div class="searchTable">
        <!--搜索表单-->
        <a-form layout="inline" :model="searchParams" @finish="doSearch">
          <a-form-item label="关键词">
            <a-input
              allow-clear
              v-model:value="searchParams.searchText"
              placeholder="请输入关键词"
            />
          </a-form-item>
          <a-form-item label="类型">
            <a-input v-model:value="searchParams.category" placeholder="请选择类型" allow-clear>
            </a-input>
          </a-form-item>
          <a-form-item label="标签">
            <a-select
              v-model:value="searchParams.tags"
              placeholder="请输入标签"
              mode="tags"
              style="min-width: 300px"
              allow-clear
            >
            </a-select>
          </a-form-item>
          <a-form-item label="审核状态" class="statusLabel">
            <a-select
              v-model:value="searchParams.reviewStatus"
              placeholder="请选择"
              allow-clear
              :options="PIC_REVIEW_STATUS_OPTIONS"
            >
              <!--              <a-select-option :value="PIC_REVIEW_STATUS_ENUM.REVIEWING">待审核</a-select-option>-->
              <!--              <a-select-option :value="PIC_REVIEW_STATUS_ENUM.PASS">通过</a-select-option>-->
              <!--              <a-select-option :value="PIC_REVIEW_STATUS_ENUM.REJECT">拒绝</a-select-option>-->
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
          <template v-if="column.dataIndex === 'url'">
            <a-image :src="record.url" style="height: 100px; object-fit: contain" />
          </template>
          <template v-if="column.dataIndex === 'tags'">
            <a-space wrap>
              <a-tag v-for="tag in JSON.parse(record.tags || '[]')" :key="tag">
                {{ tag }}
              </a-tag>
            </a-space>
          </template>
          <template v-if="column.dataIndex === 'picInfo'">
            <div>格式：{{ record.picFormat }}</div>
            <div>宽度：{{ record.picWidth }}</div>
            <div>高度：{{ record.picHeight }}</div>
            <div>宽高比：{{ record.picScale }}</div>
            <div>大小：{{  formatSize(record.picSize)}}</div>
          </template>
          <template v-else-if="column.dataIndex === 'userId'">
            <span>
              <a-tag color="green">{{ record.userId }} </a-tag>
            </span>
          </template>
          <template v-if="column.dataIndex === 'id'">
            <a-tooltip :title="record.id">
              {{ formatId(record.id) }}
            </a-tooltip>
          </template>
          <template v-if="column.dataIndex === 'reviewMessage'">
            <div>
              审核状态：
              <a-tag :color="getReviewColor(record.reviewStatus)">
                {{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}
              </a-tag>
            </div>
            <div>审核信息：{{ record.reviewMessage }}</div>
            <div>审核人：{{ record.reviewerId }}</div>
            <div v-if="record.reviewTime">
              审核时间：{{ dayjs(record.reviewTime).format('YYYY-MM-DD HH:mm') }}
            </div>
          </template>
          <template v-if="column.dataIndex === 'createTime'">
            {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm') }}
          </template>
          <template v-if="column.dataIndex === 'editTime'">
            {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm') }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" :href="`/add_picture?id=` + record.id" target="_blank">
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
            <a-button
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS"
              type="link"
              @click="openReviewModal(record, PIC_REVIEW_STATUS_ENUM.PASS)"
            >
              通过
            </a-button>

            <a-button
              type="link"
              danger
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT"
              @click="openReviewModal(record, PIC_REVIEW_STATUS_ENUM.REJECT)"
            >
              拒绝
            </a-button>
          </template>
        </template>
      </a-table>
    </div>
  </div>
  <a-modal
    v-model:open="reviewModalVisible"
    title="图片审核"
    @ok="submitReview"
    @cancel="reviewModalVisible = false"
  >
    <a-form layout="vertical">
      <a-form-item label="审核结果">
        <a-tag :color="reviewForm.reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? 'green' : 'red'">
          {{ reviewForm.reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '通过' : '拒绝' }}
        </a-tag>
      </a-form-item>

      <a-form-item label="审核备注">
        <a-textarea
          v-model:value="reviewForm.reviewMessage"
          placeholder="请输入审核说明（可选）"
          :rows="4"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import {
  deletePictureUsingPost,
  doPictureReviewUsingPost,
  listPictureByPageUsingPost,
  listPictureVoByPageUsingPost,
  updatePictureUsingPost,
} from '@/api/tupianxiangguanjiekou'
import { type FormInstance, message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getUserVoByIdUsingGet } from '@/api/yonghuxiangguanjiekou.ts'
import router from '@/router'
import {
  PIC_REVIEW_STATUS_ENUM,
  PIC_REVIEW_STATUS_MAP,
  PIC_REVIEW_STATUS_OPTIONS,
} from '@/constants/picture/picture.ts'
import { formatSize } from '@/utils'

const formatId = (id) => {
  if (!id) return ''
  const str = id.toString()
  return str.length > 8 ? `${str.slice(0, 2)}...${str.slice(-2)}` : str
}
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const columns = [
  {
    title: '图片id',
    dataIndex: 'id',
    width: 100,
  },
  {
    title: '图片',
    dataIndex: 'url',
  },
  {
    title: '名称',
    dataIndex: 'name',
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'category',
    width: 80,
  },
  {
    title: '标签',
    dataIndex: 'tags',
    width: 160,
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
  },
  {
    title: '创建人',
    dataIndex: 'userId',
  },
  {
    title: '审核信息',
    dataIndex: 'reviewMessage',
    width: 200,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 120,
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
    width: 120,
  },
  {
    title: '操作',
    key: 'action',
  },
]

//定义数据
const dataList = ref<API.Picture[]>([])
const total = ref(0)

//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
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
  const res = await listPictureByPageUsingPost({
    ...searchParams,
    nullSpaceId: true
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
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败，' + res.data.message)
  }
}
//定于数据
const reviewModalVisible = ref(false)
const reviewForm = ref({
  id: '',
  reviewStatus: 0,
  reviewMessage: '',
})
//打开弹窗
const openReviewModal = (record: API.Picture, reviewStatus: number) => {
  reviewForm.value = {
    id: record.id,
    reviewStatus,
    reviewMessage:
      reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '管理员操作通过' : '管理员操作拒绝',
  }
  reviewModalVisible.value = true
}
//提交审核
const submitReview = async () => {
  const res = await doPictureReviewUsingPost(reviewForm.value)
  if (res.data.code === 0) {
    message.success('审核操作成功')
    reviewModalVisible.value = false
    fetchData()
  } else {
    message.error('审核操作失败，' + res.data.message)
  }
}

const getReviewColor = (status: number) => {
  switch (status) {
    case PIC_REVIEW_STATUS_ENUM.REVIEWING:
      return 'blue' // 待审核
    case PIC_REVIEW_STATUS_ENUM.PASS:
      return 'green' // 通过
    case PIC_REVIEW_STATUS_ENUM.REJECT:
      return 'red' // 拒绝
    default:
      return 'default'
  }
}
</script>

<style scoped>
#pictureManagePage {
  padding: 24px;
}

/* 整体布局 */
.container {
  gap: 24px;
  margin: 0 auto;
}

.statusLabel :deep(.ant-select-selector) {
  min-width: 100px;
}

.title {
  display: flex;
  align-items: center; /* 垂直居中 */
  margin-bottom: 16px; /* 底部间距 */
  text-align: right;
}
</style>
