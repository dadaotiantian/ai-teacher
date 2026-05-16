import { ClientFunctionDef } from '../types/protocol'
import type { IncomingMessage } from '../types/message'

type Handler = (message: IncomingMessage) => void

class WsService {
  private socket: WebSocket | null = null
  private handlers = new Map<number, Handler[]>()
  private queue: Array<{ msgId: number; uid: number; body: Record<string, unknown> }> = []
  private reconnectTimer = 0
  private heartbeatTimer = 0
  status: 'closed' | 'connecting' | 'open' = 'closed'

  connect(url = `ws://${location.hostname}:8080/ws`) {
    if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
      return
    }
    this.status = 'connecting'
    this.socket = new WebSocket(url)
    this.socket.binaryType = 'arraybuffer'
    this.socket.onopen = () => {
      this.status = 'open'
      this.flush()
      this.startHeartbeat()
    }
    this.socket.onmessage = async (event) => {
      const message = this.decode(event.data as ArrayBuffer)
      this.handlers.get(message.msgId)?.forEach((handler) => handler(message))
    }
    this.socket.onclose = () => this.scheduleReconnect(url)
    this.socket.onerror = () => this.socket?.close()
  }

  on(msgId: number, handler: Handler) {
    const list = this.handlers.get(msgId) ?? []
    list.push(handler)
    this.handlers.set(msgId, list)
    return () => this.handlers.set(msgId, list.filter((item) => item !== handler))
  }

  request(msgId: ClientFunctionDef, uid: number, body: Record<string, unknown>, rspId: number): Promise<Record<string, unknown>> {
    return new Promise((resolve) => {
      const off = this.on(rspId, (message) => {
        off()
        resolve(message.body)
      })
      this.send(msgId, uid, body)
    })
  }

  send(msgId: number, uid: number, body: Record<string, unknown>) {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      this.queue.push({ msgId, uid, body })
      this.connect()
      return
    }
    this.socket.send(this.encode(msgId, uid, body))
  }

  private flush() {
    const pending = [...this.queue]
    this.queue = []
    pending.forEach((item) => this.send(item.msgId, item.uid, item.body))
  }

  private startHeartbeat() {
    window.clearInterval(this.heartbeatTimer)
    this.heartbeatTimer = window.setInterval(() => this.send(ClientFunctionDef.HEARTBEAT_REQ, 0, {}), 30000)
  }

  private scheduleReconnect(url: string) {
    this.status = 'closed'
    window.clearInterval(this.heartbeatTimer)
    window.clearTimeout(this.reconnectTimer)
    this.reconnectTimer = window.setTimeout(() => this.connect(url), 1200)
  }

  private encode(msgId: number, uid: number, body: Record<string, unknown>) {
    const bodyBytes = new TextEncoder().encode(JSON.stringify(body))
    const buffer = new ArrayBuffer(10 + bodyBytes.length)
    const view = new DataView(buffer)
    view.setUint16(0, msgId)
    view.setUint32(2, bodyBytes.length)
    view.setUint32(6, uid)
    new Uint8Array(buffer, 10).set(bodyBytes)
    return buffer
  }

  private decode(buffer: ArrayBuffer): IncomingMessage {
    const view = new DataView(buffer)
    const msgId = view.getUint16(0)
    const length = view.getUint32(2)
    const uid = view.getUint32(6)
    const text = new TextDecoder().decode(new Uint8Array(buffer, 10, length))
    return { msgId, uid, body: text ? JSON.parse(text) : {} }
  }
}

export const wsService = new WsService()
