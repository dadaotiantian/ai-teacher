export interface Player {
  uid: number
  account_id: number
  player_name: string
  level: number
  experience: number
  created_time: number
  last_login_time: number
}

export interface Agent {
  agent_id: number
  uid: number
  agent_name: string
  abilities: string
  avatar?: string
  created_time: number
}

export interface WordInfo {
  id: number
  word_str: string
  pronunciation_uk?: string
  pronunciation_us?: string
  meaning_zh?: string
  difficulty?: number
  grade?: number
}
