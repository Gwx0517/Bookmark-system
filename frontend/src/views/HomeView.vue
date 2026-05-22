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
          <!-- 所有书签项 -->
          <li 
            class="cate-item" 
            :class="{ active: selectedCategoryId === null }"
            @click="selectedCategoryId = null"
          >
            <div class="cate-content-wrapper">
              <span class="cate-name-text">
                <svg class="svg-icon menu-icon" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                所有书签
              </span>
            </div>
          </li>
          
          <!-- 具体分类项，支持拖拽放入与动画 -->
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
            <div class="cate-content-wrapper">
              <!-- 原本文本 -->
              <span 
                class="cate-name-text" 
                :class="{ 'hide-text': dragHoverCategoryId === cate.id || eatingCategoryId === cate.id }"
              >
                <svg class="svg-icon menu-icon" viewBox="0 0 24 24"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path></svg>
                {{ cate.name }}
              </span>
              
              <!-- 像素吃豆人动画组件 -->
              <div 
                class="pacman-container" 
                :class="{ 
                  'show-pacman': dragHoverCategoryId === cate.id || eatingCategoryId === cate.id,
                  'eating': eatingCategoryId === cate.id
                }"
              >
                <svg class="pixel-pacman" viewBox="0 0 13 13" shape-rendering="crispEdges">
                  <!-- 身体固定部分 -->
                  <rect x="4" y="0" width="5" height="1" fill="#DDA142" />
                  <rect x="2" y="1" width="9" height="1" fill="#DDA142" />
                  <rect x="1" y="2" width="11" height="1" fill="#DDA142" />
                  <rect x="0" y="3" width="13" height="1" fill="#DDA142" />
                  <rect x="0" y="9" width="13" height="1" fill="#DDA142" />
                  <rect x="1" y="10" width="11" height="1" fill="#DDA142" />
                  <rect x="2" y="11" width="9" height="1" fill="#DDA142" />
                  <rect x="4" y="12" width="5" height="1" fill="#DDA142" />
                  
                  <!-- 会根据张闭嘴动画隐藏/显示的嘴巴部分 -->
                  <rect x="0" y="4" width="11" height="1" fill="#DDA142" />
                  <rect x="11" y="4" width="2" height="1" class="pac-jaw-top" fill="#DDA142" />
                  
                  <rect x="0" y="5" width="8" height="1" fill="#DDA142" />
                  <rect x="8" y="5" width="5" height="1" class="pac-jaw-top" fill="#DDA142" />
                  
                  <rect x="0" y="6" width="6" height="1" fill="#DDA142" />
                  <rect x="6" y="6" width="7" height="1" class="pac-jaw-mid" fill="#DDA142" />
                  
                  <rect x="0" y="7" width="8" height="1" fill="#DDA142" />
                  <rect x="8" y="7" width="5" height="1" class="pac-jaw-bottom" fill="#DDA142" />
                  
                  <rect x="0" y="8" width="11" height="1" fill="#DDA142" />
                  <rect x="11" y="8" width="2" height="1" class="pac-jaw-bottom" fill="#DDA142" />
                  
                  <!-- 眼睛 -->
                  <rect x="7" y="2" width="2" height="2" fill="#000" />
                </svg>
              </div>
            </div>

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

    <!-- 右下角吃豆人垃圾桶 -->
    <div 
      class="trash-container"
      :class="{
        'dragging-active': isDraggingBookmark,
        'drag-over': dragHoverTrash,
        'eating': eatingTrash
      }"
      @dragover.prevent="dragHoverTrash = true"
      @dragleave="dragHoverTrash = false"
      @drop="onTrashDrop"
    >
      <div class="trash-wrapper">
        <!-- 像素垃圾桶 (常态) -->
        <svg class="pixel-trash" :class="{ 'hide': dragHoverTrash || eatingTrash }" viewBox="0 0 13 13" shape-rendering="crispEdges">
          <rect x="4" y="2" width="5" height="1" fill="#bbbbbb" />
          <rect x="2" y="3" width="9" height="1" fill="#bbbbbb" />
          <rect x="3" y="4" width="1" height="7" fill="#bbbbbb" />
          <rect x="9" y="4" width="1" height="7" fill="#bbbbbb" />
          <rect x="3" y="11" width="7" height="1" fill="#bbbbbb" />
          <rect x="5" y="5" width="1" height="5" fill="#bbbbbb" />
          <rect x="7" y="5" width="1" height="5" fill="#bbbbbb" />
        </svg>
        
        <!-- 镜像像素吃豆人 (觉醒态) -->
        <svg class="pixel-pacman flipped" :class="{ 'show': dragHoverTrash || eatingTrash }" viewBox="0 0 13 13" shape-rendering="crispEdges">
          <rect x="4" y="0" width="5" height="1" fill="#DDA142" />
          <rect x="2" y="1" width="9" height="1" fill="#DDA142" />
          <rect x="1" y="2" width="11" height="1" fill="#DDA142" />
          <rect x="0" y="3" width="13" height="1" fill="#DDA142" />
          <rect x="0" y="9" width="13" height="1" fill="#DDA142" />
          <rect x="1" y="10" width="11" height="1" fill="#DDA142" />
          <rect x="2" y="11" width="9" height="1" fill="#DDA142" />
          <rect x="4" y="12" width="5" height="1" fill="#DDA142" />
          
          <rect x="0" y="4" width="11" height="1" fill="#DDA142" />
          <rect x="11" y="4" width="2" height="1" class="pac-jaw-top" fill="#DDA142" />
          <rect x="0" y="5" width="8" height="1" fill="#DDA142" />
          <rect x="8" y="5" width="5" height="1" class="pac-jaw-top" fill="#DDA142" />
          <rect x="0" y="6" width="6" height="1" fill="#DDA142" />
          <rect x="6" y="6" width="7" height="1" class="pac-jaw-mid" fill="#DDA142" />
          <rect x="0" y="7" width="8" height="1" fill="#DDA142" />
          <rect x="8" y="7" width="5" height="1" class="pac-jaw-bottom" fill="#DDA142" />
          <rect x="0" y="8" width="11" height="1" fill="#DDA142" />
          <rect x="11" y="8" width="2" height="1" class="pac-jaw-bottom" fill="#DDA142" />
          <rect x="7" y="2" width="2" height="2" fill="#000" />
        </svg>
      </div>
    </div>
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

// Drag & Drop Animation State
const isDraggingBookmark = ref(false);
const dragHoverCategoryId = ref(null);
const eatingCategoryId = ref(null);

const dragHoverTrash = ref(false);
const eatingTrash = ref(false);

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
  event.dataTransfer.setData('text/plain', book.id);
  event.dataTransfer.effectAllowed = 'move';
  event.target.style.opacity = '0.5';
  isDraggingBookmark.value = true;
};

const onDragEnd = (event) => {
  event.target.style.opacity = '1';
  dragHoverCategoryId.value = null;
  dragHoverTrash.value = false;
  isDraggingBookmark.value = false;
};

const onDragOver = (cateId) => {
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
    // 触发吃豆人吞咽动画
    eatingCategoryId.value = targetCateId;
    
    // 延迟执行保存，等待动画结束
    setTimeout(async () => {
      eatingCategoryId.value = null; 
      book.categoryId = targetCateId; 
      
      try {
        await request.post("/bookmark/add", book);
        ElMessage.success("书签已移动");
        loadData();
      } catch (e) {
        ElMessage.error("移动失败");
        loadData(); 
      }
    }, 600); 
  }
};

const onTrashDrop = async (event) => {
  dragHoverTrash.value = false;
  const bookIdStr = event.dataTransfer.getData('text/plain');
  if (!bookIdStr) return;
  
  const bookId = parseInt(bookIdStr, 10);
  
  // 触发垃圾桶吞噬动画
  eatingTrash.value = true;
  
  setTimeout(async () => {
    eatingTrash.value = false; 
    
    // 乐观删除
    bookList.value = bookList.value.filter(b => b.id !== bookId);
    
    try {
      await request.post("/bookmark/delete", { id: bookId });
      ElMessage.success("书签已粉碎");
      loadData();
    } catch (e) {
      ElMessage.error("删除失败");
      loadData(); 
    }
  }, 600);
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
  height: 48px; /* 统一高度，以更高的那个为准 */
  padding: 0 15px; /* 取消上下 padding，靠 align-items 居中 */
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
}

/* --- 吃豆人动画容器与文本 --- */
.cate-content-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden; /* 防止缩放导致的抖动溢出 */
  height: 100%; /* 占满高度以保证居中对齐 */
}
.cate-name-text {
  flex: 1;
  display: flex;
  align-items: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform-origin: left center;
}
/* 当悬浮或正在吃时，隐藏文字，向左缩小滑动 */
.cate-name-text.hide-text {
  opacity: 0;
  transform: scale(0.5) translateX(-20px);
}

.pacman-container {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) translateX(-20px) scale(0.5);
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  pointer-events: none; /* 穿透鼠标事件，保证 drop 触发 */
}
/* 悬浮时，吃豆人向右放大滑出 */
.pacman-container.show-pacman {
  opacity: 1;
  transform: translateY(-50%) translateX(0) scale(1.3);
}
/* 放下时触发吃豆人前扑 */
.pacman-container.eating {
  opacity: 1;
  animation: pounce 0.6s ease-in-out forwards;
}

/* 像素吃豆人 SVG 样式 */
.pixel-pacman {
  width: 18px;
  height: 18px;
  display: block;
}
/* 默认张嘴状态，隐藏上下颚及缝隙 */
.pac-jaw-top, .pac-jaw-bottom, .pac-jaw-mid {
  opacity: 0; 
}

/* 吞咽动画：嘴巴开合 */
.eating .pixel-pacman .pac-jaw-top,
.eating .pixel-pacman .pac-jaw-bottom {
  animation: chomp-jaw 0.15s infinite alternate;
}
.eating .pixel-pacman .pac-jaw-mid {
  animation: chomp-mid 0.15s infinite alternate;
}

@keyframes chomp-jaw {
  0%, 30% { opacity: 0; }
  70%, 100% { opacity: 1; }
}
@keyframes chomp-mid {
  0%, 30% { opacity: 0; fill: #DDA142; }
  70%, 100% { opacity: 1; fill: #222; }
}
/* 吞咽前扑位移动画 (向右扑) */
@keyframes pounce {
  0% { transform: translateY(-50%) translateX(0) scale(1.3); }
  25% { transform: translateY(-50%) translateX(-5px) scale(1.3); } /* 蓄力向后 */
  50% { transform: translateY(-50%) translateX(10px) scale(1.6); }  /* 向前猛扑 */
  75% { transform: translateY(-50%) translateX(10px) scale(1.6); opacity: 1; }
  100% { transform: translateY(-50%) translateX(10px) scale(0.5); opacity: 0; }
}

.cate-actions {
  display: flex;
  gap: 8px;
  opacity: 0; /* 默认透明，避免 display:none 导致的布局高度塌陷 */
  visibility: hidden; /* 防止不可见时还能点击 */
  transition: opacity 0.2s;
}
.cate-item:hover .cate-actions {
  opacity: 1;
  visibility: visible;
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

/* --- 右下角吃豆人垃圾桶 --- */
.trash-container {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 120px;
  height: 120px;
  background-color: white;
  border-radius: 50%;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  opacity: 0.5; /* 默认半透明 */
  z-index: 100;
  border: 2px solid transparent;
}
.trash-container.dragging-active {
  transform: scale(1.1);
  opacity: 1;
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
  border: 2px dashed #dddddd;
}
.trash-container.drag-over {
  transform: scale(1.3);
  background-color: #fcfcfc; /* 稍亮一点 */
  border-color: #DDA142; /* 黄色高亮 */
  box-shadow: 0 8px 25px rgba(221, 161, 66, 0.3);
}
.trash-container.eating {
  transform: scale(1.3);
  background-color: #fcfcfc;
  border-color: #DDA142;
  animation: pounce-left 0.6s ease-in-out forwards;
}

.trash-wrapper {
  position: relative;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pixel-trash {
  width: 56px;
  height: 56px;
  position: absolute;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform-origin: center bottom;
}
.pixel-trash.hide {
  opacity: 0;
  transform: scale(0.2);
}

.trash-wrapper .pixel-pacman {
  width: 56px;
  height: 56px;
  position: absolute;
  opacity: 0;
  transform: scale(0.2) scaleX(-1); /* 默认隐藏且翻转 */
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.trash-wrapper .pixel-pacman.show {
  opacity: 1;
  transform: scale(1.3) scaleX(-1); /* 翻转使得吃豆人朝左 */
}

/* 垃圾桶向左扑咬动画 */
@keyframes pounce-left {
  0% { transform: scale(1.3) translateX(0); }
  25% { transform: scale(1.3) translateX(4px); } /* 蓄力向右 */
  50% { transform: scale(1.5) translateX(-8px); }  /* 向左前扑咬 */
  75% { transform: scale(1.5) translateX(-8px); opacity: 1; }
  100% { transform: scale(1.1) translateX(0); opacity: 1; } /* 回到拖拽时的 scale(1.1) 状态 */
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
