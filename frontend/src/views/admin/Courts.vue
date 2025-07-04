<template>
  <div class="courts-management">
    <div class="header">
      <h2>场地管理</h2>
      <div class="date-selector">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          @change="loadCourtData"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </div>
      <div class="actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          添加场地
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
<<<<<<< HEAD
        <el-button @click="testAuth" type="info">
          <el-icon><Key /></el-icon>
          测试权限
        </el-button>
=======
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalCourts }}</div>
          <div class="stat-label">总场地数</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ statistics.availableCourts }}</div>
          <div class="stat-label">可用场地</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalSlots }}</div>
          <div class="stat-label">今日时段</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ Math.round(statistics.occupancyRate) }}%</div>
          <div class="stat-label">占用率</div>
        </div>
      </el-card>
    </div>

    <!-- 场地时间段管理表格 -->
    <el-card class="time-slots-card">
      <div class="table-header">
        <h3>场地时间段管理 - {{ selectedDate }}</h3>
        <div class="legend">
          <span class="legend-item">
            <span class="legend-color available"></span>
            空闲
          </span>
          <span class="legend-item">
            <span class="legend-color occupied"></span>
            已预约
          </span>
          <span class="legend-item">
            <span class="legend-color closed"></span>
            关闭
          </span>
        </div>
      </div>
      
      <div class="time-table-container" v-loading="loading">
        <table class="time-table">
          <thead>
            <tr>
              <th class="time-header">时间 \ 场地</th>
              <th v-for="court in courtsData" :key="court.id" class="court-header">
                <div class="court-info">
                  <div class="name">{{ court.name }}</div>
                  <div class="price">¥{{ court.pricePerHour }}/时</div>
                  <el-switch
                    v-model="court.available"
                    size="small"
                    @change="toggleCourtAvailability(court)"
                    active-text="启用"
                    inactive-text="停用"
                  />
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="hour in timeSlots" :key="hour" class="time-row">
              <td class="time-name">
                <div class="time-info">
                  {{ hour }}:00-{{ hour + 1 }}:00
                </div>
              </td>
              <td 
                v-for="court in courtsData" 
                :key="court.id"
                :class="getSlotClass(getSlotForCourtAndTime(court, hour))"
                @click="toggleSlotStatus(getSlotForCourtAndTime(court, hour))"
                class="time-slot"
              >
                <div class="slot-content">
                  <span class="slot-status">
                    {{ getSlotStatusText(getSlotForCourtAndTime(court, hour)) }}
                  </span>
                  <span v-if="getSlotForCourtAndTime(court, hour)?.note" class="slot-note">
                    {{ getSlotForCourtAndTime(court, hour).note }}
                  </span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="table-actions">
        <el-button @click="batchOpenSlots" type="success">批量开放</el-button>
        <el-button @click="batchCloseSlots" type="warning">批量关闭</el-button>
        <el-button @click="generateTomorrowSlots" type="primary">生成明日时段</el-button>
      </div>
    </el-card>

    <!-- 场地列表 -->
    <el-card class="court-list-card">
      <div class="card-header">
        <h3>场地列表</h3>
      </div>
      <el-table :data="courtsData" style="width: 100%">
        <el-table-column prop="name" label="场地名称" width="150" />
        <el-table-column prop="description" label="描述" width="200" />
        <el-table-column prop="pricePerHour" label="价格/小时" width="120">
          <template #default="scope">
            ¥{{ scope.row.pricePerHour }}
          </template>
        </el-table-column>
        <el-table-column prop="available" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.available ? 'success' : 'danger'">
              {{ scope.row.available ? '可用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button size="small" @click="editCourt(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="scope.row.available ? 'warning' : 'success'"
              @click="toggleCourtAvailability(scope.row)"
            >
              {{ scope.row.available ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" @click="generateCourtSlots(scope.row)" type="info">生成时段</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑场地对话框 -->
    <el-dialog 
      :title="editingCourt ? '编辑场地' : '创建场地'" 
      v-model="showCreateDialog"
      width="500px"
    >
      <el-form :model="courtForm" :rules="courtRules" ref="courtFormRef" label-width="100px">
        <el-form-item label="场地名称" prop="name">
          <el-input v-model="courtForm.name" placeholder="请输入场地名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="courtForm.description" 
            type="textarea" 
            placeholder="请输入场地描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="价格/小时" prop="pricePerHour">
          <el-input-number 
            v-model="courtForm.pricePerHour" 
            :min="0" 
            :precision="2"
            placeholder="价格"
          />
        </el-form-item>
        <el-form-item label="状态" prop="available">
          <el-switch 
            v-model="courtForm.available"
            active-text="可用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCourt" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
<<<<<<< HEAD
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Key } from '@element-plus/icons-vue'
import { adminAPI } from '../../services/api'
import { useAdminStore } from '../../store/admin'

const router = useRouter()

=======
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { adminAPI } from '../../services/api'
import { useAdminStore } from '../../store/admin'

>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
// 响应式数据
const courtsData = ref([])
const statistics = ref({
  totalCourts: 0,
  availableCourts: 0,
  totalSlots: 0,
  openSlots: 0,
  availableSlots: 0,
  occupancyRate: 0
})
const selectedDate = ref(new Date().toISOString().split('T')[0])
const timeSlots = ref([8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22]) // 8:00-23:00
const loading = ref(false)
const saving = ref(false)

const showCreateDialog = ref(false)
const editingCourt = ref(null)
const courtFormRef = ref()

// 表单数据
const courtForm = reactive({
  name: '',
  description: '',
  pricePerHour: 0,
  available: true
})

// 表单验证规则
const courtRules = {
  name: [
    { required: true, message: '请输入场地名称', trigger: 'blur' }
  ],
  pricePerHour: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ]
}

// 方法
const loadCourtData = async () => {
  loading.value = true
  try {
<<<<<<< HEAD
    // 确保token存在后再发送请求
    const adminToken = localStorage.getItem("adminToken");
    if (!adminToken) {
      throw new Error("未找到管理员token，请重新登录");
    }
    
    console.log("发送场地数据请求前的token检查:", adminToken.substring(0, 20) + "...");
    
=======
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
    const [courtsResponse, statsResponse] = await Promise.all([
      adminAPI.getCourtsForManagement(selectedDate.value),
      adminAPI.getCourtStatistics()
    ])
    
    courtsData.value = courtsResponse
    statistics.value = statsResponse
  } catch (error) {
    ElMessage.error('加载数据失败: ' + (error.message || '未知错误'))
<<<<<<< HEAD
    console.error("loadCourtData错误详情:", error)
=======
    console.error(error)
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadCourtData()
  ElMessage.success('数据已刷新')
}

<<<<<<< HEAD
const testAuth = async () => {
  try {
    const response = await adminAPI.testAuth()
    console.log('权限测试响应:', response)
    ElMessage.success('权限测试成功！认证状态: ' + (response.authenticated ? '已认证' : '未认证'))
    
    // 显示详细的认证信息
    if (response.authenticated) {
      ElMessage.info(`用户: ${response.username}, 权限: ${response.authorities.map(a => a.authority).join(', ')}`)
    }
  } catch (error) {
    console.error('权限测试失败:', error)
    ElMessage.error('权限测试失败: ' + (error.message || '未知错误'))
  }
}

=======
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
const getSlotClass = (slot) => {
  if (!slot) return 'slot-empty'
  if (!slot.isOpen) return 'slot-closed'
  if (!slot.available) return 'slot-occupied'
  return 'slot-available'
}

const getSlotStatusText = (slot) => {
  if (!slot) return '-'
  if (!slot.isOpen) return '关闭'
  if (!slot.available) return '已约'
  return '空闲'
}

// 根据场地和时间获取对应的时间段
const getSlotForCourtAndTime = (court, hour) => {
  if (!court.timeSlots) return null
  return court.timeSlots.find(slot => {
    const slotHour = parseInt(slot.startTime.split(':')[0])
    return slotHour === hour
  })
}

const toggleSlotStatus = (slot) => {
  if (!slot || !slot.id) return // 如果没有slot或没有ID，说明是空或临时生成的时段，不能操作
  
  if (slot.available && slot.isOpen) {
    // 如果是空闲状态，点击关闭
    updateSlotStatus(slot, false, slot.note)
  } else if (!slot.isOpen) {
    // 如果是关闭状态，点击开放
    updateSlotStatus(slot, true, slot.note)
  }
  // 已预约状态不允许直接切换
}

const updateSlotStatus = async (slot, isOpen, note) => {
  try {
    await adminAPI.batchUpdateTimeSlots([{
      timeSlotId: slot.id,
<<<<<<< HEAD
      open: isOpen,
=======
      isOpen: isOpen,
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
      note: note || ''
    }])
    
    slot.isOpen = isOpen
    slot.note = note
    ElMessage.success('时段状态更新成功')
  } catch (error) {
    ElMessage.error('更新失败: ' + (error.message || '未知错误'))
  }
}

const toggleCourtAvailability = async (court) => {
  try {
    await adminAPI.setCourtAvailability(court.id, court.available)
    ElMessage.success(`场地已${court.available ? '启用' : '停用'}`)
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.message || '未知错误'))
    court.available = !court.available // 回滚状态
  }
}

const editCourt = (court) => {
  editingCourt.value = court
  Object.assign(courtForm, court)
  showCreateDialog.value = true
}

const saveCourt = async () => {
  try {
    await courtFormRef.value.validate()
    saving.value = true
    
    if (editingCourt.value) {
      await adminAPI.updateCourt(editingCourt.value.id, courtForm)
      ElMessage.success('场地更新成功')
    } else {
      await adminAPI.createCourt(courtForm)
      ElMessage.success('场地创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    loadCourtData()
  } catch (error) {
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

const generateCourtSlots = async (court) => {
  try {
    await adminAPI.generateTimeSlots(court.id, selectedDate.value)
    ElMessage.success('时段生成成功')
    loadCourtData()
  } catch (error) {
    ElMessage.error('生成失败: ' + (error.message || '未知错误'))
  }
}

const batchOpenSlots = async () => {
  try {
    const updates = []
    courtsData.value.forEach(court => {
      court.timeSlots?.forEach(slot => {
        // 只处理有真实ID且不是开放状态的时段（包括关闭和未定义状态）
        if (slot.id && slot.isOpen !== true) {
          updates.push({
            timeSlotId: slot.id,
<<<<<<< HEAD
            open: true,
=======
            isOpen: true,
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
            note: slot.note || ''
          })
        }
      })
    })
    
    if (updates.length === 0) {
      ElMessage.info('没有需要开放的时段')
      return
    }
    
    console.log('批量开放操作:', updates)
    await adminAPI.batchUpdateTimeSlots(updates)
    ElMessage.success(`批量开放了 ${updates.length} 个时段`)
    loadCourtData()
  } catch (error) {
    console.error('批量开放失败:', error)
    ElMessage.error('批量操作失败: ' + (error.message || '未知错误'))
  }
}

const batchCloseSlots = async () => {
  try {
    const updates = []
    courtsData.value.forEach(court => {
      court.timeSlots?.forEach(slot => {
        if (slot.id && slot.isOpen && slot.available) {
          updates.push({
            timeSlotId: slot.id,
<<<<<<< HEAD
            open: false,
=======
            isOpen: false,
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
            note: slot.note || ''
          })
        }
      })
    })
    
    if (updates.length === 0) {
      ElMessage.info('没有可以关闭的时段')
      return
    }
    
    await adminAPI.batchUpdateTimeSlots(updates)
    ElMessage.success(`批量关闭了 ${updates.length} 个时段`)
    loadCourtData()
  } catch (error) {
    ElMessage.error('批量操作失败: ' + (error.message || '未知错误'))
  }
}

const generateTomorrowSlots = async () => {
  try {
    const tomorrow = new Date()
    tomorrow.setDate(tomorrow.getDate() + 1)
    const tomorrowStr = tomorrow.toISOString().split('T')[0]
    
    const promises = courtsData.value.map(court => 
      adminAPI.generateTimeSlots(court.id, tomorrowStr)
    )
    
    await Promise.all(promises)
    ElMessage.success('明日时段生成成功')
  } catch (error) {
    ElMessage.error('生成失败: ' + (error.message || '未知错误'))
  }
}

const resetForm = () => {
  Object.assign(courtForm, {
    name: '',
    description: '',
    pricePerHour: 0,
    available: true
  })
  editingCourt.value = null
}

// 生命周期
<<<<<<< HEAD
onMounted(async () => {
  // 检查token状态
  const adminToken = localStorage.getItem("adminToken");
  console.log("=== Courts组件挂载时的token状态 ===");
  console.log("Admin Token存在:", !!adminToken);
  
  if (!adminToken) {
    console.error("❌ 未找到adminToken，重定向到登录页面");
    ElMessage.error("未找到管理员token，请重新登录");
    await router.push("/admin/login");
    return;
  }
  
  console.log("Token前缀:", adminToken.substring(0, 20) + "...");
  
  // 等待一小段时间确保所有初始化完成
  setTimeout(() => {
    loadCourtData();
  }, 100);
=======
onMounted(() => {
  loadCourtData()
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
})
</script>

<style scoped>
.courts-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
}

.header h2 {
  color: #2c3e50;
  margin: 0;
  flex-shrink: 0;
}

.date-selector {
  flex-shrink: 0;
}

.actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px;
}

.stat-value {
  font-size: 2.5em;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.time-slots-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.table-header h3 {
  margin: 0;
  color: #2c3e50;
}

.legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-color.available {
  background-color: #67c23a;
}

.legend-color.occupied {
  background-color: #f56c6c;
}

.legend-color.closed {
  background-color: #909399;
}

.time-table-container {
  overflow-x: auto;
  margin-bottom: 15px;
}

.time-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1200px;
}

.time-table th,
.time-table td {
  border: 1px solid #e4e7ed;
  text-align: center;
  padding: 8px 4px;
}

.court-header {
  background: #f5f7fa;
  font-weight: bold;
  width: 150px;
  font-size: 12px;
}

.time-header {
  background: #f5f7fa;
  font-weight: bold;
  width: 150px;
  position: sticky;
  left: 0;
  z-index: 10;
}

.time-row {
  height: 60px;
}

.time-name {
  background: #fafafa;
  position: sticky;
  left: 0;
  z-index: 5;
}

.court-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 4px;
}

.court-info .name {
  font-weight: bold;
  font-size: 12px;
  text-align: center;
  word-break: break-word;
}

.court-info .price {
  font-size: 10px;
  color: #666;
}

.time-info {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-weight: bold;
  font-size: 12px;
}

.time-slot {
  cursor: pointer;
  transition: all 0.2s;
  width: 80px;
  height: 60px;
  vertical-align: middle;
}

.time-slot:hover {
  transform: scale(1.05);
}

.slot-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 2px;
}

.slot-status {
  font-size: 12px;
  font-weight: bold;
}

.slot-note {
  font-size: 10px;
  color: #666;
  max-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.slot-available {
  background-color: #f0f9ff;
  color: #67c23a;
}

.slot-available:hover {
  background-color: #e1f5fe;
}

.slot-occupied {
  background-color: #fff2f0;
  color: #f56c6c;
  cursor: not-allowed;
}

.slot-closed {
  background-color: #f5f5f5;
  color: #909399;
}

.slot-closed:hover {
  background-color: #e8e8e8;
}

.slot-empty {
  background-color: #fafafa;
  color: #c0c4cc;
  cursor: not-allowed;
}

.table-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.court-list-card {
  margin-top: 20px;
}

.card-header {
  margin-bottom: 15px;
}

.card-header h3 {
  margin: 0;
  color: #2c3e50;
}

@media (max-width: 1200px) {
  .header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .actions {
    justify-content: center;
  }
  
  .stats-cards {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }
  
  .time-table {
    min-width: 800px;
  }
  
  .time-header {
    width: 60px;
  }
  
  .time-slot {
    width: 60px;
  }
}
</style> 