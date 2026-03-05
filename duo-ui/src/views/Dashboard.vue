<template>
  <div class="dashboard-container">
    <el-container>
      <el-header class="app-header">
        <div class="logo">👫 DuoStudy</div>
        <div class="user-info">
          <span>欢迎回来，{{ currentUser.nickname }}</span>
          <el-button link type="danger" @click="handleLogout">登出</el-button>
        </div>
      </el-header>

      <el-main>
        <el-row :gutter="20">
          <!-- 个人卡片 -->
          <el-col :span="8">
            <el-card class="box-card user-card">
              <template #header>
                <div class="card-header">
                  <span>我的档案</span>
                </div>
              </template>
              <div class="user-profile">
                <el-upload
                  class="avatar-uploader"
                  action="http://146.56.250.163/duo/file/upload"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                  :headers="uploadHeaders"
                >
                  <img v-if="currentUser.avatar" :src="getFileFullUrl(currentUser.avatar)" class="avatar" />
                  <div v-else class="avatar-placeholder">上传头像</div>
                </el-upload>
                <div class="info-item" style="margin-top: 10px;"><strong>账号：</strong>{{ currentUser.username }}</div>
              </div>
              <div class="info-item">
                <strong>专属邀请码：</strong>
                <el-tag type="success">{{ currentUser.inviteCode }}</el-tag>
                <el-button link size="small" @click="copy(currentUser.inviteCode)">复制</el-button>
              </div>
              <div class="learning-plan-card" style="margin-top: 20px;">
                <el-card class="box-card">
                  <template #header>
                    <div class="card-header">
                      <span>我的学习计划</span>
                      <span v-if="myPlans.length > 0" style="font-size: 12px; color: #909399;">
                        {{ completedPlanCount }}/{{ myPlans.length }}
                      </span>
                    </div>
                  </template>
                  <!-- 进度条 -->
                  <el-progress v-if="myPlans.length > 0" :percentage="planProgress" :stroke-width="8" :color="planProgressColor" style="margin-bottom: 12px;" />
                  <!-- 添加表单 -->
                  <div style="margin-bottom: 12px;">
                    <div style="display: flex; gap: 8px; margin-bottom: 6px;">
                      <el-input v-model="newPlanContent" placeholder="添加新计划..." @keyup.enter="handleAddPlan" size="small" style="flex: 1;"></el-input>
                      <el-button type="primary" size="small" @click="handleAddPlan">添加</el-button>
                    </div>
                    <el-date-picker v-model="newPlanDeadline" type="date" placeholder="截止日期（可选）" size="small" style="width: 100%;" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
                  </div>
                  <div class="plan-list">
                     <div v-for="plan in sortedPlans" :key="plan.id" class="plan-item" :style="{ borderLeft: getPlanBorderColor(plan) }">
                        <el-checkbox :model-value="plan.status === 1" @change="handleTogglePlan(plan)"></el-checkbox>
                        <div style="margin-left: 10px; flex: 1; min-width: 0;">
                          <span :class="{'completed-plan': plan.status === 1}" style="font-size: 14px; display: block;">{{ plan.content }}</span>
                          <span v-if="plan.deadline" :style="{ fontSize: '11px', color: isOverdue(plan) ? '#f56c6c' : '#909399' }">
                            ⏰ {{ formatDeadline(plan.deadline) }}{{ isOverdue(plan) ? ' (已逾期!)' : '' }}
                          </span>
                        </div>
                        <el-button type="danger" link size="small" @click="handleDeletePlan(plan.id)">删除</el-button>
                     </div>
                     <div v-if="myPlans.length === 0" style="text-align: center; color: #999; font-size: 13px; margin-top: 10px;">暂无计划，快来添加吧~</div>
                  </div>
                </el-card>

                <!-- TA的学习计划 -->
                <el-card v-if="partnerPlans.length > 0" class="box-card" style="margin-top: 15px;">
                  <template #header>
                    <div class="card-header">
                      <span>TA的学习计划</span>
                      <span style="font-size: 12px; color: #909399;">
                        {{ partnerPlans.filter((p: any) => p.status === 1).length }}/{{ partnerPlans.length }}
                      </span>
                    </div>
                  </template>
                  <el-progress :percentage="Math.round(partnerPlans.filter((p: any) => p.status === 1).length / partnerPlans.length * 100)" :stroke-width="8" :color="Math.round(partnerPlans.filter((p: any) => p.status === 1).length / partnerPlans.length * 100) >= 100 ? '#67c23a' : '#409eff'" style="margin-bottom: 12px;" />
                  <div>
                    <div v-for="pp in partnerPlans" :key="pp.id" style="display: flex; align-items: center; padding: 6px 10px; font-size: 14px; background: #f9fafb; margin-bottom: 8px; border-radius: 4px; border-left: 3px solid #67c23a;">
                      <span :style="{ color: pp.status === 1 ? '#67c23a' : '#c0c4cc' }">{{ pp.status === 1 ? '✅' : '⬜' }}</span>
                      <span :style="{ marginLeft: '10px', textDecoration: pp.status === 1 ? 'line-through' : 'none', color: pp.status === 1 ? '#c0c4cc' : '#606266' }">{{ pp.content }}</span>
                      <span v-if="pp.deadline" style="margin-left: auto; font-size: 11px; color: #909399;">⏰ {{ formatDeadline(pp.deadline) }}</span>
                    </div>
                  </div>
                </el-card>
              </div>
            </el-card>
          </el-col>

          <!-- 搭子卡片 (动态调节宽带) -->
          <el-col :span="relationInfo && isChatVisible ? 8 : 16">
            <!-- 已有搭子 -->
            <el-card v-if="relationInfo" class="box-card couple-card">
              <template #header>
                <div class="card-header">
                  <span>双人空间</span>
                  <el-tag type="danger">绑定中</el-tag>
                </div>
              </template>
              <div class="relation-info">
                <template v-if="relationInfo.partner">
                  <h3>你的搭子：{{ relationInfo.partner.nickname }}</h3>
                  <p>对方账号：{{ relationInfo.partner.username }}</p>
                </template>
                <div class="divider"></div>
                <h4>共同目标：{{ relationInfo.relation.sharedGoal }}</h4>
                <p>绑定时间：{{ new Date(relationInfo.relation.createTime).toLocaleDateString() }}</p>
                <div class="checkin-status" style="margin: 15px 0;">
                  <div style="display: flex; justify-content: center; gap: 10px; align-items: center; flex-wrap: wrap;">
                    <el-tag :type="checkInStatus.mine ? 'success' : 'info'" size="small">
                      我 {{ checkInStatus.mine ? '✅ 已打卡' : '⬜ 未打卡' }}
                    </el-tag>
                    <el-tag :type="checkInStatus.partner ? 'success' : 'info'" size="small">
                      TA {{ checkInStatus.partner ? '✅ 已打卡' : '⬜ 未打卡' }}
                    </el-tag>
                  </div>
                  <div v-if="studyStats.totalDays > 0" style="text-align: center; margin-top: 10px; font-size: 13px; color: #606266;">
                    🔥 连续打卡 <strong style="color: #e6a23c;">{{ studyStats.streak }}</strong> 天 · 累计 <strong>{{ studyStats.totalDays }}</strong> 天 · 共 <strong>{{ studyStats.totalHours }}</strong> 小时
                  </div>
                </div>

                <div class="action-buttons">
                  <el-button type="primary" plain @click="router.push('/calendar')">
                    📅 学习日历
                  </el-button>
                  <el-button color="#626aef" plain @click="router.push('/workspace')">
                    💻 沉浸空间
                  </el-button>
                  <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="item">
                    <el-button type="info" plain @click="toggleChat">
                      {{ isChatVisible ? '收起纸条' : '留心情纸条' }}
                    </el-button>
                  </el-badge>
                  <el-button type="danger" @click="handleUnbind">残忍解绑</el-button>
                </div>
              </div>
            </el-card>

            <!-- 还没有搭子: 去绑定 -->
            <el-card v-else class="box-card couple-card bind-card">
              <template #header>
                <div class="card-header">
                  <span>寻找搭子</span>
                </div>
              </template>
              <div class="bind-form">
                <el-alert title="您还未绑定搭子，一个人学习太孤单啦！" type="warning" show-icon />
                <br />
                <el-form label-position="top">
                  <el-form-item label="输入对方的邀请码">
                    <el-input v-model="bindForm.inviteCode" placeholder="输入6位大写邀请码"></el-input>
                  </el-form-item>
                  <el-form-item label="我们的共同目标">
                    <el-input v-model="bindForm.sharedGoal" placeholder="比如：一起考研上岸、一起前端开发"></el-input>
                  </el-form-item>
                  <el-button type="primary" @click="handleBind" :loading="bindingLoading">确认绑定</el-button>
                </el-form>
              </div>
            </el-card>
          </el-col>

          <!-- 留言板卡片 (常驻可折叠) -->
          <el-col :span="8" v-if="relationInfo && isChatVisible">
            <el-card class="box-card chat-card transition-card">
              <template #header>
                <div class="card-header">
                  <span>心情纸条</span>
                  <el-tag type="success" size="small">实时在线</el-tag>
                </div>
              </template>
              <div class="message-list" ref="messageListRef">
                <div v-for="msg in messages" :key="msg.id" :class="['message-item', msg.senderId === currentUser.id ? 'my-message' : 'partner-message']">
                  <div class="message-wrapper">
                    <div class="message-sender">
                      {{ msg.senderId === currentUser.id ? currentUser.nickname : (relationInfo?.partner?.nickname || 'TA') }}
                    </div>
                    <div class="message-bubble">
                      <div class="message-content">{{ msg.content }}</div>
                      <div class="message-time">
                        {{ new Date(msg.createTime).toLocaleString([], {hour: '2-digit', minute:'2-digit'}) }}
                        <!-- 如果是我发送的，显示已读未读状态 -->
                        <span v-if="msg.senderId === currentUser.id" :class="['read-status', msg.isRead === 1 ? 'read' : 'unread']">
                          {{ msg.isRead === 1 ? '已读' : '未读' }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 占位提示 -->
                <div v-if="messages.length === 0" class="empty-messages">
                  💌 快给对方留个纸条吧~
                </div>
              </div>
              <div class="message-input">
                <div style="display: flex; gap: 8px;">
                  <el-input v-model="newMessage" placeholder="写点什么..." @keyup.enter="sendMessage" maxlength="500">
                    <template #append>
                      <el-button type="primary" @click="sendMessage" :disabled="!newMessage.trim()">发送</el-button>
                    </template>
                  </el-input>
                  <el-button @click="pokePartner" circle title="拍一拍TA">👋</el-button>
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
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const currentUser = ref<any>({})
const relationInfo = ref<any>(null)

const API_BASE = 'http://146.56.250.163/duo'
const getFileFullUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return API_BASE + url
}
const bindForm = ref({ inviteCode: '', sharedGoal: '' })
const bindingLoading = ref(false)
const isChatVisible = ref(false)

const messages = ref<any[]>([])
const newMessage = ref('')
const messageListRef = ref<any>(null)
const chatTimer = ref<any>(null)

const checkInStatus = ref<any>({ mine: false, partner: false, myRecords: [], partnerRecords: [] })

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



const myPlans = ref<any[]>([])
const partnerPlans = ref<any[]>([])
const newPlanContent = ref('')
const newPlanDeadline = ref('')

const completedPlanCount = computed(() => myPlans.value.filter((p: any) => p.status === 1).length)
const planProgress = computed(() => myPlans.value.length ? Math.round(completedPlanCount.value / myPlans.value.length * 100) : 0)
const planProgressColor = computed(() => {
  if (planProgress.value >= 100) return '#67c23a'
  if (planProgress.value >= 50) return '#e6a23c'
  return '#409eff'
})

const sortedPlans = computed(() => {
  return [...myPlans.value].sort((a: any, b: any) => {
    if (a.status !== b.status) return a.status - b.status
    if (a.deadline && b.deadline) return new Date(a.deadline).getTime() - new Date(b.deadline).getTime()
    if (a.deadline) return -1
    if (b.deadline) return 1
    return 0
  })
})

const isOverdue = (plan: any) => {
  if (!plan.deadline || plan.status === 1) return false
  return new Date(plan.deadline) < new Date()
}

const formatDeadline = (deadline: string) => {
  if (!deadline) return ''
  const d = new Date(deadline)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const getPlanBorderColor = (plan: any) => {
  if (plan.status === 1) return '3px solid #67c23a'
  if (isOverdue(plan)) return '3px solid #f56c6c'
  if (plan.deadline) return '3px solid #e6a23c'
  return '3px solid #ebeef5'
}

const unreadCount = computed(() => {
  return messages.value.filter((msg: any) => msg.receiverId === currentUser.value.id && msg.isRead === 0).length
})

const toggleChat = async () => {
  isChatVisible.value = !isChatVisible.value
  if (isChatVisible.value) {
    await loadMessages()
    nextTick(() => {
      if (messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight
      }
    })
  }
}

const beforeAvatarUpload = (file: any) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('头像必须是图片格式!')
  if (!isLt5M) ElMessage.error('头像大小不能超过 5MB!')
  return isImage && isLt5M
}

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const handleAvatarSuccess = async (res: any) => {
  if (res.code === 200) {
    ElMessage.success('上传成功，文件存储于服务器 /uploads 目录');
    try {
      await request.post(`/user/updateAvatar?userId=${currentUser.value.id}&avatarUrl=${encodeURIComponent(res.data)}`);
      currentUser.value.avatar = res.data;
      localStorage.setItem('userInfo', JSON.stringify(currentUser.value));
    } catch (e) {
      ElMessage.error('更新头像信息失败');
    }
  } else {
    ElMessage.error('上传失败');
  }
}

const pokePartner = async () => {
  if (!relationInfo.value) return
  try {
    await request.post(`/message/send?currentUserId=${currentUser.value.id}`, { content: '✨ 拍了拍TA' })
    await loadMessages()
    ElMessage.success('已拍了拍TA')
  } catch (err) {
    console.error(err)
  }
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
    loadPlans()

    if (relationInfo.value && !chatTimer.value) {
      loadMessages()
      chatTimer.value = setInterval(loadMessages, 5000)
    }
  } catch (err: any) {
    relationInfo.value = null
    if (chatTimer.value) {
      clearInterval(chatTimer.value)
      chatTimer.value = null
    }
  }
}

const copy = (text: string) => {
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(() => {
      ElMessage.success('邀请码已复制到剪贴板！发送给你的TA吧')
    }).catch(() => {
      ElMessage.error('复制失败，请手动选择复制')
    })
  } else {
    // 兼容 HTTP 或无安全上下文的环境
    const textArea = document.createElement("textarea")
    textArea.value = text
    textArea.style.position = "fixed"
    textArea.style.left = "-999999px"
    textArea.style.top = "-999999px"
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()
    try {
      const successful = document.execCommand('copy')
      if (successful) {
        ElMessage.success('邀请码已复制到剪贴板！发送给你的TA吧')
      } else {
        ElMessage.error('复制失败，请手动选择复制')
      }
    } catch (err) {
      ElMessage.error('执行复制命令失败，请手动复制')
    }
    document.body.removeChild(textArea)
  }
}

const handleBind = async () => {
  if (!bindForm.value.inviteCode || !bindForm.value.sharedGoal) {
    ElMessage.warning('请输入邀请码和共同目标')
    return
  }
  bindingLoading.value = true
  try {
    await request.post(`/couple/bind?currentUserId=${currentUser.value.id}`, bindForm.value)
    ElMessage.success('绑定成功！')
    fetchRelation()
  } catch (err) {
    console.error(err)
  } finally {
    bindingLoading.value = false
  }
}

const loadCheckInStatus = async () => {
  if (!relationInfo.value) return
  try {
    const pId = relationInfo.value.partner ? relationInfo.value.partner.id : ''
    const res: any = await request.get(`/checkin/status?currentUserId=${currentUser.value.id}&partnerId=${pId}`)
    checkInStatus.value = res.data
  } catch (err) {
    console.error(err)
  }
}



const loadPlans = async () => {
  try {
    const pId = relationInfo.value?.partner?.id || ''
    const res: any = await request.get(`/plan/list?currentUserId=${currentUser.value.id}&partnerId=${pId}`)
    myPlans.value = res.data.mine || []
    partnerPlans.value = res.data.partner || []
  } catch (err) {
    console.error(err)
  }
}

const handleAddPlan = async () => {
  if (!newPlanContent.value.trim()) return
  try {
    const dl = newPlanDeadline.value ? `${newPlanDeadline.value}T23:59:59` : null
    await request.post(`/plan/add?currentUserId=${currentUser.value.id}`, { content: newPlanContent.value, deadline: dl })
    newPlanContent.value = ''
    newPlanDeadline.value = ''
    loadPlans()
  } catch (err) {
    console.error(err)
  }
}

const handleTogglePlan = async (plan: any) => {
  try {
    await request.post(`/plan/toggle/${plan.id}?currentUserId=${currentUser.value.id}`)
    loadPlans()
  } catch (err) {
    console.error(err)
  }
}

const handleDeletePlan = async (id: number) => {
  try {
    await request.delete(`/plan/delete/${id}?currentUserId=${currentUser.value.id}`)
    loadPlans()
  } catch (err) {
    console.error(err)
  }
}

const loadMessages = async () => {
  if (!relationInfo.value) return
  try {
    const res: any = await request.get(`/message/list?currentUserId=${currentUser.value.id}&markAsRead=${isChatVisible.value}`)
    const oldLength = messages.value.length
    messages.value = res.data
    // 如果有新消息（长度增加）或初始加载为空的变为有内容的，或者刚进来时，自动滚动
    if (messages.value.length > oldLength || (messages.value.length > 0 && oldLength === 0)) {
      nextTick(() => {
        if (messageListRef.value) {
          messageListRef.value.scrollTop = messageListRef.value.scrollHeight
        }
      })
    }
  } catch (err) {
    console.error(err)
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim()) return
  try {
    await request.post(`/message/send?currentUserId=${currentUser.value.id}`, { content: newMessage.value })
    newMessage.value = ''
    await loadMessages()
  } catch (err) {
    console.error(err)
  }
}

const handleUnbind = () => {
  ElMessageBox.confirm(
    '确定要解除你们的搭子关系吗？',
    '残忍分手',
    {
      confirmButtonText: '确定解绑',
      cancelButtonText: '再想想',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await request.post(`/couple/unbind?currentUserId=${currentUser.value.id}`)
      ElMessage.success('解绑成功，祝各自安好')
      fetchRelation()
    } catch (error) {
    }
  }).catch(() => {})
}

const handleLogout = () => {
  if (chatTimer.value) {
    clearInterval(chatTimer.value)
  }
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

onMounted(() => {
  loadUserInfo()
})

onUnmounted(() => {
  if (chatTimer.value) {
    clearInterval(chatTimer.value)
  }
})
</script>

<style scoped>
.dashboard-container {
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

.logo {
  font-size: 22px;
  font-weight: bold;
  color: #409EFF;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
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

.user-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fbfdff;
}

:deep(.avatar-uploader .el-upload) {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-placeholder {
  font-size: 14px;
  color: #8c939d;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
  border-radius: 50%;
}

.user-card .info-item {
  margin-bottom: 15px;
  font-size: 14px;
}

.couple-card {
  min-height: 250px;
}

.relation-info {
  text-align: center;
  padding: 20px 0;
}

.relation-info h3 {
  margin-bottom: 10px;
  color: #303133;
}

.relation-info p {
  color: #909399;
  font-size: 14px;
}

.divider {
  width: 100%;
  height: 1px;
  background-color: #ebeef5;
  margin: 20px 0;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

.message-list {
  height: 350px;
  overflow-y: auto;
  padding: 10px;
  background: #f9fafc;
  border-radius: 8px;
  margin-bottom: 20px;
}

.empty-messages {
  text-align: center;
  color: #909399;
  margin-top: 130px;
  font-size: 14px;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
}

.my-message {
  justify-content: flex-end;
}

.partner-message {
  justify-content: flex-start;
}

.message-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 80%;
}

.my-message .message-wrapper {
  align-items: flex-end;
}

.partner-message .message-wrapper {
  align-items: flex-start;
}

.completed-plan {
  text-decoration: line-through;
  color: #c0c4cc;
}

.plan-list {
  max-height: 200px;
  overflow-y: auto;
}

.plan-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.message-sender {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
  padding: 0 4px;
}

.message-bubble {
  padding: 10px 15px;
  border-radius: 8px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  word-break: break-all;
  font-size: 14px;
  line-height: 1.4;
}

.my-message .message-bubble {
  background-color: #ecf5ff;
  border-color: #d9ecff;
  color: #409eff;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.read-status.read {
  color: #67c23a;
}

.read-status.unread {
  color: #f56c6c;
}
.custom-calendar :deep(.el-calendar-table .el-calendar-day) {
  height: 60px;
  padding: 8px;
}

.calendar-cell {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.dot-row {
  display: flex;
  gap: 2px;
  margin-top: 2px;
}

.checkin-dot {
  font-size: 8px;
  line-height: 1;
}
</style>
