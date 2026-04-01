<template>
  <div id="addPicturePage">
    <div class="container">
      <!--      <h2 style="margin-bottom: 16px">{{ route.query?.id ? '修改图片' : '创建图片' }}</h2>-->
      <a-typography-paragraph v-if="spaceId" type="secondary">
        作品将保存至<a :href="`/space/${spaceId}`" target="_blank">作品集, 点击查看</a>
      </a-typography-paragraph>
      <a-tabs v-model:activeKey="uploadType" centered>
        <!--图片上传组件-->
        <a-tab-pane key="file" tab="文件上传">
          <picture-upload :picture="picture" :spaceId="spaceId" :on-success="onSuccess" />
        </a-tab-pane>
        <a-tab-pane key="url" tab="Url上传" force-render>
          <url-picture-upload :picture="picture" :spaceId="spaceId" :on-success="onSuccess" />
        </a-tab-pane>
      </a-tabs>
      <div v-if="picture" class="edit-bar">
        <a-button @click="doEditPicture"><EditOutlined />编辑图片</a-button>
        <ImageCropper
          ref="imageCropperRef"
          :imageUrl="picture.url"
          :picture="picture"
          :spaceId="spaceId"
          :space="space"
          :onSuccess="onCropSuccess"
        />
      </div>

      <!--图片信息表单-->
      <a-form
        v-if="picture"
        name="pictureForm"
        layout="vertical"
        :model="pictureForm"
        @finish="handleSubmit"
      >
        <a-form-item label="名称" name="name">
          <a-input allow-clear v-model:value="pictureForm.name" placeholder="请输入图片名称" />
        </a-form-item>
        <a-form-item label="简介" name="introduction">
          <a-textarea
            allow-clear
            v-model:value="pictureForm.introduction"
            placeholder="请输入图片介绍"
            :auto-size="{ minRows: 2, maxRows: 4 }"
          />
        </a-form-item>
        <a-form-item label="分类" name="category">
          <a-auto-complete
            v-model:value="pictureForm.category"
            placeholder="请输入分类"
            :options="categoryOptions"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-select
            v-model:value="pictureForm.tags"
            placeholder="请选择或自定义标签"
            mode="tags"
            :options="tagsOptions"
            allow-clear
          >
          </a-select>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%"
            ><UploadOutlined />{{ route.query?.id ? '修改图片' : '创建图片' }}</a-button
          >
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { computed, onMounted, reactive, ref, watchEffect } from 'vue'
import { EditOutlined, SearchOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { userLoginUsingPost } from '@/api/yonghuxiangguanjiekou.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/tupianxiangguanjiekou.ts'
import { useRoute } from 'vue-router'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import ImageCropper from '@/components/ImageCropper.vue'
import { SPACE_TYPE_MAP } from '@/constants/space/space.ts'
import { getSpaceVoByIdUsingGet } from '@/api/kongjianxiangguanjiekou.ts'
// 空间 id
const spaceId = computed(() => {
  return route.query?.spaceId
})

const uploadType = ref<'file' | 'url'>('file')
const picture = ref<API.PictureVO>()

const pictureForm = reactive<API.PictureEditRequest>({})
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

const handleSubmit = async (values: any) => {
  const pictureId = picture.value?.id
  if (!pictureId) {
    return
  }
  try {
    const res = await editPictureUsingPost({
      id: pictureId,
      spaceId: spaceId.value,
      ...values,
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('操作成功！')
      await router.push({
        path: `/`,
        replace: true,
      })
    } else {
      message.error('操作失败' + res.data.message)
    }
  } catch (e) {
    message.error('操作失败' + e)
  }
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
const route = useRoute()

// 获取老数据
const getOldPicture = async () => {
  // 获取数据
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({
      id: id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
    }
  }
}
// 图片编辑弹窗引用
const imageCropperRef = ref()

// 编辑图片
const doEditPicture = () => {
  if (imageCropperRef.value) {
    imageCropperRef.value.openModal()
  }
}

// 编辑成功事件
const onCropSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

onMounted(() => {
  getOldPicture()
})

const space = ref<API.SpaceVO>()

// 获取空间信息
const fetchSpace = async () => {
  // 获取数据
  if (spaceId.value) {
    const res = await getSpaceVoByIdUsingGet({
      id: spaceId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    }
  }
}

watchEffect(() => {
  fetchSpace()
})
</script>

<style scoped>
#addPicturePage {
}
.container {
  gap: 24px;
  max-width: 700px;
  margin: 0 auto;
}
#addPicturePage .edit-bar {
  text-align: center;
  margin: 16px 0;
}
</style>
