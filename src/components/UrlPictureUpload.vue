<template>
  <div class="urlPictureUpload">
    <a-input-group compact>
      <a-input
        placeholder="请输入图片地址"
        v-model:value="fileUrl"
        style="width: calc(100% - 120px); height: 50px"
      />
      <a-button type="primary" style="width: 120px; height: 50px" :loading="loading" @click="handleUpload"
        >点击上传</a-button
      >
    </a-input-group>
    <div class="imgbox">

      <a-image v-if="picture?.url" :src="picture?.url" alt="avatar"></a-image>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import { uploadPictureByUrlUsingPost, uploadPictureUsingPost } from '@/api/tupianxiangguanjiekou.ts'

interface Props {
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void
}
const fileUrl = ref<string>()

const props = defineProps<Props>()
const loading = ref(false)
/**
 * 上传图片
 * @param file
 */
const handleUpload = async () => {
  try {
    const params: API.PictureUploadRequest = { fileUrl: fileUrl.value }
    if (props.picture) {
      params.id = props.picture.id
    }
    loading.value = true
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      loading.value = false
      props.onSuccess?.(res.data.data)
    } else {
      message.error('上传失败' + res.data.message)
      loading.value = false
    }
  } catch (e) {
    console.log('图片上传失败', e)
    message.error('上传失败' + e)
    loading.value = false
  }
}
</script>

<style scoped>
.urlPictureUpload{
  margin-bottom: 16px;

}
.urlPictureUpload img {
  max-height: 380px;
  max-width: 100%;
}
.imgbox{
  text-align: center;
  margin-top: 16px;
}

</style>
