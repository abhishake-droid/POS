export interface ProductForm {
  barcode: string;
  clientId: string;
  name: string;
  mrp: number;
  imageUrl?: string;
}

export interface ProductData {
  id: string;
  barcode: string;
  clientId: string;
  clientName?: string;
  name: string;
  mrp: number;
  imageUrl?: string;
  quantity?: number;
}

export interface InventoryForm {
  productId: string;
  quantity: number;
}

export interface InventoryData {
  id: string;
  productId: string;
  barcode?: string;
  quantity: number;
}

export type ProductSearchFilter =
  | 'barcode'
  | 'name'
  | 'clientId'
  | 'clientName';
