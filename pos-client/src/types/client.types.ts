export interface ClientForm {
  name: string;
  email: string;
  phone: string;
}

export interface ClientData {
  id: string;
  clientId: string;
  name: string;
  email: string;
  phone: string;
}

export type ClientSearchFilter =
    | 'clientId'
    | 'name'
    | 'email'
    | 'phone';

export interface ClientSearchParams {
  filter: ClientSearchFilter;
  query: string;
}
