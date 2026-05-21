export type ApostleRequestMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export type ApostleResponseType = 'arrayBuffer' | 'blob' | 'clone' | 'formData' | 'json' | 'text' | 'raw'

export type ApostleInit = RequestInit & {
  onUploadProgress?: (progress: { loaded: number; total: number }) => void
}

export type ApostleRequestBody = Record<string, any> | string | FormData | URLSearchParams | Blob

export type ApostleRequestObject = {
  method: ApostleRequestMethod
  url: string
  query?: Record<string, any>
  body?: ApostleRequestBody
  responseType?: ApostleResponseType
  init?: ApostleInit
}

export type ApostleOptions = {
  baseURL?: string
  init?: ApostleInit
  fetchImpl?: typeof fetch
  interceptor?: (init: ApostleInit) => ApostleInit | Promise<ApostleInit>
  effect?: {
    onSuccess?: (response: Response) => Promise<void> | void
    onError?: (error: unknown) => Promise<unknown> | unknown
  }
  transformer?: {
    request?: (body: Record<string, any>) => Record<string, any>
    response?: (body: any) => any
  }
  config?: {
    defaultResponseType?: ApostleResponseType
    inferRequestBodyContentType?: boolean
    inferResponseBodyContentType?: boolean
    parseObjectAsJSON?: boolean
  }
}
