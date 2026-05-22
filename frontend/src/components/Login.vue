<template>
  <div class="login-view-container">
    <!-- 背景元素 -->
    <div class="bg-container">
        <!-- 左上角浅灰色圆形 -->
        <div class="top-left-circle"></div>
        
        <!-- 底部山脉与吃豆人 -->
        <svg class="bottom-mountains" viewBox="0 0 1440 900" preserveAspectRatio="xMidYMax slice">
            <g id="scrolling-mountains" ref="mountainsRef">
                <!-- 错落有致的山脉（左低右高） -->
                <path class="mountain" fill="#333333" stroke="#333333" stroke-width="1.5" d="M 0,900 L 0,650 Q 350,450 780,780 Q 1150,350 1440,650 L 1440,900 Z" />
                <!-- 左侧的补充山脉，用于无缝循环 -->
                <path class="mountain" fill="#333333" stroke="#333333" stroke-width="1.5" transform="translate(-1440, 0)" d="M 0,900 L 0,650 Q 350,450 780,780 Q 1150,350 1440,650 L 1440,900 Z" />
            </g>
            
            <!-- 黄色吃豆人，站在右侧山脉边缘，嘴巴朝左 -->
            <g id="pacman" class="pacman-group" ref="pacmanRef" transform="translate(1200, 485)">
                <path class="pacman-mouth" fill="#DDA142" d="M 0,0 L -65,-37.5 A 75,75 0 1,1 -65,37.5 Z" />
                <circle class="pacman-eye" cx="-15" cy="-35" r="9" fill="#000" />
            </g>
        </svg>
    </div>

    <!-- 登录卡片容器 -->
    <div class="login-wrapper">
        <main class="login-card">
            <h2>欢迎使用</h2>
            <form action="#" method="POST" @submit.prevent>
                <div class="input-group">
                    <input type="text" v-model="form.username" placeholder="用户名" required autocomplete="username">
                </div>
                <div class="input-group">
                    <input type="password" v-model="form.password" placeholder="密码" required autocomplete="current-password">
                </div>
                <div class="button-container">
                    <button type="button" class="login-btn primary-btn" @click="handleLogin">登录</button>
                    <button type="button" class="login-btn outline-btn" @click="handleRegister">注册</button>
                </div>
            </form>
        </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';

const emit = defineEmits(['login', 'register']);
const form = ref({ username: '', password: '' });

const handleLogin = () => {
    if (!form.value.username || !form.value.password) {
        ElMessage.warning('请输入用户名和密码！');
        return;
    }
    emit('login', form.value);
};

const handleRegister = () => {
    if (!form.value.username || !form.value.password) {
        ElMessage.warning('请输入用户名和密码！');
        return;
    }
    emit('register', form.value);
};

const mountainsRef = ref(null);
const pacmanRef = ref(null);
let animationFrameId = null;

// --- Animation Logic ---
onMounted(() => {
    let offset = 0;
    const speed = 1.5; 
    const pacmanX = 1200;
    
    const originalRadius = 75;
    const scale = 0.8; 
    const radius = originalRadius * scale; 

    let currentAngle = null;

    function getMountainY(x) {
        if (x < 780) {
            const t = (-700 + Math.sqrt(490000 + 320 * x)) / 160;
            return Math.pow(1 - t, 2) * 650 + 2 * (1 - t) * t * 450 + Math.pow(t, 2) * 780;
        } else {
            const t = (740 - Math.sqrt(547600 + 320 * (780 - x))) / 160;
            return Math.pow(1 - t, 2) * 780 + 2 * (1 - t) * t * 350 + Math.pow(t, 2) * 650;
        }
    }

    function getPhysicalCenterY(cx) {
        let minYc = Infinity;
        for (let dx = -radius; dx <= radius; dx += 2) {
            let sampleX = cx + dx;
            if (sampleX < 0) {
                sampleX = 1440 - ((-sampleX) % 1440);
            }
            sampleX = sampleX % 1440;
            
            let gy = getMountainY(sampleX);
            let requiredYc = gy - Math.sqrt(radius * radius - dx * dx);
            if (requiredYc < minYc) {
                minYc = requiredYc;
            }
        }
        return minYc;
    }

    function animate() {
        offset += speed;
        if (offset >= 1440) offset -= 1440;
        
        if (mountainsRef.value) {
           mountainsRef.value.setAttribute('transform', `translate(${offset}, 0)`);
        }
        
        let localX = pacmanX - offset;
        if (localX < 0) localX += 1440;
        
        const y = getPhysicalCenterY(localX);
        const yNext = getPhysicalCenterY(localX + 2);
        const yPrev = getPhysicalCenterY(localX - 2);
        const slope = (yNext - yPrev) / 4;
        
        const targetAngle = Math.atan(slope) * 180 / Math.PI;
        
        if (currentAngle === null) {
            currentAngle = targetAngle;
        } else {
            currentAngle += (targetAngle - currentAngle) * 0.08;
        }
        
        if (pacmanRef.value) {
            pacmanRef.value.setAttribute('transform', `translate(${pacmanX}, ${y}) scale(${scale}) rotate(${currentAngle})`);
        }
        
        animationFrameId = requestAnimationFrame(animate);
    }

    animationFrameId = requestAnimationFrame(animate);
});

onUnmounted(() => {
    if (animationFrameId) cancelAnimationFrame(animationFrameId);
});
</script>

<style scoped>
.login-view-container {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  margin: 0;
  padding: 0;
  background-color: #ffffff;
  overflow: hidden;
  height: 100vh;
  width: 100vw;
  position: fixed; /* fix it to screen to overlap everything while logged out */
  top: 0;
  left: 0;
  z-index: 1000;
}

/* ---------------- 背景元素 ---------------- */
.bg-container {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 1001;
  pointer-events: none;
}

.top-left-circle {
  position: absolute;
  top: -150px;
  left: -150px;
  width: 600px;
  height: 600px;
  background-color: #DDDDDD;
  border-radius: 50%;
  z-index: 1001;
}

.bottom-mountains {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1002;
}

/* ---------------- 登录卡片 ---------------- */
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
  position: relative;
  z-index: 1003;
}

.login-card {
  background: rgba(255, 255, 255, 0.98);
  width: 400px;
  height: 500px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 55px 45px;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08), 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0,0,0,0.06);
  transform: translateY(-35%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  animation: cardEntrance 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes cardEntrance {
  from { opacity: 0; transform: translateY(-20%); }
  to { opacity: 1; transform: translateY(-35%); }
}

.login-card h2 {
  text-align: center;
  margin-top: 0;
  margin-bottom: 45px;
  font-size: 32px;
  font-weight: 500;
  color: #222222;
  letter-spacing: 3px;
}

/* ---------------- 输入框 ---------------- */
.input-group {
  margin-bottom: 24px;
}

.input-group input {
  width: 100%;
  padding: 16px 24px;
  background-color: #EAEAEA;
  border: 1px solid transparent;
  border-radius: 30px;
  font-size: 15px;
  color: #222222;
  outline: none;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-sizing: border-box;
}

.input-group input::placeholder {
  color: #999999;
  font-weight: 400;
}

.input-group input:focus {
  background-color: #ffffff;
  border: 1px solid #c0c0c0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* ---------------- 按钮 ---------------- */
.button-container {
  display: flex;
  justify-content: space-between;
  gap: 15px;
  margin-top: 40px;
}

.login-btn {
  flex: 1;
  padding: 15px 0;
  border-radius: 30px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  letter-spacing: 2px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: none;
  box-sizing: border-box;
}

.primary-btn {
  background-color: #333333;
  color: #ffffff;
  box-shadow: 0 4px 15px rgba(51, 51, 51, 0.2);
}

.primary-btn:hover {
  background-color: #111111;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(51, 51, 51, 0.3);
}

.primary-btn:active {
  transform: translateY(1px);
  box-shadow: 0 2px 8px rgba(51, 51, 51, 0.3);
}

.outline-btn {
  background-color: transparent;
  color: #333333;
  border: 2px solid #333333;
}

.outline-btn:hover {
  background-color: #f5f5f5;
  transform: translateY(-2px);
}

.outline-btn:active {
  transform: translateY(1px);
}

/* ---------------- 动画效果 ---------------- */
.pacman-mouth {
  animation: chomp 0.8s linear infinite;
  transform-origin: 0 0;
}

@keyframes chomp {
  0%, 100% { 
    d: path("M 0,0 L -65,-37.5 A 75,75 0 1,1 -65,37.5 Z"); 
  }
  50% { 
    d: path("M 0,0 L -75,0 A 75,75 0 1,1 -75,0 Z"); 
  }
}
</style>
