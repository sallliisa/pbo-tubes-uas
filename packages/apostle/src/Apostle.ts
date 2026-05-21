import { inferResponseType } from './response'
import { toQueryString } from './query'
import type { ApostleInit, ApostleOptions, ApostleRequestObject, ApostleResponseType } from './types'

const ABSOLUTE_URL = /^https?:\/\//i

export class Apostle {
  private readonly baseURL?: string
  private readonly init: ApostleInit
  private readonly fetchImpl: typeof fetch
  private readonly interceptor: NonNullable<ApostleOptions['interceptor']>
  private readonly effect: Required<NonNullable<ApostleOptions['effect']>>
  private readonly transformer: Required<NonNullable<ApostleOptions['transformer']>>
  private readonly config: Required<NonNullable<ApostleOptions['config']>>

  constructor(options: ApostleOptions) {
    this.baseURL = options.baseURL
    this.init = options.init ?? {}
    this.fetchImpl = options.fetchImpl ?? globalThis.fetch.bind(globalThis)
    this.interceptor = options.interceptor ?? ((init) => init)
    this.effect = {
      onSuccess: options.effect?.onSuccess ?? (async () => {}),
      onError: options.effect?.onError ?? (async (error) => error),
    }
    this.transformer = {
      request: options.transformer?.request ?? ((body) => body),
      response: options.transformer?.response ?? ((body) => body),
    }
    this.config = {
      defaultResponseType: options.config?.defaultResponseType ?? 'text',
      inferRequestBodyContentType: options.config?.inferRequestBodyContentType ?? false,
      inferResponseBodyContentType: options.config?.inferResponseBodyContentType ?? false,
      parseObjectAsJSON: options.config?.parseObjectAsJSON ?? true,
    }
  }

  private resolveURL(url: string) {
    if (ABSOLUTE_URL.test(url)) return url
    return `${this.baseURL ?? ''}${url}`
  }

  public async dispatch({ method, url, query, body, responseType, init }: ApostleRequestObject): Promise<any> {
    try {
      const mergedInit = await this.interceptor({ ...this.init, ...init })
      const parsedInit: ApostleInit = { ...mergedInit }
      const { onUploadProgress } = parsedInit
      if (parsedInit.onUploadProgress) delete parsedInit.onUploadProgress

      const parseBodyAsJSON = Boolean(body && body.constructor === Object && this.config.parseObjectAsJSON)
      const parsedHeaders = new Headers(parsedInit.headers)

      let finalBody: BodyInit | null | undefined = body as BodyInit

      if (this.config.inferRequestBodyContentType && parseBodyAsJSON && !parsedHeaders.has('Content-Type')) {
        parsedHeaders.set('Content-Type', 'application/json')
      }

      if (onUploadProgress && body instanceof Blob) {
        const total = body.size
        let loaded = 0
        finalBody = new ReadableStream({
          start(controller) {
            const reader = body.stream().getReader()
            onUploadProgress({ loaded: 0, total })
            function push() {
              reader
                .read()
                .then(({ done, value }) => {
                  if (done) {
                    onUploadProgress({ loaded: total, total })
                    controller.close()
                    return
                  }
                  loaded += value?.length || 0
                  onUploadProgress({ loaded, total })
                  controller.enqueue(value)
                  push()
                })
                .catch((error) => {
                  controller.error(error)
                })
            }
            push()
          },
        })
        if (!parsedHeaders.has('Content-Length')) {
          parsedHeaders.set('Content-Length', String(total))
        }
      } else if (parseBodyAsJSON) {
        finalBody = JSON.stringify(this.transformer.request(body as Record<string, any>))
      }

      const qs = toQueryString(query)
      const finalURL = `${this.resolveURL(url)}${qs ? `?${qs}` : ''}`
      const response = await Reflect.apply(this.fetchImpl, globalThis, [
        finalURL,
        {
        ...parsedInit,
        headers: parsedHeaders,
        body: finalBody,
        method,
        },
      ])

      if (!response.ok) throw response

      let resolvedType: ApostleResponseType | undefined = responseType
      if (!resolvedType && this.config.inferResponseBodyContentType) {
        resolvedType = inferResponseType(response.headers.get('Content-Type'))
      }
      if (!resolvedType) resolvedType = this.config.defaultResponseType

      await this.effect.onSuccess(response)
      return this.transformer.response(resolvedType === 'raw' ? response : await response[resolvedType]())
    } catch (error) {
      throw await this.effect.onError(error)
    }
  }

  public get(path: string, query?: Record<string, any>, responseType?: ApostleResponseType, init?: ApostleInit) {
    return this.dispatch({ method: 'GET', url: path, query, responseType, init })
  }

  public post(path: string, body?: any, query?: Record<string, any>, responseType?: ApostleResponseType, init?: ApostleInit) {
    return this.dispatch({ method: 'POST', url: path, query, body, responseType, init })
  }

  public put(path: string, body?: any, query?: Record<string, any>, responseType?: ApostleResponseType, init?: ApostleInit) {
    return this.dispatch({ method: 'PUT', url: path, query, body, responseType, init })
  }

  public patch(path: string, body?: any, query?: Record<string, any>, responseType?: ApostleResponseType, init?: ApostleInit) {
    return this.dispatch({ method: 'PATCH', url: path, query, body, responseType, init })
  }

  public delete(path: string, body?: any, query?: Record<string, any>, responseType?: ApostleResponseType, init?: ApostleInit) {
    return this.dispatch({ method: 'DELETE', url: path, query, body, responseType, init })
  }
}
