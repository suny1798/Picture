<template>
  <div id="picturesearchform">
    <div class="container">
      <div class="searchTable">
        <!--搜索表单-->
        <a-form layout="inline" :model="searchParams" name="searchForm" @finish="doSearch">
          <a-form-item label="关键词">
            <a-input
              allow-clear
              v-model:value="searchParams.searchText"
              placeholder="请输入关键词"
            />
          </a-form-item>
          <a-form-item label="名称">
            <a-input
              allow-clear
              v-model:value="searchParams.name"
              placeholder="请输入名称"
              style="width: 150px"
            />
          </a-form-item>
          <a-form-item label="简介">
            <a-input
              allow-clear
              v-model:value="searchParams.introduction"
              placeholder="请输入简介"
              style="width: 150px"
            />
          </a-form-item>
          <a-form-item label="类别">
            <a-select
              v-model:value="searchParams.category"
              placeholder="请选择类别"
              style="width: 150px"
              :options="categoryOptions"
              allow-clear
            >
            </a-select>
          </a-form-item>
          <a-form-item label="标签">
            <a-select
              v-model:value="searchParams.tags"
              placeholder="请输入标签"
              :options="tagsOptions"
              mode="tags"
              style="min-width: 150px"
              allow-clear
            >
            </a-select>
          </a-form-item>
          <a-form-item label="日期" name="dataRange">
            <a-range-picker
              style="width: 300px"
              v-model:value="dataRange"
              show-time
              :placeholder="['开始日期', '结束日期']"
              format="YYYY/MM/DD HH:mm"
              :presets="rangePresets"
              @change="onRangeChange"
            />
          </a-form-item>
          <a-form-item label="格式" name="pictureForm">
            <a-select v-model:value="searchParams.picFormat" placeholder="请选择格式" allow-clear>
              <a-select-option value="jpg">JPG</a-select-option>
              <a-select-option value="png">PNG</a-select-option>
              <a-select-option value="jpeg">JPEG</a-select-option>
              <a-select-option value="webp">WEBP</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit"><SearchOutlined />搜索</a-button>
              <a-button html-type="reset" @click="doClear"><ClearOutlined />重置</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { SearchOutlined, ClearOutlined } from '@ant-design/icons-vue'
import { listPictureTagCategoryUsingGet } from '@/api/tupianxiangguanjiekou'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

interface Props {
  onSearch?: (searchParams: API.PictureQueryRequest) => void
}

const props = defineProps<Props>()

//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({})

//搜索
const doSearch = () => {
  //
  props.onSearch?.(searchParams)
}

const dataRange = ref<[]>([])

const onRangeChange = (dates: any, dateStrings: string[]) => {
  if (dates?.length >= 2) {
    searchParams.startEditTime = dates[0].toDate()
    searchParams.endEditTime = dates[1].toDate()
  } else {
    searchParams.startEditTime = undefined
    searchParams.endEditTime = undefined
  }
}

const rangePresets = ref([
  { label: '昨天', value: [dayjs().add(-1, 'd'), dayjs()] },
  { label: '过去 7 天', value: [dayjs().add(-7, 'd'), dayjs()] },
  { label: '过去 1 个月', value: [dayjs().add(-30, 'd'), dayjs()] },
  { label: '过去 90 天', value: [dayjs().add(-90, 'd'), dayjs()] },
])

const doClear = () => {
  // 取消所有对象的值
  Object.keys(searchParams).forEach((key) => {
    searchParams[key] = undefined
  })
  dataRange.value = []
  props.onSearch?.(searchParams)
}

const categoryOptions = ref<String[]>([])
const tagsOptions = ref<String[]>([])

const getTagAndCategory = async () => {
  try {
    const res = await listPictureTagCategoryUsingGet()
    if (res.data.code === 0 && res.data.data) {
      tagsOptions.value = (res.data.data.tagList ?? []).map((data: String) => {
        return {
          value: data,
          label: data,
        }
      })
      categoryOptions.value = (res.data.data.categoryList ?? []).map((data: String) => {
        return {
          value: data,
          label: data,
        }
      })
    } else {
      message.error('操作失败' + res.data.message)
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
.statusLabel :deep(.ant-select-selector) {
  min-width: 100px;
}

#picturesearchform .ant-form-item {
  margin-top: 8px;
}
</style>
