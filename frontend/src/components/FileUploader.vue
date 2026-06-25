<template>
  <div class="file-uploader">
    <el-upload
      ref="uploadRef"
      drag
      :auto-upload="false"
      :limit="1"
      accept=".pdf,.doc,.docx,.txt"
      :before-upload="handleBeforeUpload"
      @change="handleFileChange"
    >
      <el-icon class="upload-icon" :size="48"><UploadFilled /></el-icon>
      <div class="upload-text">
        <p>将简历文件拖拽到此处，或 <em>点击选择文件</em></p>
      </div>
      <template #tip>
        <div class="upload-tip">
          支持 PDF、Word（.doc/.docx）、TXT 格式，文件大小不超过 10MB
        </div>
      </template>
    </el-upload>

    <div v-if="selectedFile" class="file-info">
      <el-alert
        :title="'已选择文件：' + selectedFile.name"
        type="success"
        :closable="false"
        show-icon
      />
      <div class="upload-action">
        <el-button
          type="primary"
          size="large"
          :loading="uploading"
          @click="handleUpload"
        >
          <el-icon><Upload /></el-icon>
          上传文件
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadResume } from '../api/resume.js'

const emit = defineEmits(['upload-success'])

const uploadRef = ref(null)
const selectedFile = ref(null)
const uploading = ref(false)

function getSessionId() {
  let sid = sessionStorage.getItem('resume_session_id')
  if (!sid) {
    sid = crypto.randomUUID()
    sessionStorage.setItem('resume_session_id', sid)
  }
  return sid
}

function handleBeforeUpload(file) {
  const maxSize = 10 * 1024 * 1024 // 10MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

function handleFileChange(uploadFile) {
  if (uploadFile.raw) {
    selectedFile.value = uploadFile.raw
  }
}

async function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  try {
    const sessionId = getSessionId()
    const result = await uploadResume(selectedFile.value, sessionId)
    emit('upload-success', result.resumeId)
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.file-uploader {
  max-width: 600px;
  margin: 0 auto;
}

.upload-icon {
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  color: #606266;
}

.upload-text em {
  color: #409eff;
  font-style: normal;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  text-align: center;
}

.file-info {
  margin-top: 20px;
}

.upload-action {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
