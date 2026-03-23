<template>
  <div id="userManagePage">
    <div class="container">
      <div class="searchTable">
        <!--搜索表单-->
        <a-form layout="inline" :model="searchParams" @finish="doSearch">
          <a-form-item label="账号">
            <a-input
              allow-clear
              v-model:value="searchParams.userAccount"
              placeholder="请输入账号"
            />
          </a-form-item>
          <a-form-item label="昵称">
            <a-input allow-clear v-model:value="searchParams.userName" placeholder="请输入昵称" />
          </a-form-item>
          <a-form-item label="角色" class="roleLabel">
            <a-select v-model:value="searchParams.userRole" placeholder="请选择角色" allow-clear>
              <a-select-option value="admin">管理员</a-select-option>
              <a-select-option value="svip">超级会员</a-select-option>
              <a-select-option value="fvip">临时会员</a-select-option>
              <a-select-option value="user">普通用户</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item>
            <a-button type="primary" html-type="submit"><SearchOutlined />搜索</a-button>
          </a-form-item>
        </a-form>
      </div>

      <div style="margin-bottom: 16px"></div>
      <!--表格-->
      <a-button class="editable-add-btn" style="margin-bottom: 16px" @click="handleAdd"
        ><UserAddOutlined />新增</a-button
      >
      <a-table
        :columns="columns"
        :data-source="dataList"
        :pagination="pagination"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userAvatar'">
            <a-image :src="record.userAvatar" style="width: 40px" />
          </template>
          <template v-else-if="column.dataIndex === 'userRole'">
            <span v-if="record.userRole === 'admin'">
              <a-tag color="green"> 管理员 </a-tag>
            </span>
            <span v-else-if="record.userRole === 'user'">
              <a-tag color="blue"> 用户 </a-tag>
            </span>
            <span v-else-if="record.userRole === 'svip'">
              <a-tag color="red"> 超级会员 </a-tag>
            </span>
            <span v-else-if="record.userRole === 'fvip'">
              <a-tag color="grey"> 临时会员 </a-tag>
            </span>
          </template>
          <template v-if="column.dataIndex === 'id'">
            <a-tooltip :title="record.id">
              {{ formatId(record.id) }}
            </a-tooltip>
          </template>
          <template v-if="column.dataIndex === 'createTime'">
            {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm') }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button
              style="font-size: 12px"
              type="primary"
              size="small"
              @click="handleEdit(record)"
            >
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
              <a-button style="font-size: 12px" type="dashed" danger size="small">删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </div>
  </div>
  <!--  新增、修改用户弹窗-->
  <a-modal
    v-model:open="addModalVisible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    ok-text="提交"
    cancel-text="取消"
    @ok="submitAdd"
    :confirm-loading="addLoading"
  >
    <a-form :model="addForm" :rules="addRules" ref="addFormRef" layout="vertical">
      <a-form-item label="账号" name="userAccount">
        <a-input v-model:value="addForm.userAccount" :disabled="isEdit" />
      </a-form-item>

      <a-form-item label="昵称" name="userName">
        <a-input v-model:value="addForm.userName" placeholder="请输入昵称" />
      </a-form-item>

      <a-form-item label="简介" name="userProfile">
        <a-input v-model:value="addForm.userProfile" placeholder="请输入简介" />
      </a-form-item>

      <a-form-item label="角色" name="userRole">
        <a-select v-model:value="addForm.userRole">
          <a-select-option value="admin">管理员</a-select-option>
          <a-select-option value="svip">超级会员</a-select-option>
          <a-select-option value="fvip">临时会员</a-select-option>
          <a-select-option value="user">普通用户</a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { UserAddOutlined, SearchOutlined } from '@ant-design/icons-vue'
import {
  addUserUsingPost,
  deleteUserUsingPost,
  listUserVoByPageUsingPost,
  updateUserUsingPost,
} from '@/api/yonghuxiangguanjiekou'
import { type FormInstance, message } from 'ant-design-vue'
import dayjs from 'dayjs'

const formatId = (id) => {
  if (!id) return ''
  const str = id.toString()
  return str.length > 8 ? `${str.slice(0, 4)}...${str.slice(-4)}` : str
}
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '昵称',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '角色',
    dataIndex: 'userRole',
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
const dataList = ref<API.UserVO[]>([])
const total = ref(0)

const addModalVisible = ref(false)
const addLoading = ref(false)
const addFormRef = ref<FormInstance>()

const addForm = reactive<API.UserAddRequest>({
  userAccount: '',
  userName: '',
  userProfile: '',
  userRole: 'user',
})

const addRules = {
  userAccount: [
    { required: true, message: '请输入账号' },
    { min: 4, message: '账号至少4位' },
  ],
  userName: [{ required: true, message: '请输入昵称' }],
  userRole: [{ required: true, message: '请选择角色' }],
}

const handleAdd = () => {
  isEdit.value = false
  editId.value = null

  Object.assign(addForm, {
    userAccount: '',
    userName: '',
    userProfile: '',
    userRole: 'user',
  })

  addModalVisible.value = true
}
const submitAdd = async () => {
  try {
    await addFormRef.value?.validate()
    addLoading.value = true

    let res

    if (isEdit.value) {
      // 编辑
      res = await updateUserUsingPost({
        id: editId.value,
        ...addForm,
      })
    } else {
      // 新增
      res = await addUserUsingPost(addForm)
    }

    if (res.data.code === 0) {
      message.success(isEdit.value ? '修改成功' : '新增成功')
      addModalVisible.value = false
      await fetchData()
    } else {
      message.error((isEdit.value ? '修改失败：' : '新增失败：') + res.data.message)
    }
  } catch (error) {
    // 校验失败
  } finally {
    addLoading.value = false
  }
}
//搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'ascend',
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
  const res = await listUserVoByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取信息失败' + res.data.message)
  }
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
  const res = await deleteUserUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败' + res.data.message)
  }
}

//编辑用户信息

const isEdit = ref(false) // 是否编辑模式
const editId = ref<number | null>(null)

const handleEdit = (record: API.UserVO) => {
  isEdit.value = true
  editId.value = record.id

  // 回填数据
  Object.assign(addForm, {
    userAccount: record.userAccount,
    userName: record.userName,
    userProfile: record.userProfile,
    userRole: record.userRole,
  })

  addModalVisible.value = true
}
</script>

<style scoped>
#userManagePage {
  padding: 24px;
}

.roleLabel :deep(.ant-select-selector) {
  min-width: 110px;
}
</style>
