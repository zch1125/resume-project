<template>
  <div class="file-uploader">
    <div class="upload-zone" :class="{ 'zone-active': isDragOver, 'zone-has-file': selectedFile }"
      @dragover.prevent="isDragOver = true"
      @dragleave.prevent="isDragOver = false"
      @drop.prevent="onDrop"
      @click="selectFile"
    >
      <input ref="fileInput" type="file" accept=".pdf,.doc,.docx,.txt" hidden @change="onFileSelect" />
      <div v-if="!selectedFile" class="upload-placeholder">
        <div class="upload-icon-wrap">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 16V4M12 4L8 8M12 4L16 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M4 16V18C4 19.1 4.9 20 6 20H18C19.1 20 20 19.1 20 18V16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <p class="upload-text">将简历文件拖拽到这里，或 <span class="upload-link">点击选择文件</span></p>
        <p class="upload-hint">支持 PDF、Word（.doc/.docx）、TXT 格式，文件大小不超过 10MB</p>
      </div>
      <div v-else class="upload-selected">
        <div class="file-card">
          <div class="file-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M14 2H6C4.9 2 4 2.9 4 4V20C4 21.1 4.9 22 6 22H18C19.1 22 20 21.1 20 20V8L14 2Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M14 2V8H20" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="file-details">
            <span class="file-name">{{ selectedFile.name }}</span>
            <span class="file-size">{{ formatSize(selectedFile.size) }}</span>
          </div>
          <button class="file-remove" @click.stop="removeFile">✕</button>
        </div>
        <button class="upload-btn" :class="{ loading: uploading }" :disabled="uploading" @click.stop="handleUpload">
          <span v-if="uploading" class="btn-spinner"></span>
          <span v-else class="btn-icon">📤</span>
          <span>{{ uploading ? "上传中..." : "上传文件" }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { uploadResume } from "../api/resume.js";

const emit = defineEmits(["upload-success"]);

const fileInput = ref(null);
const selectedFile = ref(null);
const uploading = ref(false);
const isDragOver = ref(false);

function getSessionId() {
  let sid = sessionStorage.getItem("resume_session_id");
  if (!sid) {
    sid = crypto.randomUUID();
    sessionStorage.setItem("resume_session_id", sid);
  }
  return sid;
}

function selectFile() {
  fileInput.value?.click();
}

function onFileSelect(event) {
  const file = event.target.files[0];
  if (file) validateAndSet(file);
}

function onDrop(event) {
  isDragOver.value = false;
  const file = event.dataTransfer.files[0];
  if (file) validateAndSet(file);
}

function validateFile(file) {
  const maxSize = 10 * 1024 * 1024;
  const allowedTypes = [".pdf", ".doc", ".docx", ".txt"];
  const ext = file.name.toLowerCase().slice(file.name.lastIndexOf("."));
  if (file.size > maxSize) {
    ElMessage.error("文件大小不能超过 10MB");
    return false;
  }
  if (!allowedTypes.includes(ext)) {
    ElMessage.error("仅支持 PDF、Word、TXT 格式");
    return false;
  }
  return true;
}

function validateAndSet(file) {
  if (validateFile(file)) {
    selectedFile.value = file;
  }
}

function removeFile() {
  selectedFile.value = null;
  if (fileInput.value) fileInput.value.value = "";
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + " B";
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
  return (bytes / (1024 * 1024)).toFixed(1) + " MB";
}

async function handleUpload() {
  if (!selectedFile.value) return;
  uploading.value = true;
  try {
    const sessionId = getSessionId();
    const result = await uploadResume(selectedFile.value, sessionId);
    emit("upload-success", result.resumeId);
    ElMessage.success({ message: "简历上传成功", duration: 2000 });
  } catch (e) {
    // handled by interceptor
  } finally {
    uploading.value = false;
  }
}
</script>

<style scoped>
.file-uploader {
  width: 100%;
}

.upload-zone {
  border: 2px dashed var(--neutral-200);
  border-radius: var(--radius-lg);
  padding: 32px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.35s var(--ease-out-quart);
  background: rgba(255, 255, 255, 0.4);
  position: relative;
}

.upload-zone:hover {
  border-color: var(--primary-300);
  background: rgba(99, 102, 241, 0.03);
  border-style: solid;
}

.zone-active {
  border-color: var(--primary-400);
  background: rgba(99, 102, 241, 0.06);
  border-style: solid;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.08);
}

.zone-has-file {
  border-style: solid;
  border-color: var(--success);
  background: rgba(16, 185, 129, 0.03);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.upload-icon-wrap {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08), rgba(99, 102, 241, 0.02));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-400);
  margin-bottom: 4px;
  transition: transform 0.3s var(--ease-spring);
}

.upload-zone:hover .upload-icon-wrap {
  transform: translateY(-4px) scale(1.05);
}

.zone-active .upload-icon-wrap {
  transform: translateY(-4px) scale(1.1);
  color: var(--primary-500);
}

.upload-text {
  font-size: 15px;
  color: var(--neutral-600);
  line-height: 1.5;
}

.upload-link {
  color: var(--primary-500);
  font-weight: 600;
  text-decoration: underline;
  text-underline-offset: 2px;
  text-decoration-thickness: 1px;
  text-decoration-color: rgba(99, 102, 241, 0.3);
}

.upload-hint {
  font-size: 12px;
  color: var(--neutral-400);
}

.upload-selected {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.file-card {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  max-width: 420px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--neutral-200);
  border-radius: var(--radius-md);
  transition: all 0.25s;
}

.file-card:hover {
  border-color: var(--neutral-300);
  box-shadow: var(--shadow-sm);
}

.file-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--primary-50), var(--primary-100));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-500);
  flex-shrink: 0;
}

.file-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--neutral-700);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: var(--neutral-400);
}

.file-remove {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.08);
  color: var(--danger);
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}

.file-remove:hover {
  background: rgba(239, 68, 68, 0.15);
  transform: scale(1.15);
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 32px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  font-family: inherit;
  color: #fff;
  background: linear-gradient(135deg, var(--primary-500), var(--primary-600));
  cursor: pointer;
  transition: all 0.35s var(--ease-out-quart);
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.30);
}

.upload-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(99, 102, 241, 0.40);
}

.upload-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.30);
}

.upload-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

.loading {
  pointer-events: none;
}

.btn-icon {
  font-size: 18px;
}

.btn-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
