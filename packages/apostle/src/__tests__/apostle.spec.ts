import { describe, expect, it, vi } from 'vitest'
import { Apostle } from '../Apostle'

describe('apostle', () => {
  it('query serialization skips null/undefined', async () => {
    const fetchImpl = vi.fn(async () => new Response('ok', { status: 200, headers: { 'Content-Type': 'text/plain' } }))
    const apostle = new Apostle({ fetchImpl, baseURL: 'https://api.test' })

    await apostle.get('/users', { a: 1, b: undefined, c: null })

    expect(fetchImpl).toHaveBeenCalledWith('https://api.test/users?a=1', expect.any(Object))
  })

  it('JSON request body serialization', async () => {
    const fetchImpl = vi.fn(async () => new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } }))
    const apostle = new Apostle({ fetchImpl, baseURL: 'https://api.test', config: { inferRequestBodyContentType: true } })

    await apostle.post('/users', { name: 'John' })

    const requestInit = ((fetchImpl.mock.calls[0] as unknown) as [unknown, RequestInit?])[1] as RequestInit
    expect(requestInit.body).toBe(JSON.stringify({ name: 'John' }))
    expect(new Headers(requestInit.headers).get('Content-Type')).toBe('application/json')
  })

  it('FormData and Blob bodies are not JSON-stringified', async () => {
    const fetchImpl = vi.fn(async () => new Response('ok', { status: 200 }))
    const apostle = new Apostle({ fetchImpl, baseURL: 'https://api.test', config: { inferRequestBodyContentType: true } })

    const fd = new FormData()
    fd.append('x', '1')
    const blob = new Blob(['hello'], { type: 'text/plain' })

    await apostle.post('/fd', fd)
    await apostle.post('/blob', blob)

    expect((((fetchImpl.mock.calls[0] as unknown) as [unknown, RequestInit?])[1] as RequestInit).body).toBe(fd)
    expect((((fetchImpl.mock.calls[1] as unknown) as [unknown, RequestInit?])[1] as RequestInit).body).not.toBe(JSON.stringify(blob as any))
  })

  it('absolute URL does not receive baseURL', async () => {
    const fetchImpl = vi.fn(async () => new Response('ok', { status: 200 }))
    const apostle = new Apostle({ fetchImpl, baseURL: 'https://api.test' })

    await apostle.put('https://signed-upload-url.com/file', new Blob(['x']))

    expect(((fetchImpl.mock.calls[0] as unknown) as [unknown, RequestInit?])[0]).toBe('https://signed-upload-url.com/file')
  })

  it('response type inference + raw response return', async () => {
    const jsonResponse = new Response(JSON.stringify({ ok: true }), { status: 200, headers: { 'Content-Type': 'application/json; charset=utf-8' } })
    const rawResponse = new Response('raw', { status: 200, headers: { 'Content-Type': 'text/plain' } })
    const fetchImpl = vi.fn()
    fetchImpl.mockResolvedValueOnce(jsonResponse)
    fetchImpl.mockResolvedValueOnce(rawResponse)
    const apostle = new Apostle({ fetchImpl, baseURL: 'https://api.test', config: { inferResponseBodyContentType: true } })

    const parsed = await apostle.get('/json')
    const raw = await apostle.get('/raw', undefined, 'raw')

    expect(parsed).toEqual({ ok: true })
    expect(raw).toBeInstanceOf(Response)
  })

  it('error effect receives failed response', async () => {
    const onError = vi.fn(async (error: unknown) => error)
    const fetchImpl = vi.fn(async () => new Response('nope', { status: 500 }))
    const apostle = new Apostle({ fetchImpl, baseURL: 'https://api.test', effect: { onError } })

    await expect(apostle.get('/fail')).rejects.toBeInstanceOf(Response)
    expect(onError).toHaveBeenCalled()
  })

  it('upload progress path does not break Blob upload', async () => {
    const progress: Array<{ loaded: number; total: number }> = []
    const fetchImpl = vi.fn(async () => new Response('ok', { status: 200 }))
    const apostle = new Apostle({ fetchImpl, baseURL: 'https://api.test' })
    const blob = new Blob(['abcdef'])

    await apostle.post('/upload', blob, undefined, 'text', {
      onUploadProgress: (p) => progress.push(p),
    })

    expect(progress.length).toBeGreaterThan(0)
    expect(progress[0]?.loaded).toBe(0)
    expect(progress[progress.length - 1]?.total).toBe(blob.size)
  })
})
