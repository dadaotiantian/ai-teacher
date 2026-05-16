export function isValidName(value: string, min = 2, max = 12) {
  return value.trim().length >= min && value.trim().length <= max
}
