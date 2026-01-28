export interface OperatorForm {
  email: string;
  name: string;
  password: string;
}

export interface OperatorData {
  id: string;
  email: string;
  name: string;
  role: 'SUPERVISOR' | 'USER';
}

export interface AuditLogData {
  id: string;
  operatorEmail: string;
  operatorName: string;
  action: 'LOGIN' | 'LOGOUT';
  timestamp: string;
}
