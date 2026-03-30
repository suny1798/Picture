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
        <!--添加成员表单-->
        <a-form layout="inline" :model="formData" @finish="handleSubmit">
          <a-form-item label="用户 ID" name="userId">
            <a-input
              style="min-width: 300px"
              allow-clear
              v-model:value="formData.userId"
              placeholder="请输入用户ID"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit"><SearchOutlined />添加用户</a-button>
          </a-form-item>
        </a-form>
      </div>

      <div style="margin-bottom: 16px"></div>
      <!--表格-->
      <a-table :columns="columns" :data-source="dataList">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userInfo'">
            <a-image :src="record.user?.userAvatar" style="width: 40px" />
          </template>
          <template v-if="column.dataIndex === 'spaceRole'">
            <a-select
              v-model:value="record.spaceRole"
              :options="SPACE_ROLE_OPTIONS"
              @change="(value) => editSpaceRole(value, record)"
            />
          </template>
          <template v-if="column.dataIndex === 'createTime'">
            {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm') }}
          </template>

          <template v-if="column.key === 'action'">
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
import {
  addSpaceUsingPost,
  deleteSpaceUsingPost,
  listSpaceByPageUsingPost,
  updateSpaceUsingPost,
} from '@/api/kongjianxiangguanjiekou'
import { type FormInstance, message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getUserVoByIdUsingGet } from '@/api/yonghuxiangguanjiekou.ts'
import router from '@/router'
import {
  SPACE_LEVEL_ENUM,
  SPACE_LEVEL_MAP,
  SPACE_LEVEL_OPTIONS,
  SPACE_ROLE_OPTIONS,
} from '@/constants/space/space.ts'
import { formatSize } from '@/utils'
import {
  addSpaceUserUsingPost,
  deleteSpaceUserUsingPost,
  editSpaceUserUsingPost,
  listSpaceUserUsingPost,
} from '@/api/kongjianyonghuxiangguanjiekou.ts'

const columns = [
  {
    title: '用户',
    dataIndex: 'userInfo',
  },
  {
    title: '角色',
    dataIndex: 'spaceRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

//定义数据
const dataList = ref<API.SpaceUserVO[]>([])

interface Props {
  id: number
}
const props = defineProps<Props>()

const fetchData = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await listSpaceUserUsingPost({
    spaceId,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取信息失败，' + res.data.message)
  }
}

onMounted(() => {
  fetchData()
})
//添加成员表单
const formData = reactive<API.SpaceUserAddRequest>({})

//删除操作
const doDelete = async (id: number) => {
  const res = await deleteSpaceUserUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败' + res.data.message)
  }
}

const editSpaceRole = async (value, record) => {
  const res = await editSpaceUserUsingPost({
    id: record.id,
    spaceRole: value,
  })
  if (res.data.code === 0) {
    message.success('修改成功')
  } else {
    message.error('修改失败，' + res.data.message)
  }
}
//创检成员
const handleSubmit = async (values: any) => {
  const spaceId = props.id
  if (!spaceId) {
    return
  } else {
    const res = await addSpaceUserUsingPost({
      spaceId,
      ...formData
    })
    if (res.data.code === 0) {
      message.success('添加成功')
      fetchData()
    } else {
      message.error('更新失败，' + res.data.message)
    }
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
