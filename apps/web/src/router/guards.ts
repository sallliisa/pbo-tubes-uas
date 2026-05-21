import app from '@/configs/app'
import { getDefaultAuthenticatedRouteLocation } from './navigation'
import type { NavigationGuard } from 'vue-router'

export function isPublicRoute(routeName: unknown): boolean {
  return app.unprotectedRoutes.includes(String(routeName))
}

export function createAuthGuard(): NavigationGuard {
  return (to) => {
    if (isPublicRoute(to.name)) {
      if (String(to.name) === 'login') {
        return getDefaultAuthenticatedRouteLocation() ?? { name: 'not-found' }
      }
      return true
    }

    if (to.path === '/') {
      return getDefaultAuthenticatedRouteLocation() ?? { name: 'not-found' }
    }

    if (!to.matched.length) {
      return { name: 'not-found' }
    }

    return true
  }
}
