<template>
  <div class="user-manage">
    <el-card shadow="never">
      <div class="filter-wrapper">
        <div class="filter-left">
          <el-input 
            v-model="query.keyword" 
            placeholder="搜索昵称 / ID / 手机号" 
            style="width: 240px; margin-right: 10px;"
            size="small"
            prefix-icon="el-icon-search"
            clearable
            @clear="handleSearch"
            @keyup.enter.native="handleSearch"
          ></el-input>
          
          <el-select v-model="query.status" placeholder="账号状态" size="small" style="width: 120px; margin-right: 10px;" clearable>
            <el-option label="正常" :value="1"></el-option>
            <el-option label="冻结" :value="0"></el-option>
          </el-select>

          <el-button type="primary" size="small" icon="el-icon-search" @click="handleSearch">查询</el-button>
          <el-button size="small" icon="el-icon-refresh-right" @click="resetFilter">重置</el-button>
        </div>
        
        <div class="filter-right">
          <el-button type="success" size="small" icon="el-icon-plus" @click="handleAdd">添加用户</el-button>
        </div>
      </div>

      <el-table 
        :data="tableData" 
        border 
        stripe 
        style="width: 100%; margin-top: 15px;"
        v-loading="loading"
      >
        <el-table-column prop="id" label="UID" width="80" align="center" sortable></el-table-column>
        
        <el-table-column label="用户" min-width="180">
          <template slot-scope="scope">
            <div style="display: flex; align-items: center; cursor: pointer;" @click="openDrawer(scope.row)">
              <el-avatar shape="square" size="medium" :src="scope.row.avatarUrl || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"></el-avatar>
              <div style="margin-left: 10px;">
                <div style="font-weight: bold; color: #409EFF;">{{ scope.row.username || scope.row.nickname }}</div>
                <div style="font-size: 12px; color: #909399;">{{ scope.row.phone || '未绑定' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="nickname" label="昵称" width="120" show-overflow-tooltip></el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === 1" type="success" size="small" effect="dark">正常</el-tag>
            <el-tag v-else type="danger" size="small" effect="dark">已冻结</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="注册时间" width="160" sortable align="center" show-overflow-tooltip></el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" icon="el-icon-document" @click="openDrawer(scope.row)">详情</el-button>
            <el-divider direction="vertical"></el-divider>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, scope.row)">
              <span class="el-dropdown-link" style="color: #409EFF; cursor: pointer; font-size: 12px;">
                管理 <i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="resetPwd">重置密码</el-dropdown-item>
                <el-dropdown-item command="freeze" style="color: #F56C6C;" v-if="scope.row.status !== 0">冻结账号</el-dropdown-item>
                <el-dropdown-item command="unfreeze" style="color: #67C23A;" v-else>解封账号</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: right;">
        <el-pagination 
          background 
          layout="total, prev, pager, next" 
          :total="total"
          :current-page="query.page"
          :page-size="query.pageSize"
          @current-change="handlePageChange"
        ></el-pagination>
      </div>
    </el-card>

    <el-drawer
      :title="(currentUser.username || currentUser.nickname) + ' 的用户详情'"
      :visible.sync="drawerVisible"
      direction="rtl"
      size="500px">
      
      <div class="drawer-content" v-if="currentUser.id">
        <el-skeleton :rows="5" animated v-if="loadingDetail" />
        
        <div v-else>
          <div class="user-header">
            <el-avatar :size="70" :src="currentUser.avatarUrl || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" shape="circle" style="border: 2px solid #fff; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);"></el-avatar>
            <div class="user-header-info">
              <h3 style="margin: 0;">{{ currentUser.username || currentUser.nickname }}</h3>
              <p style="color: #999; margin: 5px 0; font-size: 13px;">UID: {{ currentUser.id }}</p>
            </div>
          </div>

          <el-divider></el-divider>

          <div class="section-title"><i class="el-icon-postcard"></i> 详细档案</div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ currentUser.nickname || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="手机号码">{{ currentUser.phone || '未绑定' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ currentUser.createTime }}</el-descriptions-item>
            <el-descriptions-item label="账号状态">
              <el-tag v-if="currentUser.status === 1" type="success" size="small">正常</el-tag>
              <el-tag v-else type="danger" size="small">已冻结</el-tag>
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="drawer-footer">
            <el-button type="warning" plain icon="el-icon-message-solid">发送通知</el-button>
            <el-button type="danger" v-if="currentUser.status !== 0" icon="el-icon-lock" @click="handleCommand('freeze', currentUser)">冻结账号</el-button>
            <el-button type="success" v-else icon="el-icon-unlock" @click="handleCommand('unfreeze', currentUser)">解封账号</el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { userApi } from '@/api/index'

export default {
  name: 'UserManage',
  data() {
    return {
      query: {
        keyword: '',
        status: null,
        page: 1,
        pageSize: 10
      },
      loading: false,
      loadingDetail: false,
      drawerVisible: false,
      currentUser: {},
      tableData: [],
      total: 0
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      userApi.getUserList(this.query)
        .then(res => {
          this.tableData = res.data.list || []
          this.total = res.data.total || 0
        })
        .catch(error => {
          this.$message.error('加载用户列表失败：' + (error.message || '未知错误'))
        })
        .finally(() => {
          this.loading = false
        })
    },
    handleSearch() {
      this.query.page = 1
      this.loadData()
    },
    resetFilter() {
      this.query = {
        keyword: '',
        status: null,
        page: 1,
        pageSize: 10
      }
      this.loadData()
    },
    handlePageChange(page) {
      this.query.page = page
      this.loadData()
    },
    handleAdd() {
      this.$message.info('添加用户功能待实现')
    },
    openDrawer(row) {
      this.drawerVisible = true
      this.loadingDetail = true
      this.currentUser = {}
      
      userApi.getUserDetail(row.id)
        .then(res => {
          this.currentUser = res.data
        })
        .catch(error => {
          this.$message.error('加载用户详情失败：' + (error.message || '未知错误'))
          this.currentUser = row // 使用列表数据作为fallback
        })
        .finally(() => {
          this.loadingDetail = false
        })
    },
    handleCommand(command, row) {
      if (command === 'freeze' || command === 'unfreeze') {
        const isFreeze = command === 'freeze'
        const actionText = isFreeze ? '冻结' : '解封'
        const status = isFreeze ? 0 : 1
        
        this.$confirm(`确定要${actionText}用户 "${row.username || row.nickname}" 吗？`, '提示', {
          confirmButtonText: `确定${actionText}`,
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          userApi.updateUserStatus(row.id, status)
            .then(() => {
              this.$message.success(`用户已成功${actionText}`)
              this.loadData()
              if (this.currentUser.id === row.id) {
                this.currentUser.status = status
              }
            })
            .catch(error => {
              this.$message.error(`${actionText}失败：` + (error.message || '未知错误'))
            })
        }).catch(() => {})
      } else if (command === 'resetPwd') {
        this.$confirm(`确定重置用户 "${row.username || row.nickname}" 的密码为默认密码 (123456) 吗？`, '提示').then(() => {
          userApi.resetUserPassword(row.id)
            .then(() => {
              this.$message.success('密码重置成功，默认密码为：123456')
            })
            .catch(error => {
              this.$message.error('重置密码失败：' + (error.message || '未知错误'))
            })
        }).catch(() => {})
      }
    }
  }
}
</script>

<style scoped>
.filter-wrapper { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.filter-left { display: flex; align-items: center; }

/* 抽屉样式 */
.drawer-content { padding: 20px; height: 100%; overflow-y: auto; padding-bottom: 60px; }
.user-header { display: flex; align-items: center; margin-bottom: 20px; }
.user-header-info { margin-left: 15px; }

.section-title { font-weight: bold; margin-bottom: 15px; color: #303133; border-left: 4px solid #409EFF; padding-left: 10px; }

.drawer-footer { position: absolute; bottom: 0; left: 0; width: 100%; padding: 15px 20px; background: #fff; border-top: 1px solid #e8e8e8; text-align: right; box-sizing: border-box; }
</style>
