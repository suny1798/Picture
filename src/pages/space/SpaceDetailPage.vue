<template>
  <div id="space-detail-page">
    <a-row :gutter="[16, 16]">
      <a-col :sm="24" :md="24" :xl="24">
        <a-card title="图片搜索" style="margin-bottom: 16px">
          <picture-search-form :onSearch="onSearch"></picture-search-form>
        </a-card>
      </a-col>
    </a-row>
    <a-row :gutter="[16, 16]">
      <!-- 空间展示区 -->
      <a-col :sm="24" :md="16" :xl="19">
        <a-card class="imageshow">
          <!-- 自定义标题 -->
          <template #title>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span></span>
              <span>{{ space.spaceName }} &nbsp; [{{ SPACE_TYPE_MAP[space.spaceType] }}]</span>
              <a-progress
                type="circle"
                :size="40"
                :percent="
                  space.maxSize ? Number(((space?.totalSize / space?.maxSize) * 100).toFixed(2)) : 0
                "
              />
            </div>
          </template>
          <div class="imgshow">
            <!--  图片列表-->
            <PictureListPage
              :data-list="dataList"
              :loading="loading"
              :canEdit="canEditPicture"
              :canDelete="canDeletePicture"
            />
            <a-pagination
              style="text-align: right"
              v-model:current="searchParams.current"
              v-model:pageSize="searchParams.pageSize"
              :total="total"
              @change="onPageChange"
            />
          </div>
        </a-card>
      </a-col>
      <!-- 空间信息区 -->
      <a-col :sm="24" :md="8" :xl="5">
        <a-card title="空间信息">
          <a-descriptions :column="1">
            <a-descriptions-item label="创建者">
              <a-space>
                <a-avatar :size="24" :src="space.user?.userAvatar" />
                <div>{{ space.user?.userName }}</div>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="空间名称">
              {{ space.spaceName ?? '默认空间' }}
            </a-descriptions-item>
            <a-descriptions-item label="空间级别">
              <a-tag :color="getColor(space.spaceLevel)">
                {{ SPACE_LEVEL_MAP[space.spaceLevel] }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="大小">
              <span> {{ formatSize(space.totalSize) }} / {{ formatSize(space.maxSize) }} </span>
            </a-descriptions-item>
            <a-descriptions-item label="容量">
              <!--              <a-progress type="circle" :percent="30" :size="40" />-->
              <span> {{ total }} / {{ space.maxCount }} </span>
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              {{ dayjs(space.createTime).format('YYYY-MM-DD HH:mm') }}
            </a-descriptions-item>
          </a-descriptions>
          <a-button
            v-if="canManageSpaceUser"
            type="primary"
            ghost
            :icon="h(BarChartOutlined)"
            :href="`/space_analyze?spaceId=${id}`"
            target="_blank"
          >
            空间分析
          </a-button>
        </a-card>
        <a-card v-if="canManageSpaceUser" title="空间管理" style="margin-top: 16px">
          <a-space wrap>
            <a-button
              v-if="canManageSpaceUser"
              type="primary"
              :href="`/spaceUserManage/${id}`"
              target="_blank"
              style="margin: 0 auto"
              >成员管理
              <template #icon>
                <TeamOutlined />
              </template>
            </a-button>
            <a-button
              v-if="canUploadPicture"
              type="primary"
              :href="`/add_picture?spaceId=${id}`"
              target="_blank"
              style="margin: 0 auto"
              >上传图片
              <template #icon>
                <UploadOutlined />
              </template>
            </a-button>

            <a-button
              v-if="canEditPicture"
              ghost
              type="primary"
              :href="`/add_space?id=` + space.id"
              target="_blank"
            >
              <EditOutlined />编辑
            </a-button>
            <a-popconfirm
              v-if="canDeletePicture"
              placement="left"
              ok-text="确定"
              cancel-text="取消"
              @confirm="doDelete(<number>space.id)"
            >
              <template #title>
                <p>确定删除空间吗？</p>
              </template>
              <a-button danger> <DeleteOutlined />删除</a-button>
            </a-popconfirm>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, onMounted, reactive, ref, watch, watchEffect } from 'vue'
import {
  deleteSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceVoByPageUsingPost,
} from '@/api/kongjianxiangguanjiekou.ts'
import { message } from 'ant-design-vue'
import {
  DownloadOutlined,
  EditOutlined,
  DeleteOutlined,
  UploadOutlined,
  BarChartOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'
import { downloadImage, formatSize } from '@/utils'
import { useLoginUserStore } from '@/stores/user.ts'
import router from '@/router'
import {
  SPACE_LEVEL_ENUM,
  SPACE_LEVEL_MAP,
  SPACE_PERMISSION_ENUM,
  SPACE_TYPE_MAP,
} from '@/constants/space/space.ts'
import PictureListPage from '@/components/PictureListPage.vue'
import { listPictureVoCacheByPageUsingPost } from '@/api/tupianxiangguanjiekou.ts'
import dayjs from 'dayjs'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
//获取路由参数
interface Props {
  id: string | number
}

const props = defineProps<Props>()
const space = ref<API.SpaceVO>({})

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

// 获取老数据
const fetchSpaceDetail = async () => {
  // 获取数据
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间信息失败，' + res.data.message)
    }
  } catch (e) {
    message.error('获取空间信息失败，' + e)
  }
}

onMounted(() => {
  fetchSpaceDetail()
})

//定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

//搜索条件
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 18,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }
  console.log(params)
  try {
    const res = await listPictureVoCacheByPageUsingPost(params)
    if (res.data.data && res.data.code === 0) {
      dataList.value = res.data.data.records ?? []
      total.value = res.data.data.total ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (e) {
    message.error('获取数据失败，' + e)
  }
  loading.value = false
}

onMounted(() => {
  fetchData()
})

const getColor = (status: number) => {
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

//删除操作
const doDelete = async (id: number) => {
  const res = await deleteSpaceUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    router.push('/my_space')
  } else {
    message.error('删除失败，' + res.data.message)
  }
}

//搜索
const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  fetchData()
}

watch(
  () => props.id,
  () => {
    fetchSpaceDetail()
    fetchData()
  },
)
</script>
<style scoped>
.imgshow {
  text-align: center;
}
</style>
