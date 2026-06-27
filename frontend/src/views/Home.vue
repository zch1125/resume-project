<template>
  <div class="home-container">
    <!-- Header -->
    <header class="app-header">
      <div class="header-badge">AI 赋能 · 智能分析</div>
      <h1 class="app-title">
        <span class="title-icon">
          <svg
            width="32"
            height="32"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
              fill="currentColor"
            />
          </svg>
        </span>
        简历分析平台
      </h1>
      <p class="app-subtitle">AI 驱动的智能简历分析与模拟面试系统</p>
      <div class="header-glow"></div>
      <el-button
        type="info"
        plain
        size="small"
        @click="handleShowHistory"
        class="history-top-btn"
      >
        面试历史记录
      </el-button>
    </header>

    <div class="main-content">
      <!-- Custom Step Indicator -->
      <div class="step-indicator">
        <div
          v-for="(step, idx) in steps"
          :key="idx"
          class="step-item"
          :class="{
            'step-active': activeStep === idx,
            'step-completed': idx < activeStep,
          }"
          @click="activeStep = idx"
        >
          <div class="step-dot-wrap">
            <div class="step-dot">
              <span v-if="idx < activeStep" class="step-check">✓</span>
              <span v-else class="step-num">{{ idx + 1 }}</span>
            </div>
            <div
              v-if="idx < steps.length - 1"
              class="step-line"
              :class="{ 'line-active': idx < activeStep }"
            ></div>
          </div>
          <div class="step-label">
            <span class="step-title">{{ step.title }}</span>
            <span class="step-desc">{{ step.desc }}</span>
          </div>
        </div>
      </div>

      <!-- Step Content -->
      <transition name="step-fade" mode="out-in">
        <!-- Step 1: Upload -->
        <div v-if="activeStep === 0" key="step0" class="step-panel">
          <div class="glass-card">
            <div class="card-accent"></div>
            <div class="card-header">
              <h3 class="card-title">
                <span class="card-step-badge">01</span>
                上传简历文件
              </h3>
              <p class="card-desc">
                支持 PDF、Word、TXT 格式，AI 将自动提取关键信息
              </p>
            </div>
            <FileUploader @upload-success="onUploadSuccess" />

            <transition name="slide-up">
              <div v-if="resumeId" class="section-divider">
                <div class="divider-line"></div>
                <span class="divider-label">文件已上传</span>
                <div class="divider-line"></div>
              </div>
            </transition>

            <transition name="slide-up">
              <div v-if="resumeId" class="parse-section">
                <div class="section-header">
                  <h4 class="section-title">
                    <span class="section-icon">🔍</span>
                    简历解析
                  </h4>
                  <p class="section-hint">点击下方按钮进行 AI 智能解析</p>
                </div>

                <button
                  class="glow-btn"
                  :class="{ loading: parsing }"
                  :disabled="parsing"
                  @click="handleParse"
                >
                  <span v-if="parsing" class="btn-spinner"></span>
                  <span v-else class="btn-icon">✨</span>
                  <span>{{ parsing ? "解析中..." : "AI 解析简历" }}</span>
                </button>

                <transition name="slide-up">
                  <div v-if="parsedData" class="parsed-result">
                    <div class="result-grid">
                      <div class="result-item">
                        <span class="result-label">姓名</span>
                        <span class="result-value">{{
                          parsedData.name || "-"
                        }}</span>
                      </div>
                      <div class="result-item">
                        <span class="result-label">学历</span>
                        <span class="result-value">{{
                          parsedData.education || "-"
                        }}</span>
                      </div>
                      <div class="result-item">
                        <span class="result-label">工作年限</span>
                        <span class="result-value">{{
                          parsedData.workYears || "-"
                        }}</span>
                      </div>
                      <div class="result-item">
                        <span class="result-label">期望岗位</span>
                        <span class="result-value">{{
                          parsedData.expectedPosition || "-"
                        }}</span>
                      </div>
                    </div>
                    <div
                      class="result-section"
                      v-if="parsedData.skills?.length"
                    >
                      <span class="result-label">技能标签</span>
                      <div class="skill-cloud">
                        <span
                          v-for="skill in parsedData.skills"
                          :key="skill"
                          class="skill-chip"
                          >{{ skill }}</span
                        >
                      </div>
                    </div>
                    <div
                      class="result-section"
                      v-if="parsedData.projects?.length"
                    >
                      <span class="result-label">项目经历</span>
                      <div
                        v-for="(proj, i) in parsedData.projects"
                        :key="i"
                        class="project-chip"
                      >
                        {{ proj }}
                      </div>
                    </div>
                  </div>
                </transition>
              </div>
            </transition>
          </div>
        </div>

        <!-- Step 2: Advice -->
        <div v-else-if="activeStep === 1" key="step1" class="step-panel">
          <div class="glass-card">
            <div class="card-accent card-accent-amber"></div>
            <div class="card-header">
              <h3 class="card-title">
                <span class="card-step-badge">02</span>
                获取修改建议
              </h3>
              <p class="card-desc">
                输入目标岗位，AI 将为你生成针对性的优化建议
              </p>
            </div>

            <div class="advice-form">
              <div class="input-group">
                <div class="input-icon">🎯</div>
                <input
                  v-model="adviceForm.jobTitle"
                  placeholder="例如：Java高级工程师、前端架构师"
                  class="premium-input"
                  @keydown.enter="handleGetAdvice"
                />
              </div>
              <button
                class="glow-btn glow-btn-amber"
                :class="{ loading: adviceLoading }"
                :disabled="adviceLoading || !adviceForm.jobTitle.trim()"
                @click="handleGetAdvice"
              >
                <span v-if="adviceLoading" class="btn-spinner"></span>
                <span v-else class="btn-icon">💡</span>
                <span>{{ adviceLoading ? "生成中..." : "获取修改建议" }}</span>
              </button>
            </div>

            <transition name="slide-up">
              <div v-if="adviceResult" class="advice-result">
                <div class="section-divider">
                  <div class="divider-line"></div>
                  <span class="divider-label">建议结果</span>
                  <div class="divider-line"></div>
                </div>

                <div v-if="!adviceResult.needModify" class="success-banner">
                  <span class="success-icon">🎉</span>
                  <div>
                    <h4>简历很棒，无需修改！</h4>
                    <p>{{ adviceResult.overallComment }}</p>
                  </div>
                </div>

                <template v-else>
                  <div class="suggestion-count">
                    共
                    <strong>{{ adviceResult.suggestions?.length }}</strong>
                    条优化建议
                  </div>
                  <div class="suggestion-list">
                    <div
                      v-for="(item, index) in adviceResult.suggestions"
                      :key="index"
                      class="suggestion-card"
                    >
                      <div class="suggestion-header">
                        <span class="suggestion-num">#{{ index + 1 }}</span>
                        <span class="suggestion-section">{{
                          item.section
                        }}</span>
                      </div>
                      <div class="suggestion-body">
                        <div class="diff-block diff-original">
                          <span class="diff-badge diff-bad">原文</span>
                          <p>{{ item.original }}</p>
                        </div>
                        <div class="diff-arrow">→</div>
                        <div class="diff-block diff-suggested">
                          <span class="diff-badge diff-good">建议</span>
                          <p>{{ item.suggested }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="overall-card">
                    <span class="overall-icon">📝</span>
                    <div>
                      <h4>整体评价</h4>
                      <p>{{ adviceResult.overallComment }}</p>
                    </div>
                  </div>
                </template>

                <!-- Advice Chat -->
                <div
                  v-if="adviceResult.suggestionId"
                  class="advice-chat-section"
                >
                  <div class="section-divider">
                    <div class="divider-line"></div>
                    <span class="divider-label">对话优化</span>
                    <div class="divider-line"></div>
                  </div>
                  <p class="chat-hint">
                    对建议不满意？告诉我你的想法，我会帮你调整
                  </p>
                  <div class="chat-messages" ref="adviceChatRef">
                    <div
                      v-for="(msg, idx) in adviceChatMessages"
                      :key="idx"
                      class="chat-msg"
                      :class="msg.role"
                    >
                      <div class="msg-avatar">
                        {{ msg.role === "ai" ? "🤖" : "👤" }}
                      </div>
                      <div class="msg-bubble">
                        <p>{{ msg.content }}</p>
                      </div>
                    </div>
                  </div>
                  <div class="chat-input-row">
                    <input
                      v-model="adviceChatInput"
                      placeholder="对建议提出修改想法，例如：把项目经验写得更具体些..."
                      :disabled="adviceChatLoading"
                      class="premium-input chat-input-small"
                      @keydown.enter.prevent="handleAdviceChat"
                    />
                    <button
                      class="glow-btn glow-btn-small"
                      :disabled="!adviceChatInput.trim() || adviceChatLoading"
                      @click="handleAdviceChat"
                    >
                      <span>发送</span>
                    </button>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>
        <!-- Step 3: Interview -->
        <div v-else-if="activeStep === 2" key="step2" class="step-panel">
          <div class="glass-card">
            <div class="card-accent card-accent-green"></div>
            <div class="card-header">
              <h3 class="card-title">
                <span class="card-step-badge">03</span>
                模拟面试
              </h3>
              <p class="card-desc">
                {{
                  sessionId
                    ? "与 AI 面试官进行一对一面试"
                    : "粘贴职位信息，开始模拟面试"
                }}
              </p>
            </div>

            <!-- Before interview -->
            <template v-if="!sessionId">
              <div class="jd-section">
                <div class="input-group jd-input-group">
                  <div class="input-icon jd-icon">📋</div>
                  <textarea
                    v-model="jobDescription"
                    :rows="8"
                    placeholder="请粘贴职位招聘信息（JD），包括岗位职责、任职要求、技术栈等..."
                    class="premium-input premium-textarea"
                  ></textarea>
                </div>

                <div class="jd-upload-area">
                  <div
                    v-if="!imagePreview"
                    class="jd-upload-trigger"
                    @click="$refs.fileInput.click()"
                  >
                    <span class="upload-icon-small">🖼️</span>
                    <span class="upload-text-small">上传职位截图（可选）</span>
                    <input
                      ref="fileInput"
                      type="file"
                      accept="image/*"
                      @change="handleImageUpload"
                      hidden
                    />
                  </div>
                  <div v-else class="jd-image-preview">
                    <img :src="imagePreview" alt="职位截图" />
                    <button class="img-remove-btn" @click="removeImage">
                      ✕
                    </button>
                  </div>
                </div>

                <button
                  class="glow-btn glow-btn-green start-btn"
                  :class="{ loading: startingInterview }"
                  :disabled="
                    (!jobDescription.trim() && !imageFile) || startingInterview
                  "
                  @click="handleStartInterview"
                >
                  <span v-if="startingInterview" class="btn-spinner"></span>
                  <span v-else class="btn-icon">🎤</span>
                  <span>{{
                    startingInterview ? "准备中..." : "开始面试"
                  }}</span>
                </button>
              </div>
            </template>

            <!-- During interview -->
            <template v-else>
              <div class="interview-header">
                <div class="interview-status">
                  <span class="status-dot"></span>
                  <span>面试进行中</span>
                </div>
                <button class="reset-btn" @click="handleResetInterview">
                  <span>✕</span>
                  结束重来
                </button>
              </div>

              <div class="chat-container" ref="chatContainer">
                <div
                  v-for="msg in messages"
                  :key="msg.id"
                  class="chat-message"
                  :class="msg.role"
                >
                  <div class="msg-avatar msg-avatar-lg">
                    {{ msg.role === "ai" ? "🤖" : "👤" }}
                  </div>
                  <div class="msg-content">
                    <div class="msg-meta">
                      <span class="msg-role">{{
                        msg.role === "ai" ? "面试官" : "我"
                      }}</span>
                      <span
                        v-if="msg.type && msg.role === 'ai'"
                        class="msg-tag"
                        :class="'tag-' + msg.type"
                        >{{ msg.type }}</span
                      >
                      <span
                        v-if="msg.isFollowUp && msg.role === 'ai'"
                        class="msg-tag tag-follow"
                        >追问</span
                      >
                    </div>
                    <div class="msg-bubble msg-bubble-lg">
                      {{ msg.content }}
                    </div>
                  </div>
                </div>

                <div v-if="chatLoading" class="chat-message ai">
                  <div class="msg-avatar msg-avatar-lg">🤖</div>
                  <div class="msg-content">
                    <div class="msg-bubble msg-bubble-lg thinking">
                      <div class="thinking-dots">
                        <span class="dot"></span>
                        <span class="dot"></span>
                        <span class="dot"></span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Evaluation -->
                <transition name="slide-up">
                  <div v-if="evaluation" class="evaluation-section">
                    <div class="eval-card">
                      <div class="eval-header">
                        <h4>面试评价</h4>
                        <div class="eval-score" :class="'score-' + evalLevel">
                          <span class="score-num">{{
                            evaluation.totalScore
                          }}</span>
                          <span class="score-unit">分</span>
                        </div>
                      </div>
                      <p class="eval-summary">{{ evaluation.summary }}</p>
                      <div class="eval-lists">
                        <div
                          class="eval-strengths"
                          v-if="evaluation.strengths?.length"
                        >
                          <div class="eval-list-header">
                            <span>✅</span>
                            <h5>优势</h5>
                          </div>
                          <ul>
                            <li v-for="(s, i) in evaluation.strengths" :key="i">
                              {{ s }}
                            </li>
                          </ul>
                        </div>
                        <div
                          class="eval-weaknesses"
                          v-if="evaluation.weaknesses?.length"
                        >
                          <div class="eval-list-header">
                            <span>💪</span>
                            <h5>待改进</h5>
                          </div>
                          <ul>
                            <li
                              v-for="(w, i) in evaluation.weaknesses"
                              :key="i"
                            >
                              {{ w }}
                            </li>
                          </ul>
                        </div>
                      </div>
                    </div>
                  </div>
                </transition>
              </div>

              <div v-if="!evaluation" class="chat-input-area">
                <div class="input-group chat-input-group">
                  <textarea
                    v-model="currentAnswer"
                    :rows="2"
                    placeholder="输入你的回答，按 Enter 发送..."
                    :disabled="chatLoading"
                    class="premium-input premium-textarea chat-textarea"
                    @keydown.enter.prevent="handleSendAnswer"
                  ></textarea>
                </div>
                <button
                  class="glow-btn glow-btn-green"
                  :disabled="!currentAnswer.trim() || chatLoading"
                  @click="handleSendAnswer"
                >
                  <span class="btn-icon">📤</span>
                  <span>发送</span>
                </button>
              </div>
            </template>
          </div>
        </div>
      </transition>

      <!-- Bottom Navigation -->
      <div class="bottom-nav">
        <button
          v-if="activeStep > 0"
          class="nav-btn nav-prev"
          @click="prevStep"
        >
          <span>←</span>
          <span>上一步</span>
        </button>
        <div class="nav-spacer"></div>
        <button
          v-if="activeStep < 2"
          class="nav-btn nav-next"
          :disabled="!canNextStep"
          @click="nextStep"
        >
          <span>下一步</span>
          <span>→</span>
        </button>
      </div>
    </div>

    <!-- 面试历史记录 Modal -->
    <div v-if="showHistory" class="modal-overlay" @click.self="closeHistory">
      <div class="modal-content modal-history">
        <div class="modal-header">
          <h3>面试历史记录</h3>
          <button class="modal-close" @click="closeHistory">✕</button>
        </div>
        <div class="modal-body">
          <div
            v-if="historyList.length === 0"
            style="text-align: center; padding: 40px; color: #909399"
          >
            暂无面试记录
          </div>
          <div v-else>
            <div
              v-for="item in historyList"
              :key="item.id"
              class="history-item"
              @click="viewHistoryDetail(item)"
            >
              <div class="history-item-header">
                <span class="history-job">{{
                  item.jobDescription
                    ? item.jobDescription.slice(0, 60) +
                      (item.jobDescription.length > 60 ? "..." : "")
                    : "暂无标题"
                }}</span>
                <el-tag
                  :type="item.status === 'completed' ? 'success' : 'warning'"
                  size="small"
                  >{{
                    item.status === "completed" ? "已结束" : "进行中"
                  }}</el-tag
                >
              </div>
              <div class="history-item-meta">
                <span>{{ item.createTime }}</span>
                <span v-if="item.status === 'completed'"
                  >得分：{{ item.totalScore }} 分 | 共
                  {{ item.questionCount }} 题</span
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 面试详情 Modal -->
    <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail">
      <div class="modal-content modal-detail">
        <div class="modal-header">
          <h3>面试详情</h3>
          <button class="modal-close" @click="closeDetail">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="detailItem">
            <p style="margin-bottom: 8px"><strong>岗位信息：</strong></p>
            <p
              style="white-space: pre-wrap; color: #606266; margin-bottom: 16px"
            >
              {{ detailItem.jobDescription }}
            </p>
            <p style="margin-bottom: 8px">
              <strong>状态：</strong>
              <el-tag
                :type="
                  detailItem.status === 'completed' ? 'success' : 'warning'
                "
                size="small"
                >{{
                  detailItem.status === "completed" ? "已结束" : "进行中"
                }}</el-tag
              >
            </p>
            <p
              v-if="detailItem.status === 'completed'"
              style="margin-bottom: 8px"
            >
              <strong>得分：</strong> {{ detailItem.totalScore }} 分 |
              <strong>题目数：</strong> {{ detailItem.questionCount }} 题
            </p>
            <p
              v-if="detailItem.summary"
              style="margin-top: 16px; margin-bottom: 8px"
            >
              <strong>综合评价：</strong>
            </p>
            <p
              v-if="detailItem.summary"
              style="
                white-space: pre-wrap;
                color: #303133;
                line-height: 1.8;
                background: #f8f9fb;
                padding: 16px;
                border-radius: 8px;
              "
            >
              {{ detailItem.summary }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, reactive, watch, nextTick } from "vue";
import { ElMessage } from "element-plus";
import FileUploader from "../components/FileUploader.vue";
import { parseResume, getResumeAdvice, chatAdvice } from "../api/resume.js";
import {
  startInterview,
  chatInterview,
  getInterviewHistory,
} from "../api/interview.js";

const steps = [
  { title: "上传简历", desc: "支持 PDF/Word/TXT" },
  { title: "修改建议", desc: "AI 智能优化" },
  { title: "模拟面试", desc: "AI 对话出题评分" },
];

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

const jobDescription = ref("");
const startingInterview = ref(false);
const sessionId = ref(null);
const imageFile = ref(null);
const showHistory = ref(false);
const showDetail = ref(false);
const historyList = ref([]);
const historyLoading = ref(false);
const detailItem = ref(null);
const imagePreview = ref(null);
const messages = ref([]);
const currentAnswer = ref("");
const chatLoading = ref(false);
const evaluation = ref(null);
const chatContainer = ref(null);

const canNextStep = computed(() => {
  if (activeStep.value === 0) return !!resumeId.value;
  if (activeStep.value === 1) return !!adviceResult.value;
  return true;
});

const evalLevel = computed(() => {
  if (!evaluation.value) return "info";
  const score = evaluation.value.totalScore;
  if (score >= 90) return "excellent";
  if (score >= 70) return "good";
  if (score >= 50) return "fair";
  return "poor";
});

watch(
  messages,
  async () => {
    await nextTick();
    scrollToBottom();
  },
  { deep: true },
);
watch(chatLoading, async () => {
  await nextTick();
  scrollToBottom();
});

function scrollToBottom() {
  if (chatContainer.value) {
    chatContainer.value.scrollTo({
      top: chatContainer.value.scrollHeight,
      behavior: "smooth",
    });
  }
}

function nextStep() {
  if (activeStep.value < 2) activeStep.value++;
}
function prevStep() {
  if (activeStep.value > 0) activeStep.value--;
}

function onUploadSuccess(id) {
  resumeId.value = id;
  ElMessage.success({ message: "简历上传成功", duration: 2000 });
}

async function handleParse() {
  parsing.value = true;
  try {
    const resume = await parseResume(resumeId.value);
    if (resume.parsedJson) {
      parsedData.value =
        typeof resume.parsedJson === "string"
          ? JSON.parse(resume.parsedJson)
          : resume.parsedJson;
    }
    ElMessage.success({ message: "简历解析完成", duration: 2000 });
  } catch (e) {
    /* handled by interceptor */
  } finally {
    parsing.value = false;
  }
}

async function handleGetAdvice() {
  if (!adviceForm.jobTitle.trim()) {
    ElMessage.warning("请输入目标岗位");
    return;
  }
  adviceLoading.value = true;
  try {
    const res = await getResumeAdvice(
      resumeId.value,
      adviceForm.jobTitle.trim(),
    );
    adviceResult.value = res;
    ElMessage.success(
      res.needModify ? "已生成修改建议" : "简历很棒，无需修改！",
    );
  } catch (e) {
    /* handled by interceptor */
  } finally {
    adviceLoading.value = false;
  }
}

async function handleStartInterview() {
  if (!jobDescription.value.trim() && !imageFile.value) {
    ElMessage.warning("请粘贴职位招聘信息或上传职位截图");
    return;
  }
  if (!jobDescription.value.trim() && imageFile.value) {
    jobDescription.value = "请根据我上传的截图中的职位招聘信息进行分析";
  }
  startingInterview.value = true;
  try {
    let imageData = null;
    let imageType = null;
    if (imageFile.value) {
      imageType = imageFile.value.type;
      const b64 = await toBase64(imageFile.value);
      imageData = b64.split(",")[1];
    }
    const res = await startInterview(
      resumeId.value,
      jobDescription.value.trim(),
      imageData,
      imageType,
    );
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
    ElMessage.success({
      message: "面试已开始，请回答第一个问题",
      duration: 2000,
    });
  } catch (e) {
    /* handled by interceptor */
  } finally {
    startingInterview.value = false;
  }
}

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
  } catch (e) {
    /* handled by interceptor */
  } finally {
    chatLoading.value = false;
  }
}

async function handleAdviceChat() {
  if (!adviceChatInput.value.trim() || !adviceResult.value?.suggestionId)
    return;
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
  } catch (e) {
    /* handled by interceptor */
  } finally {
    adviceChatLoading.value = false;
  }
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
  reader.onload = (e) => {
    imagePreview.value = e.target.result;
  };
  reader.readAsDataURL(file);
}

function removeImage() {
  imageFile.value = null;
  imagePreview.value = null;
}

// Show interview history
function handleShowHistory() {
  showHistory.value = true;
  setTimeout(() => loadHistory(), 100);
}

function closeHistory() {
  showHistory.value = false;
}

function closeDetail() {
  showDetail.value = false;
}

async function loadHistory() {
  historyLoading.value = true;
  try {
    const sid = sessionStorage.getItem("resume_session_id");
    if (sid) {
      historyList.value = await getInterviewHistory(sid);
    }
  } catch (e) {
    /* handled by interceptor */
  } finally {
    historyLoading.value = false;
  }
}

function viewHistoryDetail(item) {
  detailItem.value = item;
  showDetail.value = true;
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
/* ===== Container ===== */
.home-container {
  max-width: 860px;
  margin: 0 auto;
  padding: 32px 20px 60px;
  position: relative;
}

/* ===== Header ===== */
.app-header {
  text-align: center;
  margin-bottom: 48px;
  position: relative;
  padding: 40px 0 32px;
}

.header-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 16px;
  border-radius: var(--radius-full);
  background: rgba(99, 102, 241, 0.12);
  color: var(--primary-300);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 2px;
  text-transform: uppercase;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(99, 102, 241, 0.2);
  margin-bottom: 20px;
}

.app-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  font-size: 34px;
  font-weight: 800;
  background: linear-gradient(
    135deg,
    #e0e7ff 0%,
    #a5b4fc 30%,
    #818cf8 60%,
    #c4b5fd 100%
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.title-icon {
  display: inline-flex;
  color: var(--primary-400);
  -webkit-text-fill-color: initial;
  animation: titlePulse 3s ease-in-out infinite;
}

@keyframes titlePulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.08);
    opacity: 0.85;
  }
}

.app-subtitle {
  color: var(--neutral-400);
  font-size: 15px;
  margin-top: 12px;
  font-weight: 400;
  letter-spacing: 0.3px;
}

.header-glow {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 400px;
  height: 200px;
  background: radial-gradient(
    ellipse,
    rgba(99, 102, 241, 0.1) 0%,
    transparent 70%
  );
  pointer-events: none;
}

/* ===== Step Indicator ===== */
.step-indicator {
  display: flex;
  justify-content: center;
  gap: 0;
  margin-bottom: 40px;
  padding: 0 20px;
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  cursor: pointer;
  flex: 1;
  max-width: 220px;
  position: relative;
}

.step-dot-wrap {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.step-dot {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.06);
  border: 2px solid rgba(255, 255, 255, 0.12);
  color: var(--neutral-400);
  transition: all 0.4s var(--ease-out-quart);
  position: relative;
  z-index: 2;
}

.step-item.step-active .step-dot {
  background: linear-gradient(135deg, var(--primary-500), var(--primary-400));
  border-color: var(--primary-400);
  color: #fff;
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.35);
  transform: scale(1.05);
}

.step-item.step-completed .step-dot {
  background: linear-gradient(135deg, var(--primary-600), var(--primary-500));
  border-color: var(--primary-500);
  color: #fff;
  box-shadow: 0 0 12px rgba(99, 102, 241, 0.25);
}

.step-check {
  font-size: 16px;
  animation: checkPop 0.4s var(--ease-spring);
}

@keyframes checkPop {
  0% {
    transform: scale(0);
  }
  100% {
    transform: scale(1);
  }
}

.step-line {
  width: calc(100% + 0px);
  height: 2px;
  background: rgba(255, 255, 255, 0.08);
  position: absolute;
  top: 17px;
  left: 36px;
  z-index: 1;
  transition: background 0.5s var(--ease-out-quart);
}

.step-line.line-active {
  background: linear-gradient(90deg, var(--primary-500), var(--primary-400));
}

.step-label {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding-top: 4px;
}

.step-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--neutral-300);
  transition: color 0.3s;
}

.step-item.step-active .step-title {
  color: #fff;
}
.step-item.step-completed .step-title {
  color: var(--primary-300);
}

.step-desc {
  font-size: 11px;
  color: var(--neutral-500);
  white-space: nowrap;
}

/* ===== Glass Card ===== */
.glass-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 36px;
  box-shadow: var(--shadow-lg);
  position: relative;
  overflow: hidden;
  transition: box-shadow 0.3s var(--ease-out-quart);
}

.glass-card:hover {
  box-shadow: var(--shadow-xl);
}

.card-accent {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(
    90deg,
    var(--primary-400),
    var(--primary-500),
    var(--primary-600)
  );
}

.card-accent-amber {
  background: linear-gradient(90deg, #f59e0b, #f97316, #ef4444);
}

.card-accent-green {
  background: linear-gradient(90deg, #10b981, #34d399, #06b6d4);
}

.card-header {
  margin-bottom: 28px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  color: var(--neutral-800);
  margin-bottom: 6px;
}

.card-step-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--primary-500), var(--primary-400));
  color: #fff;
  font-size: 12px;
  font-weight: 800;
}

.card-desc {
  color: var(--neutral-500);
  font-size: 14px;
  margin-left: 44px;
}

/* ===== Divider ===== */
.section-divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 28px 0 24px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    var(--neutral-200),
    transparent
  );
}

.divider-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--neutral-400);
  letter-spacing: 1px;
  text-transform: uppercase;
  white-space: nowrap;
}

/* ===== Sections ===== */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--neutral-700);
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-icon {
  font-size: 18px;
}
.section-hint {
  font-size: 13px;
  color: var(--neutral-400);
}

/* ===== Glow Button ===== */
.glow-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 28px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  font-family: inherit;
  color: #fff;
  background: linear-gradient(135deg, var(--primary-500), var(--primary-600));
  cursor: pointer;
  transition: all 0.35s var(--ease-out-quart);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
}

.glow-btn::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, var(--primary-400), var(--primary-500));
  opacity: 0;
  transition: opacity 0.35s;
}

.glow-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(99, 102, 241, 0.4);
}

.glow-btn:hover:not(:disabled)::before {
  opacity: 1;
}

.glow-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.glow-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

.glow-btn span,
.glow-btn .btn-icon {
  position: relative;
  z-index: 1;
}
.btn-icon {
  font-size: 18px;
}

.glow-btn-amber {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  box-shadow: 0 4px 16px rgba(245, 158, 11, 0.3);
}
.glow-btn-amber:hover:not(:disabled) {
  box-shadow: 0 8px 28px rgba(245, 158, 11, 0.4);
}

.glow-btn-green {
  background: linear-gradient(135deg, #10b981, #059669);
  box-shadow: 0 4px 16px rgba(16, 185, 129, 0.3);
}
.glow-btn-green:hover:not(:disabled) {
  box-shadow: 0 8px 28px rgba(16, 185, 129, 0.4);
}

.glow-btn-small {
  padding: 10px 20px;
  font-size: 13px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.loading {
  pointer-events: none;
}

.btn-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  position: relative;
  z-index: 1;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ===== Premium Input ===== */
.input-group {
  position: relative;
  margin-bottom: 16px;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  z-index: 1;
  pointer-events: none;
}

.jd-icon {
  top: 20px;
  transform: none;
}

.premium-input {
  width: 100%;
  padding: 14px 16px 14px 46px;
  border: 2px solid var(--neutral-200);
  border-radius: var(--radius-md);
  font-size: 15px;
  font-family: inherit;
  color: var(--neutral-700);
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.25s var(--ease-out-quart);
  outline: none;
}

.premium-input::placeholder {
  color: var(--neutral-400);
}

.premium-input:focus {
  border-color: var(--primary-400);
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
  background: #fff;
}

.premium-textarea {
  padding: 16px;
  resize: vertical;
  line-height: 1.6;
  min-height: 100px;
}

.chat-input-small {
  padding: 12px 16px 12px 16px;
}

/* ===== Parse Result ===== */
.parse-section {
  animation: slideUp 0.5s var(--ease-out-quart);
}

.parsed-result {
  margin-top: 24px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: var(--radius-md);
  padding: 20px;
  border: 1px solid var(--neutral-100);
}

.result-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.result-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: var(--radius-sm);
}

.result-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--neutral-400);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.result-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--neutral-700);
}

.result-section {
  margin-top: 12px;
}

.skill-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.skill-chip {
  display: inline-flex;
  padding: 4px 14px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--primary-50), var(--primary-100));
  color: var(--primary-700);
  font-size: 13px;
  font-weight: 500;
  border: 1px solid var(--primary-200);
}

.project-chip {
  padding: 8px 14px;
  margin-top: 8px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--primary-300);
  font-size: 14px;
  color: var(--neutral-600);
  line-height: 1.5;
}

/* ===== Advice ===== */
.advice-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.success-banner {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(
    135deg,
    rgba(16, 185, 129, 0.08),
    rgba(16, 185, 129, 0.03)
  );
  border-radius: var(--radius-md);
  border: 1px solid rgba(16, 185, 129, 0.15);
}

.success-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.success-banner h4 {
  font-size: 16px;
  font-weight: 700;
  color: var(--success);
  margin-bottom: 4px;
}

.success-banner p {
  font-size: 14px;
  color: var(--neutral-500);
  line-height: 1.6;
}

.suggestion-count {
  text-align: center;
  font-size: 14px;
  color: var(--neutral-400);
  margin-bottom: 20px;
}

.suggestion-count strong {
  color: var(--warning);
  font-size: 18px;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.suggestion-card {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--neutral-100);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: all 0.3s var(--ease-out-quart);
}

.suggestion-card:hover {
  border-color: var(--neutral-200);
  box-shadow: var(--shadow-sm);
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(245, 158, 11, 0.06);
  border-bottom: 1px solid var(--neutral-100);
}

.suggestion-num {
  font-size: 12px;
  font-weight: 700;
  color: var(--warning);
  background: rgba(245, 158, 11, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.suggestion-section {
  font-size: 14px;
  font-weight: 600;
  color: var(--neutral-700);
}

.suggestion-body {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.diff-block {
  padding: 10px 14px;
  border-radius: var(--radius-sm);
}

.diff-block p {
  font-size: 14px;
  line-height: 1.6;
  margin-top: 6px;
  white-space: pre-wrap;
}

.diff-original {
  background: rgba(239, 68, 68, 0.04);
  border: 1px solid rgba(239, 68, 68, 0.1);
}
.diff-original p {
  color: var(--danger);
}

.diff-suggested {
  background: rgba(16, 185, 129, 0.04);
  border: 1px solid rgba(16, 185, 129, 0.1);
}
.diff-suggested p {
  color: var(--success);
}

.diff-arrow {
  text-align: center;
  font-size: 20px;
  color: var(--neutral-300);
  line-height: 1;
}

.diff-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  letter-spacing: 0.5px;
}

.diff-bad {
  background: rgba(239, 68, 68, 0.1);
  color: var(--danger);
}
.diff-good {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success);
}

.overall-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px 20px;
  margin-top: 16px;
  background: rgba(99, 102, 241, 0.04);
  border-radius: var(--radius-md);
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.overall-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.overall-card h4 {
  font-size: 14px;
  font-weight: 700;
  color: var(--primary-600);
  margin-bottom: 4px;
}

.overall-card p {
  font-size: 14px;
  color: var(--neutral-500);
  line-height: 1.6;
}

/* ===== Advice Chat ===== */
.advice-chat-section {
  margin-top: 8px;
}
.chat-hint {
  font-size: 13px;
  color: var(--neutral-400);
  margin-bottom: 14px;
}

.chat-messages {
  max-height: 220px;
  min-height: 60px;
  overflow-y: auto;
  padding: 14px;
  background: rgba(249, 250, 251, 0.6);
  border-radius: var(--radius-md);
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-msg {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.chat-msg.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--neutral-200);
  flex-shrink: 0;
}

.chat-msg.user .msg-avatar {
  background: rgba(99, 102, 241, 0.08);
  border-color: rgba(99, 102, 241, 0.15);
}

.msg-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  font-size: 14px;
  line-height: 1.5;
}

.chat-msg:not(.user) .msg-bubble {
  background: #fff;
  border: 1px solid var(--neutral-200);
  border-bottom-left-radius: 4px;
  color: var(--neutral-700);
}

.chat-msg.user .msg-bubble {
  background: linear-gradient(135deg, var(--primary-500), var(--primary-600));
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-bubble p {
  white-space: pre-wrap;
}

.chat-input-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
/* ===== Interview Styles ===== */
.jd-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.jd-input-group {
  margin-bottom: 0;
}
.jd-upload-area {
  margin: 4px 0;
}

.jd-upload-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border: 2px dashed var(--neutral-200);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s var(--ease-out-quart);
  background: rgba(255, 255, 255, 0.4);
}

.jd-upload-trigger:hover {
  border-color: var(--primary-300);
  background: rgba(99, 102, 241, 0.04);
  border-style: solid;
}

.upload-icon-small {
  font-size: 20px;
}
.upload-text-small {
  font-size: 14px;
  color: var(--neutral-400);
}

.jd-image-preview {
  position: relative;
  display: inline-block;
}

.jd-image-preview img {
  max-height: 160px;
  border-radius: var(--radius-md);
  border: 1px solid var(--neutral-200);
}

.img-remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: var(--danger);
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s;
}

.img-remove-btn:hover {
  transform: scale(1.1);
}
.start-btn {
  align-self: flex-start;
}
.history-top-btn {
  position: absolute;
  right: 0;
  top: 0;
}

/* Modal overlay */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}
.modal-history {
  width: 680px;
}
.modal-detail {
  width: 600px;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 0;
}
.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
.modal-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #909399;
  padding: 4px 8px;
  border-radius: 4px;
}
.modal-close:hover {
  background: #f0f0f0;
  color: #303133;
}
.modal-body {
  padding: 16px 24px 24px;
  overflow-y: auto;
}
.app-header {
  position: relative;
}

/* History items */
.history-item {
  padding: 14px 16px;
  margin-bottom: 10px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.history-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.12);
}
.history-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.history-job {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.history-item-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

/* Interview Header */
.interview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--neutral-100);
}

.interview-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--success);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success);
  animation: dotBlink 1.5s ease-in-out infinite;
}

@keyframes dotBlink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

.reset-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--radius-sm);
  background: rgba(239, 68, 68, 0.06);
  color: var(--danger);
  font-size: 13px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.25s;
}

.reset-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
}

/* Chat Container */
.chat-container {
  max-height: 500px;
  min-height: 320px;
  overflow-y: auto;
  padding: 20px;
  background: rgba(249, 250, 251, 0.5);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chat-message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  animation: msgSlide 0.4s var(--ease-out-quart);
}

@keyframes msgSlide {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-message.user {
  flex-direction: row-reverse;
}

.msg-avatar-lg {
  width: 36px;
  height: 36px;
  font-size: 20px;
  flex-shrink: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--neutral-200);
}

.msg-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 75%;
}

.chat-message.user .msg-content {
  align-items: flex-end;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.chat-message.user .msg-meta {
  flex-direction: row-reverse;
}

.msg-role {
  font-size: 12px;
  font-weight: 700;
  color: var(--neutral-400);
}

.msg-tag {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: var(--radius-full);
  letter-spacing: 0.3px;
}

.tag-技术 {
  background: rgba(99, 102, 241, 0.1);
  color: var(--primary-500);
}
.tag-项目 {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success);
}
.tag-行为 {
  background: rgba(245, 158, 11, 0.1);
  color: var(--warning);
}
.tag-follow {
  background: rgba(148, 163, 184, 0.15);
  color: var(--neutral-500);
}

.msg-bubble-lg {
  padding: 14px 18px;
  border-radius: var(--radius-lg);
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.chat-message:not(.user) .msg-bubble-lg {
  background: #fff;
  border: 1px solid var(--neutral-200);
  border-bottom-left-radius: 4px;
  color: var(--neutral-700);
  box-shadow: var(--shadow-sm);
}

.chat-message.user .msg-bubble-lg {
  background: linear-gradient(135deg, var(--primary-500), var(--primary-600));
  color: #fff;
  border-bottom-right-radius: 4px;
}

.thinking {
  display: flex;
  align-items: center;
  min-height: 40px;
}

.thinking-dots {
  display: flex;
  gap: 6px;
  align-items: center;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--neutral-300);
  animation: dotPulse 1.4s ease-in-out infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}
.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dotPulse {
  0%,
  80%,
  100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}

/* Chat Input */
.chat-input-area {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input-group {
  flex: 1;
  margin-bottom: 0;
}

.chat-textarea {
  min-height: 56px;
  max-height: 120px;
  resize: none;
}

/* Evaluation */
.evaluation-section {
  margin-top: 8px;
  width: 100%;
}

.eval-card {
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.8),
    rgba(255, 255, 255, 0.5)
  );
  border: 1px solid var(--neutral-200);
  border-radius: var(--radius-lg);
  padding: 24px;
}

.eval-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.eval-header h4 {
  font-size: 16px;
  font-weight: 700;
  color: var(--neutral-700);
}

.eval-score {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.score-num {
  font-size: 32px;
  font-weight: 800;
  line-height: 1;
}
.score-unit {
  font-size: 14px;
  font-weight: 600;
}

.score-excellent .score-num {
  color: var(--success);
}
.score-good .score-num {
  color: var(--primary-500);
}
.score-fair .score-num {
  color: var(--warning);
}
.score-poor .score-num {
  color: var(--danger);
}

.eval-summary {
  font-size: 14px;
  line-height: 1.7;
  color: var(--neutral-500);
  margin-bottom: 20px;
  white-space: pre-wrap;
}

.eval-lists {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.eval-strengths,
.eval-weaknesses {
  flex: 1;
  min-width: 200px;
}

.eval-list-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.eval-list-header h5 {
  font-size: 14px;
  font-weight: 700;
}
.eval-strengths .eval-list-header h5 {
  color: var(--success);
}
.eval-weaknesses .eval-list-header h5 {
  color: var(--warning);
}

.eval-lists ul {
  list-style: none;
  padding: 0;
}

.eval-lists li {
  position: relative;
  padding: 6px 0 6px 16px;
  font-size: 14px;
  line-height: 1.5;
  color: var(--neutral-600);
}

.eval-lists li::before {
  content: "";
  position: absolute;
  left: 0;
  top: 13px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.eval-strengths li::before {
  background: var(--success);
}
.eval-weaknesses li::before {
  background: var(--warning);
}

/* ===== Bottom Navigation ===== */
.bottom-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 32px;
  padding: 0 4px;
}

.nav-spacer {
  flex: 1;
}

.nav-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.3s var(--ease-out-quart);
}

.nav-prev {
  background: rgba(255, 255, 255, 0.1);
  color: var(--neutral-300);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.nav-prev:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.nav-next {
  background: linear-gradient(135deg, var(--primary-500), var(--primary-600));
  color: #fff;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
}

.nav-next:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
}

.nav-next:disabled {
  opacity: 0.3;
  cursor: not-allowed;
  transform: none;
}

/* ===== Animations ===== */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.step-fade-enter-active,
.step-fade-leave-active {
  transition: all 0.4s var(--ease-out-quart);
}

.step-fade-enter-from {
  opacity: 0;
  transform: translateY(24px) scale(0.98);
}

.step-fade-leave-to {
  opacity: 0;
  transform: translateY(-12px) scale(0.98);
}

.slide-up-enter-active {
  transition: all 0.5s var(--ease-out-quart);
}

.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ===== Responsive ===== */
@media (max-width: 640px) {
  .home-container {
    padding: 20px 14px 40px;
  }
  .app-title {
    font-size: 26px;
  }
  .glass-card {
    padding: 24px 20px;
  }
  .step-indicator {
    gap: 0;
    padding: 0;
  }
  .step-desc {
    display: none;
  }
  .step-label {
    padding-top: 6px;
  }
  .result-grid {
    grid-template-columns: 1fr;
  }
  .eval-lists {
    flex-direction: column;
  }
  .chat-message .msg-content {
    max-width: 85%;
  }
}
</style>
