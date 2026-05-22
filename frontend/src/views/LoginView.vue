<template>
  <Login @login="handleLogin" @register="handleRegister" />
</template>

<script setup>
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import Login from "../components/Login.vue";
import request from "../utils/request.js";

const router = useRouter();

const handleLogin = async (payload) => {
  const res = await request.post("/user/login", payload);
  if (res.data.code === 200) {
    // 将用户信息存入 localStorage，持久化登录状态
    localStorage.setItem("user", JSON.stringify(res.data.data));
    ElMessage.success("登录成功");
    router.push({ name: "Home" });
  } else {
    ElMessage.error("账号或密码错误");
  }
};

const handleRegister = async (payload) => {
  const res = await request.post("/user/register", payload);
  if (res.data.code === 200) {
    ElMessage.success("注册成功，请登录");
  } else {
    ElMessage.error("注册失败，用户名可能已存在");
  }
};
</script>
