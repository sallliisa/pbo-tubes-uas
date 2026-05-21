import { beforeEach, describe, expect, it, vi } from 'vitest'

const getDefaultRouteSpy = vi.fn(() => ({ name: 'dashboard' }))

vi.mock('../navigation', () => ({
  getDefaultAuthenticatedRouteLocation: () => getDefaultRouteSpy(),
}))

import { createAuthGuard } from '../guards'

const next = (() => {}) as any

describe('createAuthGuard', () => {
  beforeEach(() => {
    getDefaultRouteSpy.mockClear()
    getDefaultRouteSpy.mockReturnValue({ name: 'dashboard' })
  })

  it('redirects root to first menu route', () => {
    const guard = createAuthGuard()
    const result = guard({ name: undefined, fullPath: '/', path: '/', matched: [] } as any, {} as any, next)

    expect(result).toEqual({ name: 'dashboard' })
  })

  it('allows protected route', () => {
    const guard = createAuthGuard()
    const result = guard(
      { name: 'website', fullPath: '/authenticated/website/website', path: '/authenticated/website/website', matched: [{}] } as any,
      {} as any,
      next,
    )

    expect(result).toBe(true)
  })

  it('redirects login to first menu route', () => {
    const guard = createAuthGuard()
    const result = guard(
      { name: 'login', fullPath: '/unauthenticated/auth/login', path: '/unauthenticated/auth/login', matched: [{}] } as any,
      {} as any,
      next,
    )

    expect(result).toEqual({ name: 'dashboard' })
  })

  it('falls through to not-found for unmatched route', () => {
    const guard = createAuthGuard()
    const result = guard({ name: undefined, fullPath: '/missing', path: '/missing', matched: [] } as any, {} as any, next)

    expect(result).toEqual({ name: 'not-found' })
  })
})
