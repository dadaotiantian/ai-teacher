export interface IncomingMessage {
  msgId: number
  uid: number
  body: Record<string, unknown>
}
