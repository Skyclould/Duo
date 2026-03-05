<template>
  <el-container class="calendar-container">
    <el-header class="header">
      <div class="header-left">
        <el-button link @click="router.push('/dashboard')">
          <el-icon><ArrowLeft /></el-icon> 返回工作台
        </el-button>
        <div class="logo-area" @click="router.push('/dashboard')" style="cursor: pointer; margin-left: 20px;">
          <span class="logo-text">Duo<span class="logo-highlight">Study</span> 🗓️ 学习日历</span>
        </div>
      </div>
      <div class="header-right">
        <span class="user-greeting">欢迎回来, {{ currentUser.nickname || currentUser.username }}</span>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-row :gutter="24" style="height: 100%;">
        <el-col :span="14" style="height: 100%; display: flex; flex-direction: column;">
          <el-card class="box-card calendar-card" style="flex: 1;">
            <template #header>
              <div class="card-header">
                <span style="font-size: 18px; font-weight: bold;">📅 总体回顾</span>
                <div class="stats-badge" v-if="studyStats.totalDays > 0">
                  🔥 连续打卡 <strong style="color: #e6a23c;">{{ studyStats.streak }}</strong> 天 · 累计 <strong>{{ studyStats.totalDays }}</strong> 天 · 共 <strong>{{ studyStats.totalHours }}</strong> 小时
                </div>
              </div>
            </template>
            <el-calendar v-model="selectedCheckInDate" class="custom-calendar full-size-calendar">
              <template #date-cell="{ data }">
                <div :class="['calendar-cell', data.isSelected ? 'is-selected' : '']">
                  <span class="day-number">{{ data.day.split('-').slice(2).join('') }}</span>
                  <div class="dot-row">
                    <span v-if="hasCheckIn(data.day)" class="checkin-dot mine">🟢</span>
                    <span v-if="getCheckInCount(data.day) > 1" class="checkin-count">×{{ getCheckInCount(data.day) }}</span>
                    <span v-if="hasPartnerCheckIn(data.day)" class="checkin-dot partner">🔵</span>
                  </div>
                </div>
              </template>
            </el-calendar>
          </el-card>
        </el-col>
        <el-col :span="10" style="height: 100%;">
          <el-card class="box-card detail-card" style="height: 100%; display: flex; flex-direction: column;">
            <h3 class="detail-title">
              {{ selectedDateStr }} 学习详情
            </h3>
            
            <div class="detail-content" style="flex: 1; display: flex; flex-direction: column; overflow: hidden;">
              <el-tabs v-model="activeCheckInTab" class="custom-tabs" style="flex: 1; display: flex; flex-direction: column;">
                <el-tab-pane label="我的记录" name="mine" style="height: 100%; overflow-y: auto;">
                  <div v-if="getCheckInsForDate(selectedDateStr).length > 0" class="record-list">
                    <div v-for="(record, idx) in getCheckInsForDate(selectedDateStr)" :key="record.id" class="record-item mine-color">
                      <div class="record-header">
                        <span class="record-time">
                          #{{ Number(idx) + 1 }}　{{ record.timeRange || `${record.durationMinutes || 0}分钟` }}
                        </span>
                      </div>
                      <p class="record-text">{{ record.content }}</p>
                      <!-- 附件展示 -->
                      <div v-if="record.fileUrl" class="record-file">
                          <template v-if="isImageFile(record.fileUrl)">
                            <el-image :src="getFileFullUrl(record.fileUrl)" class="preview-img" :preview-src-list="[getFileFullUrl(record.fileUrl)]" fit="contain" />
                          </template>
                          <template v-else-if="isPdfFile(record.fileUrl)">
                            <iframe :src="getFileFullUrl(record.fileUrl)" class="preview-pdf"></iframe>
                          </template>
                          <template v-else>
                            <span class="file-link">📎 {{ getFileName(record.fileUrl) }}</span>
                          </template>
                          <div class="file-actions">
                            <el-button size="small" type="primary" link @click="previewFile(record.fileUrl)">👁 预览</el-button>
                            <el-button size="small" type="success" link @click="downloadFile(record.fileUrl)">⬇ 下载</el-button>
                          </div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="empty-state">
                    暂无记录
                  </div>
                </el-tab-pane>

                <el-tab-pane label="TA的记录" name="partner" :disabled="!relationInfo?.partner" style="height: 100%; overflow-y: auto;">
                  <div v-if="getPartnerCheckInsForDate(selectedDateStr).length > 0" class="record-list">
                    <div v-for="(record, idx) in getPartnerCheckInsForDate(selectedDateStr)" :key="record.id" class="record-item partner-color">
                      <div class="record-header">
                        <span class="record-time" style="color: #67c23a;">
                          #{{ Number(idx) + 1 }}　{{ record.timeRange || `${record.durationMinutes || 0}分钟` }}
                        </span>
                      </div>
                      <p class="record-text">{{ record.content }}</p>
                      <!-- 附件展示 -->
                      <div v-if="record.fileUrl" class="record-file">
                          <template v-if="isImageFile(record.fileUrl)">
                            <el-image :src="getFileFullUrl(record.fileUrl)" class="preview-img" :preview-src-list="[getFileFullUrl(record.fileUrl)]" fit="contain" />
                          </template>
                          <template v-else-if="isPdfFile(record.fileUrl)">
                            <iframe :src="getFileFullUrl(record.fileUrl)" class="preview-pdf"></iframe>
                          </template>
                          <template v-else>
                            <span class="file-link">📎 {{ getFileName(record.fileUrl) }}</span>
                          </template>
                          <div class="file-actions">
                            <el-button size="small" type="primary" link @click="previewFile(record.fileUrl)">👁 预览</el-button>
                            <el-button size="small" type="success" link @click="downloadFile(record.fileUrl)">⬇ 下载</el-button>
                          </div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="empty-state">
                    TA暂无记录
                  </div>
                </el-tab-pane>
              </el-tabs>

              <!-- 新增记录表单（始终可见） -->
              <div class="add-record-area">
                <h4 class="add-title">➕ 补录/添加新记录</h4>
                <el-form label-position="top" size="default">
                  <el-form-item label="时间段" style="margin-bottom: 12px;">
                    <el-time-picker
                      v-model="checkInForm.timeRangeArr"
                      is-range
                      range-separator="至"
                      start-placeholder="开始"
                      end-placeholder="结束"
                      format="HH:mm"
                      style="width: 100%"
                    />
                  </el-form-item>
                  <el-form-item label="学习心得" style="margin-bottom: 12px;">
                    <el-input type="textarea" v-model="checkInForm.content" placeholder="记录一下学习感悟..." rows="3"></el-input>
                  </el-form-item>
                  <div style="display: flex; gap: 15px; align-items: flex-end;">
                    <el-form-item label="上传附件" style="flex: 1; margin-bottom: 0;">
                      <el-upload
                        ref="uploadRef"
                        action="http://146.56.250.163/duo/file/upload"
                        :on-success="handleUploadSuccess"
                        :on-error="handleUploadError"
                        :limit="1"
                        :show-file-list="true"
                        name="file"
                        :headers="uploadHeaders"
                        accept=".jpg,.jpeg,.png,.gif,.webp,.pdf,.txt,.md,.doc,.docx,.csv"
                      >
                        <el-button type="default" size="default">📎 选择文件</el-button>
                        <template #tip>
                          <div class="upload-tip">图片/文档, ≤10MB</div>
                        </template>
                      </el-upload>
                    </el-form-item>
                    <el-button type="primary" style="width: 120px; height: 40px;" @click="submitCheckIn" :loading="submittingCheckIn">
                      提交打卡
                    </el-button>
                  </div>
                </el-form>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const currentUser = ref<any>({})
const relationInfo = ref<any>(null)

const checkInStatus = ref<any>({ mine: false, partner: false, myRecords: [], partnerRecords: [] })
const submittingCheckIn = ref(false)
const checkInForm = ref<any>({ durationMinutes: 60, timeRangeArr: [new Date(), new Date()], content: '', fileUrl: '', timeRange: '', checkInDate: '' })
const uploadRef = ref<any>(null)
const activeCheckInTab = ref('mine')

const studyStats = computed(() => {
  const records = checkInStatus.value.myRecords || []
  const dateSet = new Set(records.map((r: any) => r.checkInDate))
  const totalDays = dateSet.size
  const totalMinutes = records.reduce((sum: number, r: any) => sum + (r.durationMinutes || 0), 0)
  const totalHours = Math.round(totalMinutes / 60 * 10) / 10

  // Calculate streak from today backwards
  let streak = 0
  const d = new Date()
  while (true) {
    const ds = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
    if (dateSet.has(ds)) {
      streak++
      d.setDate(d.getDate() - 1)
    } else {
      break
    }
  }
  return { totalDays, totalHours, streak, totalRecords: records.length }
})

const selectedCheckInDate = ref(new Date())

const selectedDateStr = computed(() => {
  const d = selectedCheckInDate.value
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
})

const hasCheckIn = (dayStr: string) => {
  return checkInStatus.value.myRecords?.some((r: any) => r.checkInDate === dayStr)
}

const hasPartnerCheckIn = (dayStr: string) => {
  return checkInStatus.value.partnerRecords?.some((r: any) => r.checkInDate === dayStr)
}

const getCheckInsForDate = (dayStr: string) => {
  return (checkInStatus.value.myRecords || []).filter((r: any) => r.checkInDate === dayStr)
}

const getPartnerCheckInsForDate = (dayStr: string) => {
  return (checkInStatus.value.partnerRecords || []).filter((r: any) => r.checkInDate === dayStr)
}

const getCheckInCount = (dayStr: string) => {
  return getCheckInsForDate(dayStr).length
}

const API_BASE = 'http://146.56.250.163/duo'
const getFileFullUrl = (fileUrl: string) => API_BASE + fileUrl
const getFileName = (fileUrl: string) => {
  if (!fileUrl) return ''
  return fileUrl.split('/').pop() || fileUrl
}

const isImageFile = (fileUrl: string) => /\.(jpeg|jpg|gif|png|webp|bmp|svg)$/i.test(fileUrl)
const isPdfFile = (fileUrl: string) => /\.pdf$/i.test(fileUrl)

const previewFile = (fileUrl: string) => window.open(getFileFullUrl(fileUrl), '_blank')

const downloadFile = async (fileUrl: string) => {
  try {
    const fullUrl = getFileFullUrl(fileUrl)
    const response = await fetch(fullUrl)
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.style.display = 'none'
    a.href = url
    a.download = getFileName(fileUrl)
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
  } catch (err) {
    ElMessage.error('下载失败，请稍后再试')
  }
}

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const handleUploadSuccess = (response: any) => {
  if (response.code === 200) {
    checkInForm.value.fileUrl = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('文件上传失败，请检查后端是否启动')
}

const loadUserInfo = () => {
  const userStr = localStorage.getItem('userInfo')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
    fetchRelation()
  } else {
    router.push('/login')
  }
}

const fetchRelation = async () => {
  try {
    const res: any = await request.get(`/couple/info?currentUserId=${currentUser.value.id}`)
    relationInfo.value = res.data
    loadCheckInStatus()
  } catch (err: any) {
    relationInfo.value = null
  }
}

const loadCheckInStatus = async () => {
  try {
    const pId = relationInfo.value?.partner?.id || ''
    const res: any = await request.get(`/checkin/status?currentUserId=${currentUser.value.id}&partnerId=${pId}`)
    checkInStatus.value = res.data
  } catch (err) {
    console.error(err)
  }
}

const submitCheckIn = async () => {
  if (!checkInForm.value.content) {
    ElMessage.warning('请输入学习心得')
    return
  }
  
  if (checkInForm.value.timeRangeArr && checkInForm.value.timeRangeArr.length === 2) {
    const start = checkInForm.value.timeRangeArr[0]
    const end = checkInForm.value.timeRangeArr[1]
    const formatTime = (d: Date) => String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
    checkInForm.value.timeRange = `${formatTime(start)} - ${formatTime(end)}`
    checkInForm.value.durationMinutes = Math.abs((end.getTime() - start.getTime()) / 60000)
  }

  checkInForm.value.checkInDate = selectedDateStr.value
  submittingCheckIn.value = true
  try {
    await request.post(`/checkin/submit?currentUserId=${currentUser.value.id}`, checkInForm.value)
    ElMessage.success('打卡成功！')
    checkInForm.value.content = ''
    checkInForm.value.fileUrl = ''
    if (uploadRef.value) uploadRef.value.clearFiles()
    loadCheckInStatus()
  } catch (err) {
    console.error(err)
  } finally {
    submittingCheckIn.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.calendar-container {
  height: 100vh;
  background-color: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  z-index: 10;
}
.header-left {
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  color: #303133;
}
.logo-highlight {
  color: #409eff;
}
.user-greeting {
  font-size: 14px;
  color: #606266;
  margin-right: 20px;
}

.main-content {
  flex: 1;
  padding: 30px 40px;
  overflow: hidden;
}

.box-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
}

.stats-badge {
  font-size: 14px;
  color: #606266;
  background: #fdf6ec;
  padding: 6px 15px;
  border-radius: 20px;
  display: inline-block;
  margin-left: 20px;
}

/* Custom Calendar full height */
:deep(.full-size-calendar.el-calendar) {
  height: 100%;
  display: flex;
  flex-direction: column;
}
:deep(.full-size-calendar .el-calendar__body) {
  flex: 1;
  padding: 10px;
}
:deep(.full-size-calendar .el-calendar-table) {
  height: 100%;
}
:deep(.full-size-calendar .el-calendar-table .el-calendar-day) {
  height: auto !important;
  min-height: 80px;
  padding: 8px;
}
:deep(.full-size-calendar .el-calendar-table td.is-selected) {
  background-color: #ecf5ff;
}

.calendar-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}
.day-number {
  font-size: 16px;
  font-weight: bold;
}

.dot-row {
  display: flex;
  gap: 4px;
  margin-top: auto;
  align-items: center;
}
.checkin-dot {
  font-size: 12px;
}
.checkin-count {
  font-size: 11px;
  color: #409eff;
  font-weight: bold;
  background: #ecf5ff;
  padding: 1px 4px;
  border-radius: 4px;
}

.detail-title {
  margin-top: 0;
  font-size: 18px;
  border-bottom: 2px solid #f0f2f5;
  padding-bottom: 15px;
  margin-bottom: 15px;
}

:deep(.custom-tabs > .el-tabs__content) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
:deep(.custom-tabs > .el-tabs__content > .el-tab-pane) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.record-list {
  flex: 1;
  padding-right: 10px;
}

.record-item {
  padding: 12px;
  margin-bottom: 12px;
  background: #fdfdfd;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.mine-color { border-left: 4px solid #409eff; }
.partner-color { border-left: 4px solid #67c23a; }

.record-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}
.record-time {
  font-weight: bold;
  font-size: 14px;
  color: #409eff;
}
.record-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  margin: 8px 0;
}

.record-file {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
}
.preview-img { max-width: 100%; max-height: 120px; border-radius: 6px; border: 1px solid #ebeef5; }
.preview-pdf { width: 100%; height: 160px; border: 1px solid #ebeef5; border-radius: 6px; }
.file-link { font-size: 13px; color: #909399; }
.file-actions { margin-top: 6px; display: flex; gap: 10px; }

.empty-state {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-top: 40px;
}

.add-record-area {
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
  margin-top: 10px;
  background: #fff;
}
.add-title {
  margin: 0 0 15px;
  font-size: 15px;
  color: #303133;
}
.upload-tip { font-size: 12px; color: #909399; margin-top: 5px; }
</style>
