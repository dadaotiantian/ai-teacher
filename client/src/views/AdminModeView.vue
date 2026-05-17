<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAppStore } from '../stores/app'

const store = useAppStore()

type ModelKey = 'deepseek' | 'kimi' | 'bailian'
type AdminTab = 'accounts' | 'models' | 'operations'

interface ModelOption {
  key: ModelKey
  name: string
  apiUrl: string
}

interface ConfiguredModel {
  key: ModelKey
  name: string
  apiKey: string
  apiUrl: string
  ownerId: number
  createdTime: number
}

const adminTabs: Array<{ key: AdminTab; label: string; description: string }> = [
  { key: 'accounts', label: 'Accounts', description: 'Manage accounts, roles, and service access.' },
  { key: 'models', label: 'Models', description: 'View configured model services.' },
  { key: 'operations', label: 'Operations', description: 'Check WebSocket status, logs, and database health.' }
]

const modelOptions: ModelOption[] = [
  { key: 'deepseek', name: 'DeepSeek', apiUrl: 'https://api.deepseek.com' },
  { key: 'bailian', name: '百炼', apiUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1' },
  { key: 'kimi', name: 'Kimi', apiUrl: 'https://api.moonshot.cn/v1' }
]

const activeTab = ref<AdminTab>('accounts')
const configuredModels = ref<ConfiguredModel[]>([])
const selectedModel = ref<ModelOption | null>(null)
const modelApiKey = ref('')
const modelError = ref('')

const modelStorageKey = computed(() => `admin_models_${store.accountId}`)
const displayModels = computed(() =>
  modelOptions.map((option) => ({
    ...option,
    configured: configuredModels.value.find((model) => model.key === option.key)
  }))
)

onMounted(() => {
  loadConfiguredModels()
})

function loadConfiguredModels() {
  const rawValue = localStorage.getItem(modelStorageKey.value)
  if (!rawValue) {
    configuredModels.value = []
    return
  }
  try {
    configuredModels.value = JSON.parse(rawValue) as ConfiguredModel[]
  } catch {
    configuredModels.value = []
  }
}

function saveConfiguredModels() {
  localStorage.setItem(modelStorageKey.value, JSON.stringify(configuredModels.value))
}

function openModelConfig(model: ModelOption) {
  selectedModel.value = model
  modelApiKey.value = ''
  modelError.value = ''
}

function closeModelConfig() {
  selectedModel.value = null
  modelApiKey.value = ''
  modelError.value = ''
}

function saveModelConfig() {
  if (!selectedModel.value) return

  const apiKey = modelApiKey.value.trim()
  if (!apiKey) {
    modelError.value = '请输入 API Key'
    return
  }

  if (configuredModels.value.some((model) => model.key === selectedModel.value?.key)) {
    closeModelConfig()
    return
  }

  configuredModels.value.push({
    key: selectedModel.value.key,
    name: selectedModel.value.name,
    apiKey,
    apiUrl: selectedModel.value.apiUrl,
    ownerId: store.accountId,
    createdTime: Date.now()
  })
  saveConfiguredModels()
  closeModelConfig()
}
</script>

<template>
  <section class="page">
    <header class="topbar">
      <div>
        <h1>Admin Console</h1>
        <p class="muted">{{ store.username }}</p>
      </div>
      <button class="secondary" @click="store.logoutAccount()">Sign Out</button>
    </header>

    <nav class="admin-tabs" aria-label="Admin sections">
      <button
        v-for="tab in adminTabs"
        :key="tab.key"
        type="button"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span>{{ tab.label }}</span>
        <small>{{ tab.key === 'models' ? `${configuredModels.length} configured` : tab.description }}</small>
      </button>
    </nav>

    <section v-if="activeTab === 'accounts'" class="admin-panel">
      <h2>Accounts</h2>
      <p class="muted">Manage accounts, roles, and service access.</p>
    </section>

    <section v-if="activeTab === 'models'" class="admin-panel">
      <header class="topbar">
        <div>
          <h2>Model Settings</h2>
          <p class="muted">Owner ID {{ store.accountId }}</p>
        </div>
      </header>
      <div class="grid">
        <article
          v-for="model in displayModels"
          :key="model.key"
          class="card model-status-card"
          :class="{ configured: model.configured, unconfigured: !model.configured }"
        >
          <div>
            <h3>{{ model.name }}</h3>
            <p class="muted">{{ model.configured?.apiUrl || model.apiUrl }}</p>
          </div>
          <p v-if="model.configured" class="model-state">已设置</p>
          <button v-else class="model-state model-state-action" @click="openModelConfig(model)">未设置</button>
          <p v-if="model.configured">Owner ID {{ model.configured.ownerId }}</p>
        </article>
      </div>
    </section>

    <section v-if="activeTab === 'operations'" class="admin-panel">
      <h2>Operations</h2>
      <p class="muted">Check WebSocket status, logs, and database health.</p>
    </section>

    <div v-if="selectedModel" class="modal-mask" @click.self="closeModelConfig">
      <section class="modal model-modal">
        <header class="topbar">
          <div>
            <h2>设置 {{ selectedModel.name }}</h2>
            <p class="muted">{{ selectedModel.apiUrl }}</p>
          </div>
          <button class="secondary" @click="closeModelConfig">Close</button>
        </header>
        <div class="field">
          <label>API Key</label>
          <input v-model="modelApiKey" placeholder="请输入 API Key" type="password" @keyup.enter="saveModelConfig" />
        </div>
        <p v-if="modelError" class="error">{{ modelError }}</p>
        <button @click="saveModelConfig">确定</button>
      </section>
    </div>
  </section>
</template>
