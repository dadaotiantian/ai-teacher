import { defineStore } from 'pinia'
import { ClientFunctionDef, ServerFunctionDef } from '../types/protocol'
import { wsService } from '../services/wsService'
import type { Agent, Player, WordInfo } from '../types/models'

type ViewName = 'login' | 'players' | 'create-player' | 'create-agent' | 'chat'

const USERNAME_KEY = 'last_username'
const PASSWORD_KEY = 'last_password'
const PLAYER_UID_KEY = 'last_player_uid'
const PLAYER_NAME_KEY = 'last_player_name'
const MAX_NAME_LENGTH = 40

export const useAppStore = defineStore('app', {
  state: () => ({
    view: 'login' as ViewName,
    accountId: Number(localStorage.getItem('account_id') || 0),
    token: localStorage.getItem('token') || '',
    uid: Number(localStorage.getItem(PLAYER_UID_KEY) || localStorage.getItem('uid') || 0),
    username: localStorage.getItem(USERNAME_KEY) || '',
    players: [] as Player[],
    agents: [] as Agent[],
    activeAgentId: 0,
    messages: [] as Array<{ from: 'user' | 'agent' | 'system'; text: string }>,
    currentWord: null as WordInfo | null,
    error: '',
    loading: false,
    initializing: false,
    settingsOpen: false
  }),
  actions: {
    async start() {
      wsService.connect()
      await this.autoLogin()
    },
    async autoLogin() {
      const username = localStorage.getItem(USERNAME_KEY) || ''
      const password = localStorage.getItem(PASSWORD_KEY) || ''
      if (!username || !password) {
        this.view = 'login'
        return
      }

      this.initializing = true
      const ok = await this.login(username, password, true)
      this.initializing = false
      if (!ok) {
        this.clearLocalSession()
        this.view = 'login'
      }
    },
    async register(username: string, password: string) {
      return this.auth(ClientFunctionDef.REGISTER_REQ, ServerFunctionDef.REGISTER_RSP, username, password, true)
    },
    async login(username: string, password: string, autoSelect = false) {
      return this.auth(ClientFunctionDef.LOGIN_REQ, ServerFunctionDef.LOGIN_RSP, username, password, autoSelect)
    },
    async auth(req: ClientFunctionDef, rsp: ServerFunctionDef, username: string, password: string, autoSelect: boolean) {
      const cleanUsername = username.trim()
      const cleanPassword = password.trim()
      if (!cleanUsername || cleanUsername.length > MAX_NAME_LENGTH) {
        this.error = `账号名称不能为空，且不能超过 ${MAX_NAME_LENGTH} 个字符`
        return false
      }
      if (!cleanPassword) {
        this.error = '密码不能为空'
        return false
      }

      this.loading = true
      this.error = ''
      const body = await wsService.request(req, 0, { username: cleanUsername, password }, rsp)
      this.loading = false
      if (body.result !== 0) {
        this.error = String(body.message || '认证失败')
        return false
      }

      this.accountId = Number(body.account_id)
      this.username = String(body.username || cleanUsername)
      this.token = String(body.token)
      localStorage.setItem('account_id', String(this.accountId))
      localStorage.setItem('token', this.token)
      localStorage.setItem(USERNAME_KEY, cleanUsername)
      localStorage.setItem(PASSWORD_KEY, password)

      await this.loadPlayers()
      await this.enterPlayerFlow(autoSelect)
      return true
    },
    async loadPlayers() {
      const body = await wsService.request(
        ClientFunctionDef.LIST_PLAYER_REQ,
        0,
        { token: this.token },
        ServerFunctionDef.LIST_PLAYER_RSP
      )
      this.players = (body.players as Player[]) || []
    },
    async enterPlayerFlow(autoSelect: boolean) {
      if (!this.players.length) {
        this.uid = 0
        localStorage.removeItem(PLAYER_UID_KEY)
        localStorage.removeItem(PLAYER_NAME_KEY)
        localStorage.removeItem('uid')
        this.view = 'create-player'
        return
      }

      const savedUid = Number(localStorage.getItem(PLAYER_UID_KEY) || localStorage.getItem('uid') || 0)
      const savedPlayer = this.players.find((player) => player.uid === savedUid)
      if (autoSelect && savedPlayer) {
        await this.selectPlayer(savedPlayer.uid)
        return
      }

      this.view = 'players'
    },
    async createPlayer(name: string) {
      const cleanName = name.trim()
      if (!cleanName || cleanName.length > MAX_NAME_LENGTH) {
        this.error = `角色名称不能为空，且不能超过 ${MAX_NAME_LENGTH} 个字符`
        return
      }

      const body = await wsService.request(
        ClientFunctionDef.CREATE_PLAYER_REQ,
        0,
        { token: this.token, player_name: cleanName },
        ServerFunctionDef.CREATE_PLAYER_RSP
      )
      if (body.result !== 0) {
        this.error = String(body.message || '创建角色失败')
        return
      }

      await this.loadPlayers()
      const player = body.player as Player | undefined
      if (player?.uid) {
        await this.selectPlayer(player.uid)
      } else {
        this.view = 'players'
      }
    },
    async selectPlayer(uid: number) {
      const body = await wsService.request(
        ClientFunctionDef.SELECT_PLAYER_REQ,
        uid,
        { token: this.token, uid },
        ServerFunctionDef.SELECT_PLAYER_RSP
      )
      if (body.result !== 0) {
        this.error = String(body.message || '选择角色失败')
        return
      }

      this.uid = Number(body.uid)
      localStorage.setItem(PLAYER_UID_KEY, String(this.uid))
      localStorage.setItem('uid', String(this.uid))
      const selectedPlayer = this.players.find((player) => player.uid === this.uid)
      if (selectedPlayer) {
        localStorage.setItem(PLAYER_NAME_KEY, selectedPlayer.player_name)
      }

      await this.loadAgents()
      this.view = this.agents.length ? 'chat' : 'create-agent'
    },
    async deletePlayer(uid: number) {
      await wsService.request(
        ClientFunctionDef.DELETE_PLAYER_REQ,
        uid,
        { token: this.token, uid },
        ServerFunctionDef.DELETE_PLAYER_RSP
      )
      if (this.uid === uid) {
        this.uid = 0
        localStorage.removeItem(PLAYER_UID_KEY)
        localStorage.removeItem(PLAYER_NAME_KEY)
        localStorage.removeItem('uid')
      }
      await this.loadPlayers()
      await this.enterPlayerFlow(false)
    },
    async loadAgents() {
      const body = await wsService.request(
        ClientFunctionDef.LIST_AGENT_REQ,
        this.uid,
        { token: this.token },
        ServerFunctionDef.LIST_AGENT_RSP
      )
      this.agents = (body.agents as Agent[]) || []
      this.activeAgentId = this.agents[0]?.agent_id || 0
    },
    async createAgent(name: string, abilities: string[]) {
      const body = await wsService.request(
        ClientFunctionDef.CREATE_AGENT_REQ,
        this.uid,
        { token: this.token, agent_name: name, abilities: abilities.join('|'), avatar: 'default' },
        ServerFunctionDef.CREATE_AGENT_RSP
      )
      if (body.result !== 0) {
        this.error = String(body.message || '创建智能体失败')
        return
      }
      await this.loadAgents()
      this.view = 'chat'
    },
    async sendChat(text: string) {
      if (!text.trim() || !this.activeAgentId) return
      this.messages.push({ from: 'user', text })
      const body = await wsService.request(
        ClientFunctionDef.CHAT_MESSAGE_REQ,
        this.uid,
        { token: this.token, agent_id: this.activeAgentId, ability: 'dialogue', message: text },
        ServerFunctionDef.CHAT_MESSAGE_RSP
      )
      this.messages.push({ from: 'agent', text: String(body.reply || body.message || '') })
    },
    async newWord(type: string) {
      const body = await wsService.request(
        ClientFunctionDef.WORD_TEST_REQ,
        this.uid,
        { token: this.token, type },
        ServerFunctionDef.WORD_TEST_RSP
      )
      this.currentWord = body.word as WordInfo
    },
    async reviewWord(answer: string, type: string) {
      if (!this.currentWord) return
      const body = await wsService.request(
        ClientFunctionDef.WORD_REVIEW_REQ,
        this.uid,
        { token: this.token, word_id: this.currentWord.id, answer, type },
        ServerFunctionDef.WORD_REVIEW_RSP
      )
      this.messages.push({ from: 'system', text: String(body.message || '') })
      this.currentWord = null
    },
    async logoutAccount() {
      if (this.token) {
        await wsService.request(ClientFunctionDef.LOGOUT_REQ, 0, { token: this.token }, ServerFunctionDef.LOGOUT_RSP)
      }
      this.clearLocalSession()
      this.view = 'login'
    },
    clearLocalSession() {
      this.accountId = 0
      this.token = ''
      this.uid = 0
      this.username = ''
      this.players = []
      this.agents = []
      this.activeAgentId = 0
      this.messages = []
      this.currentWord = null
      this.settingsOpen = false
      localStorage.removeItem('account_id')
      localStorage.removeItem('token')
      localStorage.removeItem(PLAYER_UID_KEY)
      localStorage.removeItem(PLAYER_NAME_KEY)
      localStorage.removeItem('uid')
      localStorage.removeItem(USERNAME_KEY)
      localStorage.removeItem(PASSWORD_KEY)
    }
  }
})
