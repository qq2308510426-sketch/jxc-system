// Frontend Error Reporter - sends errors to backend for logging
export function setupErrorReporting() {
  window.addEventListener('error', (event) => {
    reportError({
      message: event.message,
      stack: event.error?.stack || '',
      url: window.location.href,
      userAgent: navigator.userAgent
    })
  })

  window.addEventListener('unhandledrejection', (event) => {
    reportError({
      message: 'Unhandled Promise: ' + (event.reason?.message || event.reason),
      stack: event.reason?.stack || '',
      url: window.location.href,
      userAgent: navigator.userAgent
    })
  })
}

function reportError(data) {
  try {
    const token = localStorage.getItem('token')
    fetch('/api/error/report', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': 'Bearer ' + token } : {})
      },
      body: JSON.stringify(data)
    }).catch(() => {})
  } catch (e) {
    // Silently fail
  }
}
