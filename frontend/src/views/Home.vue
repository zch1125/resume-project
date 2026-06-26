<template>
  <div class="home-container">
    <header class="app-header">
      <h1>
        <el-icon :size="28"><Document /></el-icon>
        简历分析平台
      </h1>
      <p class="subtitle">AI 驱动的智能简历分析与模拟面试系统</p>
    </header>

    <div class="main-content">
      <el-steps :active="activeStep" align-center class="step-bar">
        <el-step title="上传简历" description="支持 PDF/Word/TXT" />
        <el-step title="修改建议" description="AI 智能优化" />
        <el-step title="模拟面试" description="AI 对话出题评分" />
      </el-steps>

      <div class="step-content">
        <!-- Step 1: Upload -->
        <div v-show="activeStep === 0" class="step-panel">
          <div class="panel-card">
            <h3 class="panel-title">上传简历文件</h3>
            <FileUploader @upload-success="onUploadSuccess" />

            <div v-if="resumeId" class="parse-section">
              <el-divider />
              <h3 class="panel-title">简历解析</h3>
              <p class="info-text">文件已上传，点击下方按钮进行 AI 智能解析</p>
              <el-button type="primary" :loading="parsing" @click="handleParse" size="large">
                <el-icon><MagicStick /></el-icon>
                AI 解析简历
              </el-button>

              <div v-if="parsedData" class="parsed-result">
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="姓名">{{ parsedData.name || "-" }}</el-descriptions-item>
                  <el-descriptions-item label="学历">{{ parsedData.education || "-" }}</el-descriptions-item>
                  <el-descriptions-item label="工作年限">{{ parsedData.workYears || "-" }}</el-descriptions-item>
                  <el-descriptions-item label="期望岗位">{{ parsedData.expectedPosition || "-" }}</el-descriptions-item>
                  <el-descriptions-item label="技能" :span="2">
                    <el-tag v-for="skill in parsedData.skills || []" :key="skill" class="skill-tag">{{ skill }}</el-tag>
                    <span v-if="!parsedData.skills?.length">-</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="项目经历" :span="2">
                    <div v-for="proj in parsedData.projects || []" :key="proj" class="project-item">{{ proj }}</div>
                    <span v-if="!parsedData.projects?.length">-</span>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </div>

              <!-- Advice chat -->
              <div v-if="adviceResult &amp;&amp; adviceResult.suggestionId" class="advice-chat-section">
                <el-divider />
                <h4 class="chat-section-title">与 AI 对话优化建议</h4>
                <div class="advice-chat-messages" ref="adviceChatRef">
                  <div v-for="(msg, idx) in adviceChatMessages" :key="idx" class="advice-chat-msg">
                    <div :class="[&apos;advice-bubble&apos;, msg.role === &apos;user&apos; ? &apos;user-bubble&apos; : &apos;ai-bubble&apos;]">
                      <span class="bubble-role-label">{{ msg.role === &apos;ai&apos; ? &apos;AI&apos; : &apos;我&apos; }}</span>
                      <p>{{ msg.content }}</p>
                    </div>
                  </div>
                </div>
                <div class="advice-chat-input-row">
                  <el-input v-model="adviceChatInput" placeholder="对建议提出修改想法，例如：把项目经验写得更具体些..." :disabled="adviceChatLoading" @keydown.enter.prevent="handleAdviceChat" class="advice-chat-input" />
                  <el-button type="primary" :loading="adviceChatLoading" :disabled="!adviceChatInput.trim()" @click="handleAdviceChat" class="advice-chat-send-btn">发送</el-button>
                </div>
              </div>
          </div>
        </div>

        <!-- Step 2: Advice -->
        <div v-show="activeStep === 1" class="step-panel">
          <div class="panel-card">
            <h3 class="panel-title">获取简历修改建议</h3>
            <div class="advice-form">
              <el-form :model="adviceForm" label-width="120px">
                <el-form-item label="目标岗位" required>
                  <el-input v-model="adviceForm.jobTitle" placeholder="例如：Java高级工程师、前端架构师" size="large" clearable />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="adviceLoading" @click="handleGetAdvice" size="large">
                    <el-icon><ChatLineSquare /></el-icon>
                    获取修改建议
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <div v-if="adviceResult" class="advice-result">
              <el-divider />
              <el-result v-if="!adviceResult.needModify" icon="success" title="简历很棒，无需修改！">
                <template #extra>
                  <p class="overall-comment">{{ adviceResult.overallComment }}</p>
                </template>
              </el-result>

              <div v-else>
                <el-alert title="建议修改以下内容" type="warning" :closable="false" show-icon />
                <div class="suggestion-items">
                  <div v-for="(item, index) in adviceResult.suggestions" :key="index" class="suggestion-item">
                    <h4><el-tag type="warning" size="small">#{{ index + 1 }}</el-tag> {{ item.section }}</h4>
                    <div class="suggestion-diff">
                      <div class="diff-original"><span class="diff-label">原文：</span><p>{{ item.original }}</p></div>
                      <div class="diff-suggested"><span class="diff-label">建议：</span><p>{{ item.suggested }}</p></div>
                    </div>
                  </div>
                </div>
                <el-divider />
                <el-card class="overall-card">
                  <template #header><span>整体评价</span></template>
                  <p>{{ adviceResult.overallComment }}</p>
                </el-card>
              </div>
            </div>
              <!-- Advice chat -->
              <div v-if="adviceResult &amp;&amp; adviceResult.suggestionId" class="advice-chat-section">
                <el-divider />
                <h4 class="chat-section-title">与 AI 对话优化建议</h4>
                <div class="advice-chat-messages" ref="adviceChatRef">
                  <div v-for="(msg, idx) in adviceChatMessages" :key="idx" class="advice-chat-msg">
                    <div :class="['advice-bubble', msg.role === 'user' ? 'user-bubble' : 'ai-bubble']">
                      <span class="bubble-role-label">{{ msg.role === 'ai' ? 'AI' : '我' }}</span>
                      <p>{{ msg.content }}</p>
                    </div>
                  </div>
                </div>
                <div class="advice-chat-input-row">
                  <el-input v-model="adviceChatInput" placeholder="对建议提出修改想法，例如：把项目经验写得更具体些..." :disabled="adviceChatLoading" @keydown.enter.prevent="handleAdviceChat" class="advice-chat-input" />
                  <el-button type="primary" :loading="adviceChatLoading" :disabled="!adviceChatInput.trim()" @click="handleAdviceChat" class="advice-chat-send-btn">发送</el-button>
                </div>
              </div>
          </div>
        </div>

        <!-- Step 3: Conversational Interview -->
        <div v-show="activeStep === 2" class="step-panel">
          <div class="panel-card interview-card">
            <!-- Before interview: paste job description -->
            <template v-if="!sessionId">
              <h3 class="panel-title">模拟面试</h3>
              <p class="info-text">粘贴岗位招聘信息（JD），AI 将根据你的简历和岗位要求进行一对一真实模拟面试</p>
              <div class="job-desc-form">
                <el-input
                  v-model="jobDescription"
                  type="textarea"
                  :rows="10"
                  placeholder="请粘贴岗位招聘信息（JD），包括岗位职责、任职要求、技术栈等..."
                  class="jd-input"
                />
                <div class="jd-upload-area">
                  <div v-if="!imagePreview" class="jd-upload-trigger" @click="$refs.fileInput.click()">
                    <span class="jd-upload-icon">🖼️</span>
                    <span class="jd-upload-text">上传岗位截图（可选）</span>
                    <input ref="fileInput" type="file" accept="image/*" @change="handleImageUpload" hidden />
                  </div>
                  <div v-else class="jd-image-preview">
                    <img :src="imagePreview" alt="岗位截图" />
                    <el-button @click="removeImage" type="danger" circle size="small" class="jd-remove-btn">✕</el-button>
                  </div>
                </div>
                <el-button
                  type="primary"
                  size="large"
                  :loading="startingInterview"
                  :disabled="!jobDescription.trim()"
                  @click="handleStartInterview"
                  class="start-btn"
                >
                  <el-icon><CaretRight /></el-icon>
                  开始面试
                </el-button>
              </div>
            </template>

            <!-- During / after interview -->
            <template v-else>
              <div class="interview-header">
                <h3 class="panel-title">模拟面试进行中</h3>
                <el-button type="danger" plain size="small" @click="handleResetInterview">
                  <el-icon><Refresh /></el-icon>
                  结束重来
                </el-button>
              </div>

              <div class="chat-container" ref="chatContainer">
                <div v-for="msg in messages" :key="msg.id" class="chat-message">
                  <div :class="['chat-bubble', msg.role === 'ai' ? 'ai-bubble' : 'user-bubble']">
                    <div class="bubble-header">
                      <span class="bubble-role">{{ msg.role === 'ai' ? '面试官' : '我' }}</span>
                      <el-tag
                        v-if="msg.type && msg.role === 'ai'"
                        :type="msg.type === '技术' ? 'primary' : msg.type === '项目' ? 'success' : 'warning'"
                        size="small"
                      >{{ msg.type }}</el-tag>
                      <el-tag v-if="msg.isFollowUp && msg.role === 'ai'" type="info" size="small" effect="plain">追问</el-tag>
                    </div>
                    <div class="bubble-text">{{ msg.content }}</div>
                  </div>
                </div>

                <div v-if="chatLoading" class="chat-message">
                  <div class="chat-bubble ai-bubble thinking-bubble">
                    <div class="thinking-dots">
                      <span class="dot"></span>
                      <span class="dot"></span>
                      <span class="dot"></span>
                    </div>
                  </div>
                </div>

                <!-- Evaluation -->
                <div v-if="evaluation" class="evaluation-section">
                  <el-divider />
                  <el-card class="evaluation-card">
                    <template #header>
                      <div class="eval-header">
                        <span>面试评价</span>
                        <el-tag :type="evalTagType" size="large" class="eval-score-tag">{{ evaluation.totalScore }} 分</el-tag>
                      </div>
                    </template>
                    <div class="eval-body">
                      <p class="eval-summary">{{ evaluation.summary }}</p>
                      <div class="eval-lists">
                        <div class="eval-strengths" v-if="evaluation.strengths?.length">
                          <h4>优势</h4>
                          <ul><li v-for="(s, i) in evaluation.strengths" :key="i">{{ s }}</li></ul>
                        </div>
                        <div class="eval-weaknesses" v-if="evaluation.weaknesses?.length">
                          <h4>待改进</h4>
                          <ul><li v-for="(w, i) in evaluation.weaknesses" :key="i">{{ w }}</li></ul>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </div>
              </div>

              <!-- Answer input -->
              <div v-if="!evaluation" class="chat-input-area">
                <el-input
                  v-model="currentAnswer"
                  type="textarea"
                  :rows="2"
                  placeholder="输入你的回答，按 Enter 发送..."
                  :disabled="chatLoading"
                  @keydown.enter.prevent="handleSendAnswer"
                  class="chat-input"
                />
                <el-button type="primary" :loading="chatLoading" :disabled="!currentAnswer.trim()" @click="handleSendAnswer" class="send-btn">
                  <el-icon><Promotion /></el-icon>
                  发送
                </el-button>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div class="bottom-actions">
        <el-button v-if="activeStep > 0" @click="prevStep">
          <el-icon><ArrowLeft /></el-icon>
          上一步
        </el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="nextStep" :disabled="!canNextStep">
          下一步
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watch, nextTick } from "vue";
import { ElMessage } from "element-plus";
import FileUploader from "../components/FileUploader.vue";
import { parseResume, getResumeAdvice, chatAdvice } from "../api/resume.js";
import { startInterview, chatInterview } from "../api/interview.js";

const activeStep = ref(0);
const resumeId = ref(null);
const parsing = ref(false);
const parsedData = ref(null);
const adviceLoading = ref(false);
const adviceResult = ref(null);
const adviceChatInput = ref("");
const adviceChatMessages = ref([]);
const adviceChatLoading = ref(false);
const adviceChatRef = ref(null);

const adviceForm = reactive({
  jobTitle: "",
});

// Interview chat state
const jobDescription = ref("");
const startingInterview = ref(false);
const sessionId = ref(null);
const imageFile = ref(null);
const imagePreview = ref(null);
const messages = ref([]);
const currentAnswer = ref("");
const chatLoading = ref(false);
const evaluation = ref(null);
const chatContainer = ref(null);

// Computed
const canNextStep = computed(() => {
  if (activeStep.value === 0) return !!resumeId.value;
  if (activeStep.value === 1) return !!adviceResult.value;
  return true;
});

const evalTagType = computed(() => {
  if (!evaluation.value) return "info";
  const score = evaluation.value.totalScore;
  if (score >= 90) return "success";
  if (score >= 60) return "warning";
  return "danger";
});

// Auto-scroll chat to bottom when messages or loading change
watch(messages, async () => { await nextTick(); scrollToBottom(); }, { deep: true });
watch(chatLoading, async () => { await nextTick(); scrollToBottom(); });

function scrollToBottom() {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
}

// Navigation
function nextStep() { if (activeStep.value < 2) activeStep.value++; }
function prevStep() { if (activeStep.value > 0) activeStep.value--; }

function onUploadSuccess(id) {
  resumeId.value = id;
  ElMessage.success("简历上传成功");
}

async function handleParse() {
  parsing.value = true;
  try {
    const resume = await parseResume(resumeId.value);
    if (resume.parsedJson) {
      parsedData.value = typeof resume.parsedJson === "string" ? JSON.parse(resume.parsedJson) : resume.parsedJson;
    }
    ElMessage.success("简历解析完成");
  } catch (e) { /* handled by interceptor */ }
  finally { parsing.value = false; }
}

async function handleGetAdvice() {
  if (!adviceForm.jobTitle.trim()) { ElMessage.warning("请输入目标岗位"); return; }
  adviceLoading.value = true;
  try {
    const res = await getResumeAdvice(resumeId.value, adviceForm.jobTitle.trim());
    adviceResult.value = res;
    ElMessage.success(res.needModify ? "已生成修改建议" : "简历很棒，无需修改！");
  } catch (e) { /* handled by interceptor */ }
  finally { adviceLoading.value = false; }
}

// Start the conversational interview
async function handleStartInterview() {
  if (!jobDescription.value.trim()) { ElMessage.warning("请粘贴岗位招聘信息"); return; }
  startingInterview.value = true;
  try {
    let imageData = null;
    let imageType = null;
    if (imageFile.value) {
      imageType = imageFile.value.type;
      const b64 = await toBase64(imageFile.value);
      imageData = b64.split(",")[1];
    }
    const res = await startInterview(resumeId.value, jobDescription.value.trim(), imageData, imageType);
    sessionId.value = res.sessionId;
    imageFile.value = null;
    imagePreview.value = null;
    messages.value = [];
    messages.value.push({
      id: Date.now(),
      role: "ai",
      content: res.question,
      type: res.type,
      isFollowUp: false,
    });
    ElMessage.success("面试已开始，请回答第一个问题");
  } catch (e) { /* handled by interceptor */ }
  finally { startingInterview.value = false; }
}

// Send answer and get next question
async function handleSendAnswer() {
  if (!currentAnswer.value.trim() || !sessionId.value) return;
  const userAnswer = currentAnswer.value.trim();
  currentAnswer.value = "";

  messages.value.push({
    id: Date.now() + Math.random(),
    role: "user",
    content: userAnswer,
  });

  chatLoading.value = true;
  try {
    const res = await chatInterview(sessionId.value, userAnswer);

    if (res.isComplete) {
      evaluation.value = {
        totalScore: res.totalScore,
        summary: res.summary,
        strengths: res.strengths || [],
        weaknesses: res.weaknesses || [],
      };
      ElMessage.success("面试结束！得分：" + res.totalScore);
    } else {
      messages.value.push({
        id: Date.now() + Math.random(),
        role: "ai",
        content: res.question,
        type: res.type,
        isFollowUp: res.isFollowUp || false,
      });
    }
  } catch (e) { /* handled by interceptor */ }
  finally { chatLoading.value = false; }
}


async function handleAdviceChat() {
  if (!adviceChatInput.value.trim() || !adviceResult.value?.suggestionId) return;
  const msg = adviceChatInput.value.trim();
  adviceChatInput.value = "";
  adviceChatMessages.value.push({ role: "user", content: msg });
  adviceChatLoading.value = true;
  try {
    const res = await chatAdvice(adviceResult.value.suggestionId, msg);
    adviceResult.value.suggestions = res.suggestions;
    adviceResult.value.overallComment = res.overallComment;
    adviceResult.value.needModify = res.needModify;
    adviceChatMessages.value.push({ role: "ai", content: "建议已更新！" });
  } catch (e) { /* handled by interceptor */ }
  finally { adviceChatLoading.value = false; }
}


function toBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

function handleImageUpload(event) {
  const file = event.target.files[0];
  if (!file) return;
  imageFile.value = file;
  const reader = new FileReader();
  reader.onload = (e) => { imagePreview.value = e.target.result; };
  reader.readAsDataURL(file);
}

function removeImage() {
  imageFile.value = null;
  imagePreview.value = null;
}

function handleResetInterview() {
  sessionId.value = null;
  messages.value = [];
  currentAnswer.value = "";
  evaluation.value = null;
  jobDescription.value = "";
}
</script>

<style scoped>
.home-container { max-width: 960px; margin: 0 auto; padding: 40px 20px; }
.app-header { text-align: center; margin-bottom: 40px; }
.app-header h1 { display: flex; align-items: center; justify-content: center; gap: 12px; font-size: 28px; color: #303133; }
.subtitle { color: #909399; margin-top: 8px; font-size: 14px; }
.step-bar { margin-bottom: 40px; }
.step-content { min-height: 400px; }
.step-panel { animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.panel-card { background: #fff; border-radius: 12px; padding: 32px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); }
.panel-title { font-size: 18px; color: #303133; margin-bottom: 16px; }
.info-text { color: #909399; margin-bottom: 16px; }
.parse-section { margin-top: 16px; }
.parsed-result { margin-top: 20px; }
.skill-tag { margin: 2px 4px 2px 0; }
.project-item { padding: 4px 0; border-bottom: 1px solid #f0f0f0; }
.project-item:last-child { border-bottom: none; }
.advice-form { max-width: 500px; }
.suggestion-items { margin-top: 16px; }
.suggestion-item { padding: 16px; margin-bottom: 12px; background: #fafafa; border-radius: 8px; border-left: 3px solid #e6a23c; }
.suggestion-item h4 { margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
.suggestion-diff { display: flex; flex-direction: column; gap: 8px; }
.diff-label { font-weight: bold; color: #606266; font-size: 13px; }
.diff-original p, .diff-suggested p { margin-top: 4px; padding: 8px; border-radius: 4px; font-size: 14px; line-height: 1.6; }
.diff-original p { background: #fef0f0; color: #f56c6c; }
.diff-suggested p { background: #f0f9eb; color: #67c23a; }
.overall-card { margin-top: 8px; }
.overall-comment { font-size: 15px; color: #606266; line-height: 1.6; }
.bottom-actions { display: flex; justify-content: space-between; margin-top: 32px; padding: 0 4px; }

/* Interview / Chat styles */
.interview-card { display: flex; flex-direction: column; }
.job-desc-form { display: flex; flex-direction: column; gap: 16px; }
.start-btn { align-self: flex-start; }

/* Image upload */
.jd-upload-area { margin: 8px 0; }
.jd-upload-trigger { display: flex; align-items: center; gap: 8px; padding: 16px; border: 2px dashed #dcdfe6; border-radius: 8px; cursor: pointer; transition: all 0.2s; background: #fafafa; }
.jd-upload-trigger:hover { border-color: #409eff; background: #ecf5ff; }
.jd-upload-icon { font-size: 24px; }
.jd-upload-text { font-size: 14px; color: #909399; }
.jd-image-preview { position: relative; display: inline-block; }
.jd-image-preview img { max-height: 160px; border-radius: 8px; border: 1px solid #ebeef5; }
.jd-remove-btn { position: absolute; top: -8px; right: -8px; }
.interview-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }

.chat-container {
  max-height: 480px;
  min-height: 320px;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fb;
  border-radius: 8px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-message { display: flex; flex-direction: column; }
.chat-bubble { max-width: 85%; padding: 14px 16px; border-radius: 12px; font-size: 14px; line-height: 1.6; word-break: break-word; }
.ai-bubble { background: #fff; border: 1px solid #e4e7ed; align-self: flex-start; border-bottom-left-radius: 4px; }
.user-bubble { background: #409eff; color: #fff; align-self: flex-end; border-bottom-right-radius: 4px; }

.bubble-header { display: flex; align-items: center; gap: 6px; margin-bottom: 6px; }
.user-bubble .bubble-header { flex-direction: row-reverse; }
.bubble-role { font-weight: 600; font-size: 12px; color: #606266; }
.user-bubble .bubble-role { color: rgba(255, 255, 255, 0.9); }
.bubble-text { white-space: pre-wrap; }

.thinking-bubble { padding: 14px 24px; }
.thinking-dots { display: flex; gap: 6px; align-items: center; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #c0c4cc; animation: dotPulse 1.4s ease-in-out infinite; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotPulse { 0%, 80%, 100% { opacity: 0.3; } 40% { opacity: 1; } }

.chat-input-area { display: flex; gap: 12px; align-items: flex-start; }
.chat-input { flex: 1; }
.send-btn { margin-top: 2px; }

.evaluation-section { margin-top: 8px; }
.evaluation-card { width: 100%; }
.eval-header { display: flex; align-items: center; justify-content: space-between; }
.eval-score-tag { font-size: 18px; font-weight: bold; padding: 4px 16px; }
.eval-body { padding: 8px 0; }
.eval-summary { font-size: 15px; line-height: 1.8; color: #303133; margin-bottom: 16px; white-space: pre-wrap; }
.eval-lists { display: flex; gap: 24px; flex-wrap: wrap; }
.eval-strengths, .eval-weaknesses { flex: 1; min-width: 200px; }
.eval-strengths h4 { color: #67c23a; margin-bottom: 8px; }
.eval-weaknesses h4 { color: #e6a23c; margin-bottom: 8px; }
.eval-lists li { padding: 4px 0; font-size: 14px; line-height: 1.5; color: #606266; }

/* Advice chat */
.advice-chat-section { margin-top: 8px; }
.chat-section-title { font-size: 15px; color: #303133; margin-bottom: 12px; }
.advice-chat-messages { max-height: 240px; min-height: 60px; overflow-y: auto; padding: 12px; background: #f8f9fb; border-radius: 8px; margin-bottom: 12px; display: flex; flex-direction: column; gap: 10px; }
.advice-chat-msg { display: flex; }
.advice-bubble { max-width: 90%; padding: 10px 14px; border-radius: 10px; font-size: 14px; line-height: 1.5; }
.advice-bubble.ai-bubble { background: #fff; border: 1px solid #e4e7ed; align-self: flex-start; border-bottom-left-radius: 4px; }
.advice-bubble.user-bubble { background: #409eff; color: #fff; align-self: flex-end; border-bottom-right-radius: 4px; margin-left: auto; }
.bubble-role-label { font-weight: 600; font-size: 11px; display: block; margin-bottom: 4px; opacity: 0.7; }
.user-bubble .bubble-role-label { text-align: right; color: rgba(255,255,255,0.9); }
.ai-bubble .bubble-role-label { color: #606266; }
.advice-bubble p { margin: 0; white-space: pre-wrap; }
.advice-chat-input-row { display: flex; gap: 10px; align-items: flex-start; }
.advice-chat-input { flex: 1; }
.advice-chat-send-btn { flex-shrink: 0; }

</style>