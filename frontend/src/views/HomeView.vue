<template>
  <div class="home-app">
    <div class="nav-header">
      <div class="logo"><span class="brand-dot"></span> 书签管理系统</div>
      <el-button class="logout-btn outline-btn" size="small" @click="logout">退出登录</el-button>
    </div>

    <div class="layout">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <div class="sidebar-header">分类</div>
        <ul class="cate-list-menu">
          <!-- 所有书签项，本身不支持放置，因为它不是具体分类 -->
          <li 
            class="cate-item" 
            :class="{ active: selectedCategoryId === null }"
            @click="selectedCategoryId = null"
          >
            <span>
              <svg class="svg-icon menu-icon" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
              所有书签
            </span>
          </li>
          
          <!-- 具体分类项，支持拖拽放入 -->
          <li 
            v-for="cate in cateList" 
            :key="cate.id" 
            class="cate-item"
            :class="{ 
              active: selectedCategoryId === cate.id,
              'drag-over': dragHoverCategoryId === cate.id
            }"
            @click="selectedCategoryId = cate.id"
            @dragover.prevent="onDragOver(cate.id)"
            @dragleave="onDragLeave"
            @drop="onDrop($event, cate.id)"
          >
            <span class="cate-name">
              <svg class="svg-icon menu-icon" viewBox="0 0 24 24"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path></svg>
              {{ cate.name }}
            </span>
            <div class="cate-actions">
              <span class="action-icon" @click.stop="openEditCate(cate)" title="编辑">
                <svg class="svg-icon" viewBox="0 0 24 24"><path d="M12 20h9"></path><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path></svg>
              </span>
              <span class="action-icon" @click.stop="delCate(cate.id)" title="删除">
                <svg class="svg-icon" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
              </span>
            </div>
          </li>
        </ul>

        <div class="sidebar-footer">
          <input 
            v-model="cateName" 
            placeholder="新分类名称..." 
            class="modern-input"
            @keyup.enter="addCate"
          />
          <button class="primary-btn small-btn" @click="addCate">添加分类</button>
        </div>
      </aside>

      <!-- 主要内容区 -->
      <main class="content">
        <div class="content-header">
          <h2 class="category-title">
            <span v-if="selectedCategoryId !== null" class="brand-dot"></span>
            {{ currentCategoryName }}
          </h2>
          <div class="book-add-bar" v-if="selectedCategoryId !== null">
            <input v-model="bookTitle" placeholder="书签名称" class="modern-input" />
            <input v-model="bookUrl" placeholder="网址 (如 https://...)" class="modern-input url-input" />
            <button class="primary-btn" @click="addBook">添加书签</button>
          </div>
          <div class="book-add-bar disabled-bar" v-else>
            <span><span class="brand-dot small"></span> 请先在左侧选择一个特定分类，然后再添加书签。</span>
          </div>
        </div>

        <div class="book-grid">
          <div 
            v-for="book in filteredBookList" 
            :key="book.id" 
            class="book-card"
            draggable="true"
            @dragstart="onDragStart($event, book)"
            @dragend="onDragEnd"
          >
            <div class="drag-handle">⋮⋮</div>
            <div class="book-info">
              <a :href="book.url" target="_blank" class="book-title">{{ book.title }}</a>
              <span class="book-url-text">{{ book.url }}</span>
            </div>
            <div class="book-actions">
               <span class="action-icon" @click="openEditBook(book)" title="编辑">
                 <svg class="svg-icon" viewBox="0 0 24 24"><path d="M12 20h9"></path><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path></svg>
               </span>
               <span class="action-icon" @click="delBook(book.id)" title="删除">
                 <svg class="svg-icon" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
               </span>
            </div>
          </div>
        </div>
        
        <div v-if="filteredBookList.length === 0" class="empty-state">
          <svg class="svg-icon empty-icon" viewBox="0 0 24 24"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path></svg>
          该分类下暂无书签
        </div>
      </main>
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="showEditCate" title="编辑分类" width="400px">
      <input v-model="editCate.name" class="modern-input full-width" />
      <template #footer>
        <button class="outline-btn small-btn mr-10" @click="showEditCate = false">取消</button>
        <button class="primary-btn small-btn" @click="saveEditCate">保存</button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEditBook" title="编辑书签" width="400px">
      <input v-model="editBook.title" class="modern-input full-width mb-10" placeholder="标题" />
      <input v-model="editBook.url" class="modern-input full-width" placeholder="URL" />
      <template #footer>
        <button class="outline-btn small-btn mr-10" @click="showEditBook = false">取消</button>
        <button class="primary-btn small-btn" @click="saveEditBook">保存</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import request from "../utils/request.js";

const router = useRouter();

// User Info
const currentUser = JSON.parse(localStorage.getItem("user") || "{}");
const userId = currentUser.id;

// State
const cateList = ref([]);
const bookList = ref([]);
const selectedCategoryId = ref(null);
const dragHoverCategoryId = ref(null);

const cateName = ref("");
const bookTitle = ref("");
const bookUrl = ref("");
const showEditCate = ref(false);
const editCate = ref({});
const showEditBook = ref(false);
const editBook = ref({});

// Computed
const currentCategoryName = computed(() => {
  if (selectedCategoryId.value === null) return "所有书签";
  const cate = cateList.value.find(c => c.id === selectedCategoryId.value);
  return cate ? cate.name : "未知分类";
});

const filteredBookList = computed(() => {
  if (selectedCategoryId.value === null) {
    return bookList.value;
  }
  return bookList.value.filter(b => b.categoryId === selectedCategoryId.value);
});

// Drag and Drop Handlers
const onDragStart = (event, book) => {
  // 必须设置数据，Firefox 等浏览器才允许拖拽
  event.dataTransfer.setData('text/plain', book.id);
  event.dataTransfer.effectAllowed = 'move';
  // 可以在这里设置拖动时的透明度等效果
  event.target.style.opacity = '0.5';
};

const onDragEnd = (event) => {
  event.target.style.opacity = '1';
  dragHoverCategoryId.value = null;
};

const onDragOver = (cateId) => {
  // 只在不同分类间拖拽才高亮
  dragHoverCategoryId.value = cateId;
};

const onDragLeave = () => {
  dragHoverCategoryId.value = null;
};

const onDrop = async (event, targetCateId) => {
  dragHoverCategoryId.value = null;
  const bookIdStr = event.dataTransfer.getData('text/plain');
  if (!bookIdStr) return;
  
  const bookId = parseInt(bookIdStr, 10);
  const book = bookList.value.find(b => b.id === bookId);
  
  if (book && book.categoryId !== targetCateId) {
    // 乐观更新（提前在前端修改，体验更流畅）
    book.categoryId = targetCateId;
    
    // 调用后端保存
    try {
      await request.post("/bookmark/add", book);
      ElMessage.success("书签已移动");
      loadData();
    } catch (e) {
      ElMessage.error("移动失败");
      loadData(); // 恢复原始状态
    }
  }
};


// API Logic
const loadData = async () => {
  const c = await request.get(`/category/list?userId=${userId}`);
  const b = await request.get(`/bookmark/list?userId=${userId}`);
  cateList.value = c.data.data;
  bookList.value = b.data.data;
};

const addCate = async () => {
  if (!cateName.value.trim()) return;
  await request.post("/category/add", { name: cateName.value, userId });
  cateName.value = "";
  loadData();
};

const delCate = async (id) => {
  if (selectedCategoryId.value === id) {
    selectedCategoryId.value = null;
  }
  await request.post("/category/delete", { id });
  ElMessage.success("分类已删除");
  loadData();
};

const openEditCate = (cate) => {
  editCate.value = { ...cate };
  showEditCate.value = true;
};

const saveEditCate = async () => {
  await request.post("/category/add", editCate.value);
  showEditCate.value = false;
  loadData();
};

const addBook = async () => {
  if (!bookTitle.value.trim() || !bookUrl.value.trim()) {
    ElMessage.warning("请填写书签名称和网址");
    return;
  }
  await request.post("/bookmark/add", {
    title: bookTitle.value,
    url: bookUrl.value,
    categoryId: selectedCategoryId.value,
    userId,
  });
  bookTitle.value = "";
  bookUrl.value = "";
  loadData();
};

const delBook = async (id) => {
  await request.post("/bookmark/delete", { id });
  ElMessage.success("书签已删除");
  loadData();
};

const openEditBook = (book) => {
  editBook.value = { ...book };
  showEditBook.value = true;
};

const saveEditBook = async () => {
  await request.post("/bookmark/add", editBook.value);
  showEditBook.value = false;
  loadData();
};

const logout = () => {
  localStorage.removeItem("user");
  ElMessage.success("已退出登录");
  router.push({ name: "Login" });
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
/* 继承登录页的高级字体 */
.home-app {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  color: #222222;
}

/* --- SVG 与品牌元素 --- */
.brand-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  background-color: #DDA142; /* 吃豆人黄 */
  border-radius: 50%;
  margin-right: 8px;
}
.brand-dot.small {
  width: 8px;
  height: 8px;
}
.svg-icon {
  width: 16px;
  height: 16px;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
  fill: none;
}
.menu-icon {
  margin-right: 8px;
  vertical-align: -3px;
  opacity: 0.7;
}
.active .menu-icon {
  opacity: 1;
  stroke: #DDA142;
}

/* 顶部导航 */
.nav-header {
  height: 60px;
  background-color: #333333;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  z-index: 10;
}
.logo {
  font-size: 20px;
  font-weight: 500;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
}

/* 核心布局 */
.layout {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 侧边栏 */
.sidebar {
  width: 280px;
  background-color: white;
  border-right: 1px solid rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  z-index: 5;
}
.sidebar-header {
  padding: 20px;
  font-size: 14px;
  color: #999999;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
}
.cate-list-menu {
  list-style: none;
  padding: 0 10px;
  margin: 0;
  flex: 1;
  overflow-y: auto;
}
.cate-item {
  padding: 12px 15px;
  margin-bottom: 5px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}
.cate-item:hover {
  background-color: #f9f9f9;
}
.cate-item.active {
  background-color: #f0f0f0;
  color: #111111;
  font-weight: 500;
}
.cate-item.drag-over {
  background-color: #EAEAEA;
  border: 1px dashed #333333;
  transform: scale(1.02);
}
.cate-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
}
.cate-actions {
  display: none;
  gap: 8px;
}
.cate-item:hover .cate-actions {
  display: flex;
}
.action-icon {
  opacity: 0.5;
  cursor: pointer;
  transition: opacity 0.2s, color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.action-icon:hover {
  opacity: 1;
  color: #DDA142;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 主内容区 */
.content {
  flex: 1;
  padding: 40px;
  overflow-y: auto;
  background-color: #fbfbfb;
}
.content-header {
  margin-bottom: 30px;
}
.category-title {
  font-size: 28px;
  font-weight: 500;
  margin-bottom: 20px;
  color: #222222;
  display: flex;
  align-items: center;
}

/* 顶部操作栏 */
.book-add-bar {
  display: flex;
  gap: 15px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.03);
  border: 1px solid rgba(0,0,0,0.04);
}
.disabled-bar {
  background: transparent;
  box-shadow: none;
  border: 1px dashed #cccccc;
  color: #999999;
  justify-content: center;
  padding: 15px;
  display: flex;
  align-items: center;
}
.url-input {
  flex: 1;
}

/* 书签网格 */
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}
.book-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
  border: 1px solid rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: grab;
}
.book-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}
.book-card:active {
  cursor: grabbing;
}
.drag-handle {
  color: #cccccc;
  font-size: 18px;
  cursor: grab;
  user-select: none;
  line-height: 1;
}
.book-info {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.book-title {
  font-weight: 500;
  color: #333333;
  text-decoration: none;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.book-title:hover {
  text-decoration: underline;
}
.book-url-text {
  font-size: 12px;
  color: #999999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.book-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 15px;
  padding: 80px;
  color: #bbbbbb;
  font-size: 16px;
}
.empty-icon {
  width: 48px;
  height: 48px;
  stroke: #dddddd;
  stroke-width: 1.5;
}

/* 共用现代组件样式 */
.modern-input {
  padding: 12px 20px;
  background-color: #EAEAEA;
  border: 1px solid transparent;
  border-radius: 30px;
  font-size: 14px;
  color: #222222;
  outline: none;
  transition: all 0.3s;
}
.modern-input:focus {
  background-color: #ffffff;
  border: 1px solid #c0c0c0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.full-width {
  width: 100%;
  box-sizing: border-box;
}

.primary-btn {
  background-color: #333333;
  color: #ffffff;
  border: none;
  padding: 12px 25px;
  border-radius: 30px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}
.primary-btn:hover {
  background-color: #111111;
  transform: translateY(-1px);
}
.outline-btn {
  background-color: transparent;
  border: 1px solid #ffffff;
  color: #ffffff;
  padding: 10px 20px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.2s;
}
.outline-btn:hover {
  background-color: rgba(255,255,255,0.1);
}
.small-btn {
  padding: 10px 15px;
  font-size: 13px;
}
.outline-btn.small-btn {
  border-color: #333;
  color: #333;
}
.outline-btn.small-btn:hover {
  background-color: #f5f5f5;
}
.mb-10 { margin-bottom: 10px; }
.mr-10 { margin-right: 10px; }
</style>
