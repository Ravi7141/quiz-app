import { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('quizvault_user')) || null
    } catch {
      return null
    }
  })

  const login = useCallback((userData) => {
    localStorage.setItem('quizvault_user', JSON.stringify(userData))
    setUser(userData)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('quizvault_user')
    setUser(null)
  }, [])

  const isAdmin = user?.role === 'ADMIN'
  const isStudent = user?.role === 'STUDENT'

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin, isStudent }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be inside AuthProvider')
  return ctx
}
