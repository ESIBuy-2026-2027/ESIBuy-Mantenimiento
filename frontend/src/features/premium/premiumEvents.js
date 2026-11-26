export const PREMIUM_STATUS_CHANGED = 'esibuy:premium-status-changed'

export function notifyPremiumStatusChanged() {
  window.dispatchEvent(new Event(PREMIUM_STATUS_CHANGED))
}
