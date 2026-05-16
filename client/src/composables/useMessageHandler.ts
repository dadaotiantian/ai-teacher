import { wsService } from '../services/wsService'

export function useMessageHandler() {
  return wsService.on.bind(wsService)
}
