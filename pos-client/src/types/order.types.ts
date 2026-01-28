export type OrderStatus = 'PLACED' | 'INVOICED' | 'CANCELLED';

export interface OrderLineForm {
  productId: string;
  productName?: string;
  barcode?: string;
  quantity: number;
  mrp: number;
  lineTotal: number;
}

export interface CreateOrderForm {
  lines: OrderLineForm[];
}

export interface OrderItemData {
  id: string;
  productId: string;
  barcode: string;
  productName: string;
  quantity: number;
  mrp: number;
  lineTotal: number;
}

export interface OrderData {
  id: string;
  orderId: string;
  createdAt: string;
  status: OrderStatus;
  totalItems: number;
  totalAmount: number;
  hasInvoice: boolean;
  items?: OrderItemData[];
}

export interface OrderSearchFilters {
  fromDate?: string;
  toDate?: string;
  status?: OrderStatus | '';
  orderId?: string;
}

export interface SalesReportRow {
  brand: string;
  category: string;
  quantitySum: number;
  revenueSum: number;
}

