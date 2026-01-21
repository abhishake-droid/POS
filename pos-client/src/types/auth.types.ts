export interface LoginForm {
  email: string;
  password?: string; // Optional for regular users, required for supervisors
}

export interface AuthData {
  token: string;
  email: string;
  name: string;
  role: 'SUPERVISOR' | 'USER';
}
