<template>
  <div class="admin-manage">
    <el-card shadow="never">
      <div class="filter-wrapper">
        <div class="filter-left">
          <el-input 
            v-model="query.keyword" 
            placeholder="搜索用户名 / ID / 手机号" 
            style="width: 240px; margin-right: 10px;"
            size="small"
            prefix-icon="el-icon-search"
            clearable
            @clear="handleSearch"
            @keyup.enter.native="handleSearch"
          ></el-input>
          
          <el-select v-model="query.status" placeholder="账号状态" size="small" style="width: 120px; margin-right: 10px;" clearable>
            <el-option label="正常" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>

          <el-button type="primary" size="small" icon="el-icon-search" @click="handleSearch">查询</el-button>
          <el-button size="small" icon="el-icon-refresh-right" @click="resetFilter">重置</el-button>
        </div>
        
        <div class="filter-right">
          <el-button type="success" size="small" icon="el-icon-plus" @click="handleAdd">添加管理员</el-button>
        </div>
      </div>

      <el-table 
        :data="tableData" 
        border 
        stripe 
        style="width: 100%; margin-top: 15px;"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" sortable></el-table-column>
        
        <el-table-column label="管理员" min-width="180">
          <template slot-scope="scope">
            <div style="display: flex; align-items: center;">
              <el-avatar shape="square" size="medium" icon="el-icon-user-solid"></el-avatar>
              <div style="margin-left: 10px;">
                <div style="font-weight: bold; color: #409EFF;">{{ scope.row.username }}</div>
                <div style="font-size: 12px; color: #909399;">{{ scope.row.realName || '未设置' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="realName" label="真实姓名" width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" show-overflow-tooltip></el-table-column>
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip></el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === 1" type="success" size="small" effect="dark">正常</el-tag>
            <el-tag v-else type="danger" size="small" effect="dark">已禁用</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160" sortable align="center" show-overflow-tooltip></el-table-column>
        
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-divider direction="vertical"></el-divider>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, scope.row)">
              <span class="el-dropdown-link" style="color: #409EFF; cursor: pointer; font-size: 12px;">
                更多 <i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="resetPwd">重置密码</el-dropdown-item>
                <el-dropdown-item command="disable" style="color: #F56C6C;" v-if="scope.row.status !== 0">禁用账号</el-dropdown-item>
                <el-dropdown-item command="enable" style="color: #67C23A;" v-else>启用账号</el-dropdown-item>
                <el-dropdown-item command="delete" style="color: #F56C6C;" divided>删除</el-dropdown-item>
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（留空则不修改）" show-password></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="角色ID" prop="roleId">
          <el-input-number v-model="form.roleId" :min="1" placeholder="请输入角色ID"></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { adminApi } from '@/api/index'

export default {
  name: 'AdminManage',
  data() {
    return {
      query: {
        keyword: '',
        status: null,
        page: 1,
        pageSize: 10
      },
      loading: false,
      dialogVisible: false,
      isEdit: false,
      currentId: null,
      tableData: [],
      total: 0,
      form: {
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        roleId: null
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度至少6位', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
        email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑管理员' : '添加管理员'
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      adminApi.getAdminList(this.query)
        .then(res => {
          this.tableData = res.data.list || []
          this.total = res.data.total || 0
        })
        .catch(error => {
          this.$message.error('加载管理员列表失败：' + (error.message || '未知错误'))
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
      this.isEdit = false
      this.currentId = null
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEdit = true
      this.currentId = row.id
      adminApi.getAdminDetail(row.id)
        .then(res => {
          const data = res.data
          this.form = {
            username: data.username || '',
            password: '',
            realName: data.realName || '',
            phone: data.phone || '',
            email: data.email || '',
            roleId: data.roleId || null
          }
          this.dialogVisible = true
        })
        .catch(error => {
          this.$message.error('加载管理员详情失败：' + (error.message || '未知错误'))
        })
    },
    submitForm() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          const formData = { ...this.form }
          // 如果是编辑且密码为空，则不传密码
          if (this.isEdit && !formData.password) {
            delete formData.password
          }
          
          const apiCall = this.isEdit 
            ? adminApi.updateAdmin(this.currentId, formData)
            : adminApi.createAdmin(formData)
          
          apiCall
            .then(() => {
              this.$message.success(this.isEdit ? '更新成功' : '创建成功')
              this.dialogVisible = false
              this.loadData()
            })
            .catch(error => {
              this.$message.error((this.isEdit ? '更新' : '创建') + '失败：' + (error.message || '未知错误'))
            })
        }
      })
    },
    resetForm() {
      this.$refs.form && this.$refs.form.resetFields()
      this.form = {
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        roleId: null
      }
    },
    handleCommand(command, row) {
      if (command === 'disable' || command === 'enable') {
        const isDisable = command === 'disable'
        const actionText = isDisable ? '禁用' : '启用'
        const status = isDisable ? 0 : 1
        
        this.$confirm(`确定要${actionText}管理员 "${row.username}" 吗？`, '提示', {
          confirmButtonText: `确定${actionText}`,
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          adminApi.updateAdminStatus(row.id, status)
            .then(() => {
              this.$message.success(`管理员已成功${actionText}`)
              this.loadData()
            })
            .catch(error => {
              this.$message.error(`${actionText}失败：` + (error.message || '未知错误'))
            })
        }).catch(() => {})
      } else if (command === 'resetPwd') {
        this.$confirm(`确定重置管理员 "${row.username}" 的密码为默认密码 (123456) 吗？`, '提示').then(() => {
          adminApi.resetAdminPassword(row.id)
            .then(() => {
              this.$message.success('密码重置成功，默认密码为：123456')
            })
            .catch(error => {
              this.$message.error('重置密码失败：' + (error.message || '未知错误'))
            })
        }).catch(() => {})
      } else if (command === 'delete') {
        this.$confirm(`确定删除管理员 "${row.username}" 吗？此操作不可恢复！`, '危险操作', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'error'
        }).then(() => {
          adminApi.deleteAdmin(row.id)
            .then(() => {
              this.$message.success('删除成功')
              this.loadData()
            })
            .catch(error => {
              this.$message.error('删除失败：' + (error.message || '未知错误'))
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
</style>
