import { defineStore } from 'pinia'
import { wsService } from '../services/wsService'

export const useWebSocketStore = defineStore('websocket', {
  state: () => ({
    status: wsService.status
  }),
  actions: {
    connect() {
      wsService.connect()
      this.status = wsService.status
    }
  }
})
