import qs from 'qs'

export function sanitizeQuery(query?: Record<string, any>) {
  if (!query) return undefined

  const clean: Record<string, any> = { ...query }
  for (const key of Object.keys(clean)) {
    if (clean[key] === undefined || clean[key] === null) {
      delete clean[key]
    }
  }

  return clean
}

export function toQueryString(query?: Record<string, any>) {
  if (!query) return ''
  const clean = sanitizeQuery(query)
  if (!clean || Object.keys(clean).length === 0) return ''
  return qs.stringify(clean)
}
