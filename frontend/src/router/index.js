import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/student',
    name: 'Student',
    component: () => import('../components/StudentLayout.vue'),
    redirect: '/student/courses',
    children: [
      {
        path: 'courses',
        name: 'StudentCourses',
        component: () => import('../views/student/CourseList.vue')
      },
      {
        path: 'leave-apply',
        name: 'LeaveApply',
        component: () => import('../views/student/LeaveApply.vue')
      },
      {
        path: 'leave-records',
        name: 'LeaveRecords',
        component: () => import('../views/student/LeaveRecords.vue')
      }
    ]
  },
  {
    path: '/counselor',
    name: 'Counselor',
    component: () => import('../components/CounselorLayout.vue'),
    redirect: '/counselor/pending',
    children: [
      {
        path: 'pending',
        name: 'PendingList',
        component: () => import('../views/counselor/PendingList.vue')
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('../views/counselor/Statistics.vue')
      }
    ]
  },
  {
    path: '/teacher',
    name: 'Teacher',
    component: () => import('../components/TeacherLayout.vue'),
    redirect: '/teacher/courses',
    children: [
      {
        path: 'courses',
        name: 'TeacherCourses',
        component: () => import('../views/teacher/CourseList.vue')
      },
      {
        path: 'leave-list',
        name: 'LeaveList',
        component: () => import('../views/teacher/LeaveList.vue')
      },
      {
        path: 'attendance-stats',
        name: 'AttendanceStats',
        component: () => import('../views/teacher/AttendanceStats.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
