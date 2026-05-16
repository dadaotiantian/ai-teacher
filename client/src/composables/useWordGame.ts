import { useAppStore } from '../stores/app'

export function useWordGame() {
  const store = useAppStore()
  return {
    newWord: store.newWord,
    reviewWord: store.reviewWord
  }
}
