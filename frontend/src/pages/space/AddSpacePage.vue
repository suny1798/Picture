<template>
  <div id="addSpacePage">
    <div class="container">
      <h2 style="margin-bottom: 16px">
        {{ route.query?.id ? '修改' : '创建' }} {{ SPACE_TYPE_MAP[spaceType] }}
      </h2>

      <!--空间信息表单-->
      <a-form name="spaceForm" layout="vertical" :model="spaceForm" @finish="handleSubmit">
        <a-form-item label="空间名称" name="spaceName">
          <a-input allow-clear v-model:value="spaceForm.spaceName" placeholder="请输入空间名称" />
        </a-form-item>

        <a-form-item label="空间级别" name="spaceLevel">
          <a-select
            v-model:value="spaceForm.spaceLevel"
            placeholder="请选择空间级别"
            allow-clear
            style="min-width: 180px"
            :options="SPACE_LEVEL_OPTIONS"
          >
          </a-select>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading"
            ><UploadOutlined />{{ route.query?.id ? '修改空间' : '创建空间' }}</a-button
          >
        </a-form-item>
      </a-form>
      <a-card title="空间级别介绍">
        <a-typography-paragraph>
          * 目前仅支持开通普通版，如需升级空间，请联系
          <a href="https://github.com/suny1798" target="_blank">站长</a>。
        </a-typography-paragraph>
        <a-typography-paragraph v-for="spaceLevel in spaceLevelList">
          <a-tag :color="getReviewColor(spaceLevel.value)">
            {{ spaceLevel.text }}
          </a-tag>
          ： 大小 {{ formatSize(spaceLevel.maxSize) }}， 数量
          {{ spaceLevel.maxCount }}
        </a-typography-paragraph>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { SearchOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import router from '@/router'
import {
  addSpaceUsingPost,
  editSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet,
  updateSpaceUsingPost,
} from '@/api/kongjianxiangguanjiekou.ts'
import { useRoute } from 'vue-router'
import {
  SPACE_LEVEL_ENUM,
  SPACE_LEVEL_OPTIONS,
  SPACE_TYPE_ENUM,
  SPACE_TYPE_MAP,
} from '@/constants/space/space.ts'
import { formatSize } from '@/utils'

const space = ref<API.SpaceVO>()
const spaceForm = reactive<API.SpaceAddRequest | API.SpaceEditRequest>({})

const loading = ref(false)

//空间类别 默认为私有空间
const spaceType = computed(() => {
  if (route.query?.type) {
    return Number(route.query.type)
  } else {
    return SPACE_TYPE_ENUM.PRIVATE
  }
})

const handleSubmit = async (values: any) => {
  const spaceId = space.value?.id
  loading.value = true
  let res
  if (spaceId) {
    res = await updateSpaceUsingPost({
      id: spaceId,
      ...spaceForm,
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('更新成功！')
      //跳转到空间详情页
      await router.push({
        path: `/my_space`,
        replace: true,
      })
    } else {
      message.error('更新失败，' + res.data.message)
    }
  } else {
    res = await addSpaceUsingPost({
      ...spaceForm,
      spaceType: spaceType.value,
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('创建成功！')
      //跳转到空间详情页
      await router.push({
        path: `/space/${res.data.data}`,
        replace: true,
      })
    } else {
      message.error('创建失败，' + res.data.message)
    }
  }
  loading.value = false
}

const route = useRoute()

// 获取老数据
const getOldSpace = async () => {
  // 获取数据
  const id = route.query?.id
  if (id) {
    const res = await getSpaceVoByIdUsingGet({
      id: id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      space.value = data
      spaceForm.spaceName = data.spaceName
      spaceForm.spaceLevel = data.spaceLevel
    }
  }
}
const spaceLevelList = ref<API.SpaceLevel[]>([])

// 获取空间级别
const fetchSpaceLevelList = async () => {
  const res = await listSpaceLevelUsingGet()
  if (res.data.code === 0 && res.data.data) {
    spaceLevelList.value = res.data.data
  } else {
    message.error('加载空间级别失败，' + res.data.message)
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
onMounted(() => {
  fetchSpaceLevelList()
})

onMounted(() => {
  getOldSpace()
})
</script>

<style scoped>
#addSpacePage {
}
.container {
  gap: 24px;
  max-width: 700px;
  margin: 0 auto;
}
</style>
