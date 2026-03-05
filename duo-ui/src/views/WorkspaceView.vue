<template>
  <div class="workspace-container">
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-button link @click="router.push('/')">
            <el-icon><ArrowLeft /></el-icon> 返回仪表盘
          </el-button>
          <div class="logo" style="margin-left: 20px;">💻 DuoStudy 沉浸空间</div>
        </div>
        <div class="user-info">
          <span>{{ currentUser.nickname }}</span>
        </div>
      </el-header>

      <el-main>
        <!-- 顶部：双人番茄钟 -->
        <el-card class="box-card pomodoro-card">
          <template #header>
            <div class="card-header">
              <span>🍅 双人同步番茄钟</span>
            </div>
          </template>
          <div class="pomodoro-wrapper">
            <!-- 我的状态 -->
            <div class="pomo-block">
              <div class="pomo-title">我的状态</div>
              <div style="font-size: 13px; color: #67c23a; margin-bottom: 5px;">🔥 今日已专注：{{ pomoStatus.mine.todayFocusMinutes || 0 }} 分钟</div>
              <div v-if="pomoStatus.mine.status === 'IDLE'" class="time-display">00:00</div>
              <div v-else class="time-display focusing">{{ mineTimeLeft }}</div>
              
              <div v-if="pomoStatus.mine.status === 'IDLE'" class="pomo-actions">
                <el-button type="primary" @click="startPomodoro(1440)">开始专注</el-button>
              </div>
              <div v-else-if="pomoStatus.mine.status === 'FOCUSING'" class="pomo-actions">
                <el-button type="warning" @click="pausePomodoro">暂停</el-button>
                <el-button type="danger" @click="stopPomodoro">结束</el-button>
              </div>
              <div v-else-if="pomoStatus.mine.status === 'PAUSED'" class="pomo-actions">
                <el-button type="success" @click="resumePomodoro">继续</el-button>
                <el-button type="danger" @click="stopPomodoro">结束</el-button>
              </div>
            </div>

            <div class="divider-vertical"></div>

            <!-- TA的状态 -->
            <div class="pomo-block">
              <div class="pomo-title">TA的状态 ({{ relationInfo?.partner?.nickname || '未绑定' }})</div>
              <template v-if="relationInfo?.partner">
                <div style="font-size: 13px; color: #67c23a; margin-bottom: 5px;">🔥 今日已专注：{{ pomoStatus.partner.todayFocusMinutes || 0 }} 分钟</div>
                <div v-if="pomoStatus.partner.status === 'IDLE'" class="time-display info">未开始专注</div>
                <div v-else class="time-display focusing">{{ partnerTimeLeft }}</div>
                <div style="margin-top: 10px; font-size: 14px; color: #909399;">
                  {{ pomoStatus.partner.status === 'IDLE' ? 'TA正在休息喔' : (pomoStatus.partner.status === 'PAUSED' ? 'TA正在暂停休息...' : '🤫 TA正在沉浸学习中...') }}
                </div>
              </template>
              <div v-else style="color: #909399; margin-top: 20px;">绑定搭子后方可查看</div>
            </div>
          </div>
        </el-card>

        <el-row :gutter="20" style="margin-top: 20px;">
          <!-- 左侧：个人笔记 -->
          <el-col :span="14">
            <el-card class="box-card note-card">
              <template #header>
                <div class="card-header">
                  <span>📝 个人草稿板/笔记 (Markdown)</span>
                  <div>
                    <el-button type="success" size="small" plain @click="shareNoteAsFile" :loading="sharingNote">共享给TA</el-button>
                    <el-button type="primary" size="small" @click="saveNote" :loading="savingNote">保存笔记</el-button>
                  </div>
                </div>
              </template>
              <el-input
                v-model="myNote.content"
                type="textarea"
                :rows="15"
                placeholder="支持Markdown格式，记录你的思考..."
                class="note-editor"
              />
            </el-card>
          </el-col>

          <!-- 右侧：资源共享区 -->
          <el-col :span="10">
            <el-card class="box-card file-card">
              <template #header>
                <div class="card-header">
                  <span>📁 双人资料库</span>
                  <el-upload
                    action="http://146.56.250.163/duo/file/upload"
                    :show-file-list="false"
                    :before-upload="beforeFileUpload"
                    :http-request="customUpload"
                  >
                    <el-button type="success" size="small" :loading="uploadingFile">上传文件</el-button>
                  </el-upload>
                </div>
              </template>
              
              <el-alert
                title="所有文件默认保存在服务器的 /uploads 或本地上传目录下，此资料库仅你们两人可见。"
                type="info"
                show-icon
                style="margin-bottom: 15px;"
                :closable="false"
              />

              <div class="file-list">
                <div v-for="file in sharedFiles" :key="file.id" class="file-item">
                  <div class="file-info">
                    <el-icon><Document /></el-icon>
                    <a :href="getFileFullUrl(file.fileUrl)" target="_blank" class="file-name">{{ file.fileName }}</a>
                    <span class="file-size">{{ formatSize(file.fileSize) }}</span>
                  </div>
                  <div class="file-meta">
                    <span class="file-uploader">{{ file.uploaderId === currentUser.id ? '我' : 'TA' }}上传于 {{ new Date(file.createTime).toLocaleDateString() }}</span>
                    <el-button v-if="file.uploaderId === currentUser.id" type="danger" link size="small" @click="deleteFile(file.id)">删除</el-button>
                  </div>
                </div>
                <div v-if="sharedFiles.length === 0" class="empty-files">
                  暂无共享资料，快来上传吧！
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { ArrowLeft, Document } from '@element-plus/icons-vue'

const router = useRouter()
const currentUser = ref<any>({})
const relationInfo = ref<any>(null)

const myNote = ref<any>({ content: '' })
const savingNote = ref(false)
const sharingNote = ref(false)

const sharedFiles = ref<any[]>([])
const uploadingFile = ref(false)

const API_BASE = 'http://146.56.250.163/duo'
const getFileFullUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return API_BASE + url
}

const pomoStatus = ref({
  mine: { status: 'IDLE', startTime: 0, durationMinutes: 0, todayFocusMinutes: 0, accumulatedFocusMs: 0 },
  partner: { status: 'IDLE', startTime: 0, durationMinutes: 0, todayFocusMinutes: 0, accumulatedFocusMs: 0 }
})
const mineTimeLeft = ref('00:00')
const partnerTimeLeft = ref('00:00')
let pomoTimer: any = null
let syncTimer: any = null

const loadUserInfo = () => {
  const userStr = localStorage.getItem('userInfo')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
    fetchRelation()
    loadNote()
  } else {
    router.push('/login')
  }
}

const fetchRelation = async () => {
  try {
    const res: any = await request.get(`/couple/info?currentUserId=${currentUser.value.id}`)
    relationInfo.value = res.data
    if (relationInfo.value) {
      loadSharedFiles()
    }
  } catch (err) {
    console.error(err)
  } finally {
    // ensure initial status is fetched after relation info might or might not resolve
    syncPomodoroStatus()
  }
}

// 笔记
const loadNote = async () => {
  try {
    const res: any = await request.get(`/note/get?userId=${currentUser.value.id}`)
    myNote.value = res.data
  } catch(e) { console.error(e) }
}

const saveNote = async () => {
  savingNote.value = true
  try {
    await request.post('/note/save', myNote.value)
    ElMessage.success('笔记已保存')
  } catch(e) {
    ElMessage.error('保存失败')
  } finally {
    savingNote.value = false
  }
}

const shareNoteAsFile = async () => {
  if (!myNote.value.content || !relationInfo.value?.partner) {
    ElMessage.warning('笔记内容为空或未绑定搭子')
    return
  }
  sharingNote.value = true
  try {
    const blob = new Blob([myNote.value.content], { type: 'text/markdown;charset=utf-8' })
    const file = new File([blob], `大神的笔记_${new Date().getTime()}.md`, { type: 'text/markdown' })
    
    const formData = new FormData()
    formData.append('file', file)
    
    const uploadRes: any = await request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    if (uploadRes.code === 200) {
      const fileUrl = uploadRes.data
      await request.post('/sharedFile/add', {
        uploaderId: currentUser.value.id,
        partnerId: relationInfo.value.partner.id,
        fileName: file.name,
        fileUrl: fileUrl,
        fileSize: file.size
      })
      ElMessage.success('笔记已成功共享为文件')
      loadSharedFiles()
    } else {
      ElMessage.error(uploadRes.msg || '共享失败')
    }
  } catch (e) {
    ElMessage.error('共享出错')
  } finally {
    sharingNote.value = false
  }
}

// 文件
const formatSize = (bytes: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const loadSharedFiles = async () => {
  if (!relationInfo.value?.partner) return
  try {
    const res: any = await request.get(`/sharedFile/list?currentUserId=${currentUser.value.id}&partnerId=${relationInfo.value.partner.id}`)
    sharedFiles.value = res.data
  } catch(e) {}
}

const beforeFileUpload = (file: any) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) ElMessage.error('文件大小不能超过 10MB!')
  return isLt10M
}

const customUpload = async (params: any) => {
  const file = params.file
  const formData = new FormData()
  formData.append('file', file)
  
  uploadingFile.value = true
  try {
    const uploadRes: any = await request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (uploadRes.code === 200) {
      const fileUrl = uploadRes.data
      await request.post('/sharedFile/add', {
        uploaderId: currentUser.value.id,
        partnerId: relationInfo.value.partner.id,
        fileName: file.name,
        fileUrl: fileUrl,
        fileSize: file.size
      })
      ElMessage.success('上传成功')
      loadSharedFiles()
    } else {
      ElMessage.error(uploadRes.msg || '上传失败')
    }
  } catch (e) {
    ElMessage.error('上传出错')
  } finally {
    uploadingFile.value = false
  }
}

const deleteFile = async (id: number) => {
  try {
    await request.delete(`/sharedFile/delete/${id}?currentUserId=${currentUser.value.id}`)
    ElMessage.success('已删除')
    loadSharedFiles()
  } catch(e) {}
}

// 番茄钟
const syncPomodoroStatus = async () => {
  try {
    const pid = relationInfo.value?.partner?.id || ''
    const res: any = await request.get(`/pomodoro/status?currentUserId=${currentUser.value.id}&partnerId=${pid}`)
    pomoStatus.value = res.data
    updateLocalTimer()
  } catch(e) {}
}

const startPomodoro = async (mins: number) => {
  try {
    await request.post(`/pomodoro/start?currentUserId=${currentUser.value.id}&durationMinutes=${mins}`)
    syncPomodoroStatus()
  } catch(e) {}
}

const stopPomodoro = async () => {
  try {
    await request.post(`/pomodoro/stop?currentUserId=${currentUser.value.id}`)
    syncPomodoroStatus()
  } catch(e) {}
}

const pausePomodoro = async () => {
  try {
    await request.post(`/pomodoro/pause?currentUserId=${currentUser.value.id}`)
    syncPomodoroStatus()
  } catch(e) {}
}

const resumePomodoro = async () => {
  try {
    await request.post(`/pomodoro/resume?currentUserId=${currentUser.value.id}`)
    syncPomodoroStatus()
  } catch(e) {}
}

const calculateTick = (state: any) => {
  if (state.status === 'IDLE') return '00:00'
  
  let diff = state.accumulatedFocusMs || 0
  if (state.status === 'FOCUSING') {
    const now = Date.now()
    diff += (now - state.startTime)
  }
  
  const m = Math.floor(diff / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const updateLocalTimer = () => {
  mineTimeLeft.value = calculateTick(pomoStatus.value.mine)
  if (pomoStatus.value.partner) {
    partnerTimeLeft.value = calculateTick(pomoStatus.value.partner)
  }
}

onMounted(() => {
  // Clear any existing timers just in case to avoid duplicates across hmr/route changes
  if (syncTimer) clearInterval(syncTimer)
  if (pomoTimer) clearInterval(pomoTimer)
  
  loadUserInfo()
  
  syncTimer = setInterval(syncPomodoroStatus, 3000)
  pomoTimer = setInterval(updateLocalTimer, 1000)
})

onUnmounted(() => {
  if (syncTimer) { clearInterval(syncTimer); syncTimer = null; }
  if (pomoTimer) { clearInterval(pomoTimer); pomoTimer = null; }
})
</script>

<style scoped>
.workspace-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.box-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.pomodoro-wrapper {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.pomo-block {
  text-align: center;
  flex: 1;
}

.divider-vertical {
  width: 1px;
  height: 120px;
  background-color: #ebeef5;
}

.pomo-title {
  font-size: 16px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 15px;
}

.time-display {
  font-size: 48px;
  font-family: monospace;
  font-weight: bold;
  color: #303133;
  margin-bottom: 15px;
}

.time-display.focusing {
  color: #f56c6c;
}

.time-display.info {
  font-size: 24px;
  color: #909399;
}

.note-editor {
  font-family: monospace;
  font-size: 14px;
}

.file-list {
  max-height: 400px;
  overflow-y: auto;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
}

.file-item:last-child {
  border-bottom: none;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-name {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
}

.file-name:hover {
  text-decoration: underline;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.file-meta {
  display: flex;
  align-items: center;
  gap: 15px;
}

.file-uploader {
  font-size: 12px;
  color: #909399;
}

.empty-files {
  text-align: center;
  color: #909399;
  padding: 30px 0;
  font-size: 14px;
}
</style>
