<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <div class="brand">👫 DuoStudy</div>
          <p class="brand-sub">和搭子一起，学习不孤单</p>
        </div>
      </template>

      <el-form :model="form" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" prefix-icon="User" clearable></el-input>
        </el-form-item>

        <el-form-item v-if="isRegister" label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称（如：学习小飞侠）" prefix-icon="Avatar" clearable></el-input>
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password prefix-icon="Lock"></el-input>
        </el-form-item>

        <el-form-item v-if="isRegister" label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" show-password prefix-icon="Lock"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="handleSubmit" :loading="loading">
            {{ isRegister ? '注 册' : '登 录' }}
          </el-button>
        </el-form-item>

        <div class="toggle-link">
          <el-link type="primary" @click="isRegister = !isRegister">
            {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
          </el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const isRegister = ref(false)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const handleSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('账号密码不能为空')
    return
  }

  if (isRegister.value) {
    if (!form.nickname) {
      ElMessage.warning('请输入昵称')
      return
    }
    if (form.password !== form.confirmPassword) {
      ElMessage.warning('两次输入的密码不一致')
      return
    }
  }

  loading.value = true
  try {
    if (isRegister.value) {
      await request.post('/user/register', {
        username: form.username,
        password: form.password,
        nickname: form.nickname
      })
      ElMessage.success('注册成功，请登录')
      isRegister.value = false
      form.password = ''
      form.confirmPassword = ''
    } else {
      const res: any = await request.post('/user/login', {
        username: form.username,
        password: form.password
      })
      
      const { token, user } = res.data
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(user))
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } catch (err: any) {
    console.error(err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #e0eafc 0%, #cfdef3 100%);
}

.login-card {
  width: 420px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.brand {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
}

.brand-sub {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin: 8px 0 0;
}

.card-header h2 {
  margin: 0;
  text-align: center;
  color: #333;
}

.toggle-link {
  text-align: center;
  margin-top: 10px;
}
</style>

