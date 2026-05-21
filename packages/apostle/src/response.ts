import type { ApostleResponseType } from './types'

const ianaMediaTypeMethodMap: Record<string, ApostleResponseType> = {
  'application/xhtml+xml': 'text',
  'application/json': 'json',
  'application/ld+json': 'json',
  'application/xml': 'text',
  'image/svg+xml': 'text',
}

const ianaRegistriesMethodMap: Record<string, ApostleResponseType> = {
  application: 'blob',
  audio: 'blob',
  font: 'blob',
  example: 'text',
  image: 'blob',
  message: 'text',
  model: 'blob',
  multipart: 'formData',
  text: 'text',
  video: 'blob',
}

export function inferResponseType(contentType: string | null): ApostleResponseType | undefined {
  if (!contentType) return undefined

  const normalized = contentType.split(';')[0]?.trim() || ''
  if (ianaMediaTypeMethodMap[normalized]) return ianaMediaTypeMethodMap[normalized]

  const registry = normalized.split('/')[0] || ''
  return ianaRegistriesMethodMap[registry]
}
