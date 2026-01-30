// Daily Sales Report Types
export interface DailySalesData {
    id: string;
    date: string;
    clientId: string;
    clientName: string;
    invoicedOrdersCount: number;
    invoicedItemsCount: number;
    totalRevenue: number;
}

// Custom Report Types
export interface ProductSales {
    barcode: string;
    productName: string;
    quantity: number;
    revenue: number;
}

export interface ClientSalesReport {
    clientId: string;
    clientName: string;
    products: ProductSales[];
    totalQuantity: number;
    invoicedOrdersCount: number;
    totalRevenue: number;
    minPrice?: number;
    maxPrice?: number;
    avgPrice?: number;
}

// Report Filters
export interface ReportFilters {
    fromDate: string;
    toDate: string;
    clientId?: string;
}
