<template>
  <div class="video-player-container">
    <!-- 视频播放器主区域 -->
    <div class="video-player-main">
      <!-- 当前播放的视频 -->
      <div class="current-video-section">
        <el-card class="video-player-card">
          <div class="video-wrapper">
            <video 
              ref="videoPlayer"
              :src="currentVideo.url"
              class="video-element"
              controls
              autoplay
              @timeupdate="handleTimeUpdate"
              @ended="handleVideoEnded"
              @play="handleVideoPlay"
              @pause="handleVideoPause"
            ></video>
          </div>
          
          <!-- 视频信息 -->
          <div class="video-info-section">
            <div class="video-title-row">
              <h2 class="video-title">{{ currentVideo.title }}</h2>
              <div class="video-actions">
                <el-button 
                  type="danger" 
                  :icon="currentVideo.isLiked ? 'el-icon-star-on' : 'el-icon-star-off'"
                  @click="handleLike"
                  :loading="likeLoading"
                >
                  {{ currentVideo.likes }}
                </el-button>
                <el-button 
                  type="primary" 
                  icon="el-icon-chat-dot-round"
                  @click="showCommentModal = true"
                >
                  评论
                </el-button>
                <el-button 
                  type="success" 
                  icon="el-icon-share"
                  @click="handleShare"
                >
                  分享
                </el-button>
              </div>
            </div>
            
            <!-- 作者信息 -->
            <div class="author-info">
              <el-avatar 
                :size="40" 
                :src="currentVideo.author.avatar"
                class="author-avatar"
              ></el-avatar>
              <div class="author-details">
                <div class="author-name">{{ currentVideo.author.name }}</div>
                <div class="upload-time">{{ currentVideo.uploadTime }} · {{ currentVideo.views }}次播放</div>
              </div>
              <el-button 
                v-if="!currentVideo.isFollowing"
                type="primary" 
                size="small"
                @click="handleFollow"
              >
                关注
              </el-button>
            </div>
            
            <!-- 视频描述 -->
            <div class="video-description">
              {{ currentVideo.description }}
            </div>
          </div>
        </el-card>
        
        <!-- 评论区（简化版） -->
        <el-card class="comments-section" v-if="showComments">
          <div slot="header">
            <span>评论</span>
            <span class="comment-count">({{ currentVideo.comments.length }})</span>
          </div>
          <div class="comment-input-row">
            <el-input
              v-model="newComment"
              placeholder="发表你的评论..."
              @keyup.enter.native="submitComment"
            >
              <el-button slot="append" icon="el-icon-position" @click="submitComment"></el-button>
            </el-input>
          </div>
          <div class="comment-list">
            <div v-for="comment in currentVideo.comments.slice(0, 3)" :key="comment.id" class="comment-item">
              <el-avatar :size="32" :src="comment.userAvatar"></el-avatar>
              <div class="comment-content">
                <div class="comment-user">{{ comment.userName }}</div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-time">{{ comment.time }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 推荐视频列表 -->
      <div class="recommendation-section">
        <div class="section-header">
          <h3>推荐视频</h3>
          <el-button type="text" @click="refreshRecommendations">换一批</el-button>
        </div>
        
        <div class="video-list">
          <div 
            v-for="video in recommendedVideos" 
            :key="video.id"
            class="video-item"
            @click="switchVideo(video)"
          >
            <div class="video-thumbnail">
              <img :src="video.thumbnail" alt="thumbnail">
              <div class="video-duration">{{ video.duration }}</div>
            </div>
            <div class="video-item-info">
              <div class="video-item-title">{{ video.title }}</div>
              <div class="video-item-meta">
                <span>{{ video.author.name }}</span>
                <span>·</span>
                <span>{{ video.views }}次播放</span>
                <span>·</span>
                <span>{{ video.uploadTime }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 热门排行榜 -->
    <el-card class="hot-ranking-card" v-if="showRanking">
      <div slot="header">
        <span><i class="el-icon-trophy"></i> 热门排行榜</span>
      </div>
      <div class="ranking-list">
        <div 
          v-for="(video, index) in hotRanking" 
          :key="video.id"
          class="ranking-item"
          :class="{ 'top-three': index < 3 }"
          @click="switchVideo(video)"
        >
          <div class="ranking-order">{{ index + 1 }}</div>
          <div class="ranking-info">
            <div class="ranking-title">{{ video.title }}</div>
            <div class="ranking-stats">
              <span><i class="el-icon-view"></i> {{ video.views }}</span>
              <span><i class="el-icon-star-on"></i> {{ video.likes }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 不喜欢/反馈按钮 -->
    <div class="feedback-section" v-if="showFeedback">
      <el-button 
        type="text" 
        icon="el-icon-close" 
        @click="handleDislike"
        class="dislike-btn"
      >
        不感兴趣
      </el-button>
      <el-button 
        type="text" 
        icon="el-icon-edit" 
        @click="showTagModal = true"
      >
        调整兴趣标签
      </el-button>
    </div>
    
    <!-- 兴趣标签弹窗 -->
    <el-dialog
      title="调整兴趣标签"
      :visible.sync="showTagModal"
      width="500px"
    >
      <div class="interest-tags">
        <el-tag
          v-for="tag in interestTags"
          :key="tag"
          :type="selectedTags.includes(tag) ? 'primary' : 'info'"
          @click="toggleTag(tag)"
          class="tag-selectable"
        >
          {{ tag }}
        </el-tag>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showTagModal = false">取消</el-button>
        <el-button type="primary" @click="updateInterests">确认更新</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'VideoPlayer',
  data() {
    return {
      // 当前播放视频
      currentVideo: {
        id: 1,
        title: '超美风景航拍：云海日出',
        url: 'https://example.com/video1.mp4',
        thumbnail: 'https://picsum.photos/300/200?random=1',
        duration: '3:45',
        description: '在海拔3000米的山顶拍摄的云海日出，大自然的美景让人心旷神怡。',
        views: '125.8万',
        likes: 24500,
        isLiked: false,
        uploadTime: '3天前',
        author: {
          id: 1,
          name: '旅行摄影师',
          avatar: 'https://picsum.photos/40/40?random=2'
        },
        isFollowing: false,
        comments: [
          { id: 1, userName: '用户A', userAvatar: 'https://picsum.photos/32/32?random=3', content: '太美了！', time: '2小时前' },
          { id: 2, userName: '用户B', userAvatar: 'https://picsum.photos/32/32?random=4', content: '想去这里旅行！', time: '5小时前' }
        ]
      },
      
      // 推荐视频列表
      recommendedVideos: [
        { id: 2, title: '美食探店：网红火锅店', thumbnail: 'https://picsum.photos/300/200?random=5', duration: '4:20', views: '89.2万', uploadTime: '1天前', author: { name: '美食家' } },
        { id: 3, title: '健身教程：腹肌训练', thumbnail: 'https://picsum.photos/300/200?random=6', duration: '5:30', views: '102.5万', uploadTime: '2天前', author: { name: '健身教练' } },
        { id: 4, title: '搞笑宠物合集', thumbnail: 'https://picsum.photos/300/200?random=7', duration: '2:15', views: '156.3万', uploadTime: '5小时前', author: { name: '萌宠日记' } },
        { id: 5, title: '科技产品评测', thumbnail: 'https://picsum.photos/300/200?random=8', duration: '6:45', views: '78.9万', uploadTime: '3天前', author: { name: '科技达人' } },
        { id: 6, title: '音乐现场：流行歌曲', thumbnail: 'https://picsum.photos/300/200?random=9', duration: '3:50', views: '210.4万', uploadTime: '1周前', author: { name: '音乐现场' } }
      ],
      
      // 热门排行榜
      hotRanking: [
        { id: 7, title: '舞蹈挑战赛冠军', views: '350.2万', likes: 45000 },
        { id: 8, title: '搞笑情景剧', views: '289.7万', likes: 38000 },
        { id: 9, title: '旅行vlog', views: '265.4万', likes: 32000 },
        { id: 10, title: '美食制作教程', views: '240.1万', likes: 29000 },
        { id: 11, title: '科技新闻速递', views: '198.5万', likes: 25000 }
      ],
      
      // 兴趣标签
      interestTags: ['搞笑', '美食', '旅行', '科技', '音乐', '体育', '舞蹈', '萌宠', '教育', '时尚', '游戏', '影视'],
      selectedTags: ['搞笑', '美食', '旅行'],
      
      // UI状态
      showComments: true,
      showRanking: true,
      showFeedback: true,
      showTagModal: false,
      likeLoading: false,
      newComment: '',
      
      // 用户行为数据（临时存储，实际应发送到后端）
      userBehavior: {
        videoId: null,
        playDuration: 0,
        isCompleted: false,
        likeCount: 0,
        commentCount: 0,
        shareCount: 0
      },
      
      // 播放计时器
      playTimer: null,
      startTime: null
    }
  },
  
  mounted() {
    this.initializeVideoPlayer()
    this.loadRecommendations()
    this.loadHotRanking()
    this.startBehaviorTracking()
  },
  
  beforeDestroy() {
    this.stopBehaviorTracking()
  },
  
  methods: {
    // 初始化视频播放器
    initializeVideoPlayer() {
      const videoElement = this.$refs.videoPlayer
      if (videoElement) {
        videoElement.addEventListener('loadeddata', () => {
          console.log('视频加载完成')
        })
      }
    },
    
    // 加载推荐视频（模拟从后端获取）
    loadRecommendations() {
      // 这里应调用后端API获取个性化推荐
      console.log('加载个性化推荐视频')
      // 模拟API调用
      this.$nextTick(() => {
        // 已使用模拟数据
      })
    },
    
    // 加载热门排行榜
    loadHotRanking() {
      // 调用后端API获取热门视频
      console.log('加载热门排行榜')
    },
    
    // 切换视频
    switchVideo(video) {
      this.stopBehaviorTracking()
      
      // 保存当前视频行为数据
      this.sendUserBehavior()
      
      // 切换到新视频
      this.currentVideo = {
        ...video,
        isLiked: false,
        isFollowing: false,
        comments: [] // 清空评论
      }
      
      // 开始跟踪新视频行为
      this.startBehaviorTracking()
      
      // 滚动到顶部
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    
    // 点赞处理
    handleLike() {
      this.likeLoading = true
      
      // 模拟API调用延迟
      setTimeout(() => {
        this.currentVideo.isLiked = !this.currentVideo.isLiked
        this.currentVideo.likes += this.currentVideo.isLiked ? 1 : -1
        
        // 记录点赞行为
        this.recordInteraction('like')
        
        this.likeLoading = false
      }, 300)
    },
    
    // 关注作者
    handleFollow() {
      this.currentVideo.isFollowing = true
      this.$message.success('关注成功')
      
      // 记录关注行为
      this.recordInteraction('follow')
    },
    
    // 分享视频
    handleShare() {
      this.$message.success('分享功能已调用')
      
      // 记录分享行为
      this.recordInteraction('share')
    },
    
    // 提交评论
    submitComment() {
      if (!this.newComment.trim()) return
      
      const newComment = {
        id: Date.now(),
        userName: localStorage.getItem('username') || '用户',
        userAvatar: 'https://picsum.photos/32/32?random=' + Date.now(),
        content: this.newComment,
        time: '刚刚'
      }
      
      this.currentVideo.comments.unshift(newComment)
      this.newComment = ''
      
      // 记录评论行为
      this.recordInteraction('comment')
    },
    
    // 处理不喜欢
    handleDislike() {
      this.$confirm('将减少此类视频的推荐，确定吗？', '不感兴趣', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 发送不喜欢反馈到后端
        this.sendDislikeFeedback()
        
        // 刷新推荐列表
        this.refreshRecommendations()
        
        this.$message.success('已记录您的偏好')
      })
    },
    
    // 刷新推荐
    refreshRecommendations() {
      // 向后端请求新的推荐
      console.log('刷新推荐列表')
      this.$message.info('正在为您推荐新内容...')
    },
    
    // 切换兴趣标签
    toggleTag(tag) {
      const index = this.selectedTags.indexOf(tag)
      if (index > -1) {
        this.selectedTags.splice(index, 1)
      } else {
        this.selectedTags.push(tag)
      }
    },
    
    // 更新兴趣标签
    updateInterests() {
      // 发送到后端更新用户画像
      console.log('更新兴趣标签:', this.selectedTags)
      this.showTagModal = false
      
      // 根据新标签刷新推荐
      this.refreshRecommendations()
    },
    
    // 视频播放事件处理
    handleTimeUpdate(event) {
      const video = event.target
      const currentTime = video.currentTime
      const duration = video.duration
      
      // 更新播放时长
      this.userBehavior.playDuration = currentTime
      
      // 检查是否完播
      if (currentTime >= duration * 0.9) { // 90%视为完播
        this.userBehavior.isCompleted = true
      }
    },
    
    handleVideoEnded() {
      console.log('视频播放结束')
      this.userBehavior.isCompleted = true
      this.sendUserBehavior()
    },
    
    handleVideoPlay() {
      console.log('视频开始播放')
      this.startTime = new Date()
    },
    
    handleVideoPause() {
      console.log('视频暂停')
    },
    
    // 开始用户行为跟踪
    startBehaviorTracking() {
      this.userBehavior = {
        videoId: this.currentVideo.id,
        playDuration: 0,
        isCompleted: false,
        likeCount: 0,
        commentCount: 0,
        shareCount: 0
      }
    },
    
    // 停止行为跟踪并发送数据
    stopBehaviorTracking() {
      this.sendUserBehavior()
    },
    
    // 发送用户行为数据到后端
    sendUserBehavior() {
      if (!this.userBehavior.videoId) return
      
      const behaviorData = {
        userId: localStorage.getItem('userId') || 'anonymous',
        videoId: this.userBehavior.videoId,
        behaviorType: 'watch',
        duration: this.userBehavior.playDuration,
        isCompleted: this.userBehavior.isCompleted,
        timestamp: new Date().toISOString()
      }
      
      // 实际应调用后端API
      console.log('发送用户行为数据:', behaviorData)
      // this.$api.userBehavior.collect(behaviorData)
    },
    
    // 记录交互行为
    recordInteraction(type) {
      const interactionData = {
        userId: localStorage.getItem('userId') || 'anonymous',
        videoId: this.currentVideo.id,
        behaviorType: type,
        timestamp: new Date().toISOString()
      }
      
      // 更新本地计数
      if (type === 'like') this.userBehavior.likeCount++
      if (type === 'comment') this.userBehavior.commentCount++
      if (type === 'share') this.userBehavior.shareCount++
      
      // 发送到后端
      console.log('发送交互行为:', interactionData)
      // this.$api.userBehavior.collect(interactionData)
    },
    
    // 发送不喜欢反馈
    sendDislikeFeedback() {
      const feedbackData = {
        userId: localStorage.getItem('userId') || 'anonymous',
        videoId: this.currentVideo.id,
        feedbackType: 'dislike',
        timestamp: new Date().toISOString()
      }
      
      console.log('发送不喜欢反馈:', feedbackData)
      // this.$api.feedback.submit(feedbackData)
    }
  }
}
</script>

<style scoped>
.video-player-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.video-player-main {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.video-player-card {
  margin-bottom: 20px;
}

.video-wrapper {
  position: relative;
  width: 100%;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.video-element {
  width: 100%;
  max-height: 600px;
  display: block;
}

.video-info-section {
  padding: 20px 0;
}

.video-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.video-title {
  margin: 0;
  font-size: 20px;
  flex: 1;
}

.video-actions {
  display: flex;
  gap: 10px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.author-details {
  flex: 1;
}

.author-name {
  font-weight: bold;
  font-size: 16px;
}

.upload-time {
  color: #666;
  font-size: 14px;
}

.video-description {
  line-height: 1.6;
  color: #333;
}

.comments-section {
  margin-top: 20px;
}

.comment-count {
  color: #999;
  font-size: 14px;
  margin-left: 5px;
}

.comment-input-row {
  margin-bottom: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f5f5f5;
}

.comment-content {
  flex: 1;
}

.comment-user {
  font-weight: bold;
  margin-bottom: 5px;
}

.comment-text {
  margin-bottom: 5px;
  line-height: 1.4;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

/* 推荐视频列表 */
.recommendation-section {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
}

.video-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.video-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.video-item:hover {
  background-color: #f5f5f5;
}

.video-thumbnail {
  position: relative;
  flex-shrink: 0;
}

.video-thumbnail img {
  width: 120px;
  height: 68px;
  border-radius: 4px;
  object-fit: cover;
}

.video-duration {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 2px;
}

.video-item-info {
  flex: 1;
}

.video-item-title {
  font-weight: 500;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 14px;
}

.video-item-meta {
  color: #666;
  font-size: 12px;
  display: flex;
  gap: 4px;
}

/* 热门排行榜 */
.hot-ranking-card {
  grid-column: 1 / -1;
  margin-top: 20px;
}

.ranking-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 10px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.ranking-item:hover {
  background-color: #f5f5f5;
}

.ranking-item.top-three .ranking-order {
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
  color: white;
  font-weight: bold;
}

.ranking-order {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  border-radius: 50%;
  font-size: 14px;
}

.ranking-info {
  flex: 1;
}

.ranking-title {
  font-weight: 500;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.ranking-stats {
  display: flex;
  gap: 12px;
  color: #666;
  font-size: 12px;
}

/* 反馈区域 */
.feedback-section {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.dislike-btn {
  color: #f56c6c;
}

/* 兴趣标签 */
.interest-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-selectable {
  cursor: pointer;
  user-select: none;
}

@media (max-width: 1200px) {
  .video-player-main {
    grid-template-columns: 1fr;
  }
  
  .recommendation-section {
    position: static;
  }
}

@media (max-width: 768px) {
  .video-player-container {
    padding: 10px;
  }
  
  .video-title-row {
    flex-direction: column;
    gap: 10px;
  }
  
  .video-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>