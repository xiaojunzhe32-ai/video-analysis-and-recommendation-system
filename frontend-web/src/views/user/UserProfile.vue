<template>
  <div class="profile-container">
    <el-card>
      <div slot="header">
        <span>个人中心</span>
        <el-button style="float: right;" type="text" icon="el-icon-edit">
          编辑资料
        </el-button>
      </div>
      
      <div class="profile-content">
        <div class="profile-header">
          <div class="avatar-section">
            <el-avatar 
              :size="100" 
              :src="userInfo.avatar"
              class="user-avatar"
            >
              {{ userInfo.username.charAt(0) }}
            </el-avatar>
            <h2>{{ userInfo.username }}</h2>
            <p>{{ userInfo.bio || '暂无简介' }}</p>
          </div>
          
          <div class="stats-section">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-number">128</div>
                  <div class="stat-label">关注</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-number">256</div>
                  <div class="stat-label">粉丝</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-number">48</div>
                  <div class="stat-label">作品</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
        
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="我的作品" name="videos">
            <div class="my-videos">
              <p>我的视频列表...</p>
              <el-empty description="暂无作品" v-if="false"></el-empty>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="收藏" name="favorites">
            <div class="my-favorites">
              <p>收藏的视频...</p>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="设置" name="settings">
            <div class="settings">
              <el-form label-width="100px">
                <el-form-item label="消息通知">
                  <el-switch v-model="settings.notifications"></el-switch>
                </el-form-item>
                <el-form-item label="隐私模式">
                  <el-switch v-model="settings.privacy"></el-switch>
                </el-form-item>
                <el-form-item label="主题颜色">
                  <el-color-picker v-model="settings.themeColor"></el-color-picker>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'UserProfile',
  data() {
    return {
      activeTab: 'videos',
      userInfo: {
        username: localStorage.getItem('username') || '用户',
        avatar: '',
        bio: '热爱生活，喜欢分享'
      },
      settings: {
        notifications: true,
        privacy: false,
        themeColor: '#409EFF'
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.avatar-section {
  text-align: center;
}

.user-avatar {
  margin-bottom: 10px;
}

.stats-section {
  flex: 1;
  max-width: 400px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.profile-tabs {
  margin-top: 20px;
}

.my-videos, .my-favorites, .settings {
  padding: 20px;
  min-height: 200px;
}
</style>