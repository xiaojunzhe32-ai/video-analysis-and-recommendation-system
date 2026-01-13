<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img src="https://img.icons8.com/color/96/vue-js.png" alt="logo" class="logo-img">
        <div class="title">短视频系统入口</div>
        <div class="subtitle">Short Video System Login</div>
      </div>
      
      <el-form :model="loginForm" :rules="rules" ref="loginForm" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" prefix-icon="el-icon-user" placeholder="请输入账号"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            prefix-icon="el-icon-lock" 
            type="password" 
            placeholder="请输入密码" 
            show-password 
            @keyup.enter.native="handleLogin"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">立即登录</el-button>
        </el-form-item>
      </el-form>
      
      <div class="footer">Copyright © 2026 Integrated System</div>
    </div>
  </div>
</template>

<script>
import { authApi } from '@/api/index'

export default {
  name: 'UserLogin',
  data() {
    return {
      loading: false,
      loginForm: {
        username: 'admin', // 默认填好方便测试
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true;
          
          // 调用后端登录接口
          authApi.login(this.loginForm)
            .then(res => {
              const { token, userType, username } = res.data;
              
              // 根据用户类型存储token
              if (userType === 'admin') {
                localStorage.setItem('adminToken', token);
                localStorage.setItem('username', username);
                this.$message.success('管理员登录成功，正在进入后台...');
                this.$router.push('/admin');
              } else {
                localStorage.setItem('userToken', token);
                localStorage.setItem('username', username);
                this.$message.success('用户登录成功，欢迎回来！');
                this.$router.push('/main');
              }
            })
            .catch(error => {
              this.$message.error(error.message || '登录失败，请检查账号密码');
            })
            .finally(() => {
              this.loading = false;
            });
        }
      });
    }
  }
}
</script>

<style scoped>
/* 保持之前的高颜值磨砂风格 */
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  /* 漂亮的蓝紫渐变背景 */
  background-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: cover;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95); /* 几乎不透明的白 */
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2); /* 强烈的浮起感 */
  text-align: center;
}

.logo-img { width: 50px; margin-bottom: 10px; }
.title { font-size: 24px; font-weight: bold; color: #333; margin-bottom: 5px; }
.subtitle { font-size: 12px; color: #999; margin-bottom: 30px; letter-spacing: 1px; }

.login-btn {
  width: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%); /* 按钮也用渐变 */
  border: none;
  font-size: 16px;
  padding: 12px 0;
  border-radius: 4px;
  transition: opacity 0.3s;
}
.login-btn:hover { opacity: 0.9; }

.footer { margin-top: 20px; font-size: 12px; color: #bbb; }
</style>