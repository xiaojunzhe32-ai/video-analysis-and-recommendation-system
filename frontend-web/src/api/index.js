import request from '@/utils/request'

// 认证相关API
export const authApi = {
  // 登录
  login(data) {
    return request({
      url: '/api/auth/login',
      method: 'post',
      data
    })
  }
}

// 用户管理API
export const userApi = {
  // 获取用户列表
  getUserList(params) {
    return request({
      url: '/api/admin/user/list',
      method: 'get',
      params
    })
  },
  // 获取用户详情
  getUserDetail(id) {
    return request({
      url: `/api/admin/user/${id}`,
      method: 'get'
    })
  },
  // 更新用户状态
  updateUserStatus(id, status) {
    return request({
      url: `/api/admin/user/${id}/status`,
      method: 'put',
      params: { status }
    })
  },
  // 重置用户密码
  resetUserPassword(id) {
    return request({
      url: `/api/admin/user/${id}/password`,
      method: 'put'
    })
  }
}

// 管理员管理API
export const adminApi = {
  // 获取管理员列表
  getAdminList(params) {
    return request({
      url: '/api/admin/admin/list',
      method: 'get',
      params
    })
  },
  // 获取管理员详情
  getAdminDetail(id) {
    return request({
      url: `/api/admin/admin/${id}`,
      method: 'get'
    })
  },
  // 创建管理员
  createAdmin(data) {
    return request({
      url: '/api/admin/admin',
      method: 'post',
      data
    })
  },
  // 更新管理员
  updateAdmin(id, data) {
    return request({
      url: `/api/admin/admin/${id}`,
      method: 'put',
      data
    })
  },
  // 删除管理员
  deleteAdmin(id) {
    return request({
      url: `/api/admin/admin/${id}`,
      method: 'delete'
    })
  },
  // 更新管理员状态
  updateAdminStatus(id, status) {
    return request({
      url: `/api/admin/admin/${id}/status`,
      method: 'put',
      params: { status }
    })
  },
  // 重置管理员密码
  resetAdminPassword(id) {
    return request({
      url: `/api/admin/admin/${id}/password`,
      method: 'put'
    })
  }
}

// 视频管理API
export const videoApi = {
  // 获取视频列表
  getVideoList(params) {
    return request({
      url: '/api/admin/video/list',
      method: 'get',
      params
    })
  },
  // 审核视频
  auditVideo(data) {
    return request({
      url: '/api/admin/video/audit',
      method: 'post',
      data
    })
  },
  // 删除视频
  deleteVideo(id) {
    return request({
      url: `/api/admin/video/${id}`,
      method: 'delete'
    })
  },
  // 设置热门
  setHot(id, isHot) {
    return request({
      url: `/api/admin/video/${id}/hot`,
      method: 'put',
      params: { isHot }
    })
  }
}
