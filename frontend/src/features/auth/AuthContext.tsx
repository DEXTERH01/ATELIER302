import { createContext, useContext, useEffect, useMemo, useState, type PropsWithChildren } from 'react';
import * as authApi from './api/authApi';
import type { LoginInput, RegisterInput } from './api/authApi';
import type { UsuarioSesion } from '../../types/auth';
type AuthContextValue = {
  usuario: UsuarioSesion | null;
  cargando: boolean;
  login: (v: LoginInput) => Promise<void>;
  register: (v: RegisterInput) => Promise<void>;
  logout: () => Promise<void>;
};
const AuthContext = createContext<AuthContextValue | null>(null);
export function AuthProvider({ children }: PropsWithChildren) {
  const [usuario, setUsuario] = useState<UsuarioSesion | null>(null);
  const [cargando, setCargando] = useState(true);
  useEffect(() => {
    authApi
      .restoreSession()
      .then(setUsuario)
      .catch(() => setUsuario(null))
      .finally(() => setCargando(false));
  }, []);
  const value = useMemo<AuthContextValue>(
    () => ({
      usuario,
      cargando,
      login: async (v) => setUsuario((await authApi.login(v)).usuario),
      register: async (v) => setUsuario((await authApi.registerClient(v)).usuario),
      logout: async () => {
        await authApi.logout();
        setUsuario(null);
      },
    }),
    [usuario, cargando],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth requiere AuthProvider');
  return value;
}
