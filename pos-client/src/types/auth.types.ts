export interface LoginForm {
  email: string;
  password: string;
}

export interface AuthData {
  token: string;
  email: string;
  name: string;
  role: 'SUPERVISOR' | 'USER';
}
