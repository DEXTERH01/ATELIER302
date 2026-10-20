export type Rol='ADMINISTRADOR'|'ENCARGADO'|'ESPECIALISTA'|'CLIENTE';
export interface UsuarioSesion{id:string;correo:string;nombreCompleto:string;rol:Rol}
export interface AuthResponse{accessToken:string;tokenType:'Bearer';expiresIn:number;usuario:UsuarioSesion}

