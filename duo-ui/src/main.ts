import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// Delete style.css since it has defaults that conflict
// actually we just overwrite simple CSS in App.vue and will delete the import.
app.mount('#app')
