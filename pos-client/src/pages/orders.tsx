import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  MenuItem,
  Pagination,
  TextField,
  Typography,
  Collapse,
  CircularProgress,
  Chip,
  Autocomplete,
  Grid,
  Card,
  CardContent,
  CardHeader,
  CardActions,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
} from '@mui/material';
import {
  Add,
  Search,
  ExpandMore,
  ExpandLess,
  Receipt,
  Cancel,
  Download,
  FirstPage,
  LastPage,
  NavigateBefore,
  NavigateNext,
  ChevronLeft,
  ChevronRight,
  Edit as EditIcon,
  Refresh as RetryIcon,
} from '@mui/icons-material';
import { styled } from '@mui/material/styles';
import { toastError, toastSuccess, toastWarning } from '../utils/toast';
import {
  CreateOrderForm,
  OrderData,
  OrderLineForm,
  OrderSearchFilters,
  OrderStatus,
} from '../types/order.types';
import { orderService } from '../services/order.service';
import { productService } from '../services/product.service';
import { ProductData } from '../types/product.types';
import { OrderItemsTable } from '../components/OrderItemsTable';
import { formatDateTimeText } from '../utils/dateFormat';
import { formatINR } from '../utils/formatNumber';

const PAGE_SIZE = 10;

const StyledContainer = styled(Container)({
  paddingTop: '3rem',
  paddingBottom: '3rem',
  minHeight: 'calc(100vh - 64px)',
});

const HeaderBox = styled(Box)({
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  marginBottom: '1.5rem',
  padding: '1.25rem 1.5rem',
  borderRadius: '16px',
  backgroundColor: '#ffffff',
  border: '1px solid #e5e7eb',
  boxShadow: '0 2px 8px rgba(15,23,42,0.08)',
});

const SectionCard = styled(Card)({
  padding: '1.5rem',
  borderRadius: '16px',
  backgroundColor: '#ffffff',
  border: '1px solid #e5e7eb',
  boxShadow: '0 2px 8px rgba(15,23,42,0.08)',
  marginBottom: '1.5rem',
});

const SearchBox = styled(Box)({
  display: 'flex',
  gap: '0.75rem',
  flexWrap: 'wrap',
  alignItems: 'center',
  padding: '1rem 1.25rem',
  marginBottom: '1.5rem',
  backgroundColor: '#ffffff',
  borderRadius: '14px',
  border: '1px solid #e5e7eb',
});

const PaginationBox = styled(Box)({
  display: 'flex',
  justifyContent: 'center',
  gap: '1rem',
  marginTop: '2rem',
});

const StyledIconButton = styled(IconButton)({
  backgroundColor: '#1976d2',
  color: 'white',
  '&:hover': {
    backgroundColor: '#1565c0',
  },
});

export default function OrdersPage() {
  const [orders, setOrders] = useState<OrderData[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [orderForm, setOrderForm] = useState<CreateOrderForm>({
    lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
  });

  // Initialize loading state to prevent double calls
  const [initialLoad, setInitialLoad] = useState(true);

  const [products, setProducts] = useState<ProductData[]>([]);
  const [loadingProducts, setLoadingProducts] = useState(false);
  const [productSearchQuery, setProductSearchQuery] = useState('');
  const [selectedProducts, setSelectedProducts] = useState<Map<number, ProductData>>(new Map());

  const [filters, setFilters] = useState<OrderSearchFilters>({});
  const [filterType, setFilterType] = useState<'orderId' | 'dateStatus'>('dateStatus');
  const [expandedOrderId, setExpandedOrderId] = useState<string | null>(null);
  const [viewOrderData, setViewOrderData] = useState<OrderData | null>(null);
  const [viewDialogOpen, setViewDialogOpen] = useState(false);
  const [generatingInvoice, setGeneratingInvoice] = useState<string | null>(null);

  // Cancel confirmation dialog state
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [orderToCancel, setOrderToCancel] = useState<string | null>(null);

  // Edit order state
  const [editingOrderId, setEditingOrderId] = useState<string | null>(null);
  const [editDialogOpen, setEditDialogOpen] = useState(false);

  // Retry order state
  const [retryDialogOpen, setRetryDialogOpen] = useState(false);
  const [retryingOrderId, setRetryingOrderId] = useState<string | null>(null);

  useEffect(() => {
    // Load orders on mount and when page changes
    const loadData = async () => {
      try {
        await loadOrders(currentPage, filters);
      } catch (err) {
        console.error('Error in useEffect loadOrders:', err);
        // Error already handled in loadOrders function
      } finally {
        if (initialLoad) {
          setInitialLoad(false);
        }
      }
    };

    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage]);

  // Debounced product search
  useEffect(() => {
    if (!createDialogOpen && !editDialogOpen && !retryDialogOpen) {
      return;
    }

    const timer = setTimeout(async () => {
      setLoadingProducts(true);
      try {
        const res = await productService.search(productSearchQuery, 0, 50);
        setProducts(res.content || []);
      } catch (e: any) {
        console.error('Failed to search products:', e);
        setProducts([]);
      } finally {
        setLoadingProducts(false);
      }
    }, 200); // 300ms debounce

    return () => clearTimeout(timer);
  }, [productSearchQuery, createDialogOpen, editDialogOpen, retryDialogOpen]);

  // Auto-search for Order ID with debounce
  useEffect(() => {
    if (filterType === 'orderId' && filters.orderId !== undefined) {
      const timer = setTimeout(() => {
        setCurrentPage(0);
        loadOrders(0, filters).catch((err) => {
          console.error('Error in auto-search:', err);
        });
      }, 200); // 500ms debounce

      return () => clearTimeout(timer);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.orderId, filterType]);

  const loadOrders = async (page: number, currentFilters: OrderSearchFilters) => {
    setLoading(true);
    try {
      const res = await orderService.getAll(page, PAGE_SIZE, currentFilters);
      // Safely extract data with defaults
      setOrders(Array.isArray(res?.content) ? res.content : []);
      setTotalPages(res?.totalPages ?? 0);
    } catch (e: any) {
      // Handle all types of errors gracefully
      const status = e?.response?.status;
      const errorMsg = e?.response?.data?.message || e?.message || 'Failed to load orders';

      // Backend endpoint doesn't exist yet or server error - show empty state silently
      if (status === 404 || status === 500 || status === 503 || !e?.response) {
        setOrders([]);
        setTotalPages(0);
        // Only show warning on first load, not on every error
        if (page === 0 && Object.keys(currentFilters || {}).length === 0) {
          console.warn('Order service endpoint not available:', errorMsg);
        }
      } else {
        // Only show toast for unexpected errors
        if (status !== 401) {
          toastError(errorMsg);
        }
        setOrders([]);
        setTotalPages(0);
      }
    } finally {
      setLoading(false);
    }
  };


  const handleAddLine = () => {
    setOrderForm((prev) => ({
      ...prev,
      lines: [...prev.lines, { productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
    }));
  };

  const handleRemoveLine = (index: number) => {
    setOrderForm((prev) => ({
      ...prev,
      lines: prev.lines.filter((_, i) => i !== index),
    }));
  };

  const handleProductSelect = (index: number, product: ProductData | null) => {
    // Cache the selected product
    if (product) {
      setSelectedProducts(prev => {
        const newMap = new Map(prev);
        newMap.set(index, product);
        return newMap;
      });
    } else {
      setSelectedProducts(prev => {
        const newMap = new Map(prev);
        newMap.delete(index);
        return newMap;
      });
    }

    setOrderForm((prev) => {
      const newLines = prev.lines.map((line, i) => {
        if (i === index) {
          const mrp = product ? product.mrp : 0;
          const quantity = line.quantity || 1;
          return {
            ...line,
            productId: product ? product.id : '',
            productName: product ? product.name : undefined,
            barcode: product ? product.barcode : undefined,
            mrp,
            lineTotal: mrp * quantity,
          };
        }
        return line;
      });
      return { ...prev, lines: newLines };
    });
  };

  const handleQuantityChange = (index: number, quantity: number) => {
    setOrderForm((prev) => {
      const newLines = prev.lines.map((line, i) => {
        if (i === index) {
          const qty = quantity > 0 ? quantity : 1;
          return {
            ...line,
            quantity: qty,
            lineTotal: line.mrp * qty,
          };
        }
        return line;
      });
      return { ...prev, lines: newLines };
    });
  };

  const handleMrpChange = (index: number, mrp: number) => {
    setOrderForm((prev) => {
      const newLines = prev.lines.map((line, i) => {
        if (i === index) {
          const price = mrp >= 0 ? mrp : 0;
          return {
            ...line,
            mrp: price,
            lineTotal: price * line.quantity,
          };
        }
        return line;
      });
      return { ...prev, lines: newLines };
    });
  };

  const handleCreateOrder = async () => {
    try {
      // Validation
      if (orderForm.lines.length === 0) {
        toastError('Add at least one product line');
        return;
      }
      for (const line of orderForm.lines) {
        if (!line.productId) {
          toastError('Please select a product.');
          return;
        }
        if (line.quantity <= 0) {
          toastError('Quantity must be positive');
          return;
        }
        if (line.mrp < 0) {
          toastError('MRP cannot be negative');
          return;
        }
      }

      const result = await orderService.create(orderForm);

      // Show different messages based on fulfillability
      if (result.status === 'UNFULFILLABLE') {
        toastWarning('Order created but is UNFULFILLABLE due to insufficient inventory');
      } else {
        toastSuccess('Order created successfully');
      }

      setCreateDialogOpen(false);
      setOrderForm({
        lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
      });
      setCurrentPage(0);
      loadOrders(0, filters).catch((err) => {
        console.error('Error reloading orders:', err);
      });
    } catch (e: any) {
      const status = e.response?.status;
      const errorMsg = e.response?.data?.message || e.message || 'Failed to create order';
      if (status === 404 || status === 500 || !e.response) {
        toastError('Order creation endpoint is not available yet. Backend needs to be implemented.');
      } else {
        toastError(errorMsg);
      }
    }
  };

  const handleEditOrder = async (orderId: string) => {
    try {
      setEditingOrderId(orderId);

      // Products will load via debounced search
      setProductSearchQuery('');
      console.log('Edit dialog opening');

      const orderData = await orderService.getById(orderId);
      console.log('Order data:', orderData);

      // Convert order items to form format
      const lines = (orderData.items || []).map(item => {
        console.log('Order item:', item);
        return {
          productId: item.productId,
          quantity: item.quantity,
          mrp: item.mrp,
          lineTotal: item.lineTotal,
          productName: item.productName,
          barcode: item.barcode,
        };
      });

      console.log('Form lines:', lines);
      setOrderForm({ lines });
      setEditDialogOpen(true);
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Failed to load order details');
    }
  };

  const handleUpdateOrder = async () => {
    try {
      if (!editingOrderId) return;

      if (orderForm.lines.length === 0) {
        toastError('Add at least one product line');
        return;
      }
      for (const line of orderForm.lines) {
        if (!line.productId) {
          toastError('Please select a product.');
          return;
        }
        if (line.quantity <= 0) {
          toastError('Quantity must be positive');
          return;
        }
        if (line.mrp < 0) {
          toastError('MRP cannot be negative');
          return;
        }
      }

      const result = await orderService.update(editingOrderId, orderForm);

      // Show different messages based on order status
      if (result.status === 'UNFULFILLABLE') {
        toastWarning('Order updated but is UNFULFILLABLE due to insufficient inventory');
      } else {
        toastSuccess('Order updated successfully');
      }

      setEditDialogOpen(false);
      setEditingOrderId(null);
      setOrderForm({
        lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
      });
      setCurrentPage(0);
      loadOrders(0, filters).catch((err) => {
        console.error('Error reloading orders:', err);
      });
    } catch (e: any) {
      const status = e.response?.status;
      const errorMsg = e.response?.data?.message || e.message || 'Failed to update order';
      toastError(errorMsg);
    }
  };

  const handleFilterChange = (patch: Partial<OrderSearchFilters>) => {
    setFilters((prev) => {
      // When in orderId mode, only keep orderId filter
      if (filterType === 'orderId') {
        return { orderId: patch.orderId };
      }
      // When in dateStatus mode, exclude orderId
      const { orderId, ...rest } = prev;
      return { ...rest, ...patch };
    });
  };

  const applyFilters = () => {
    setCurrentPage(0);
    loadOrders(0, filters).catch((err) => {
      console.error('Error applying filters:', err);
    });
  };

  const clearFilters = () => {
    const reset: OrderSearchFilters = {};
    setFilters(reset);
    setCurrentPage(0);
    loadOrders(0, reset).catch((err) => {
      console.error('Error clearing filters:', err);
    });
  };

  const toggleExpand = (orderId: string) => {
    handleViewOrder(orderId);
  };

  const handleGenerateInvoice = async (orderId: string) => {
    setGeneratingInvoice(orderId);
    try {
      await orderService.generateInvoice(orderId);
      toastSuccess('Invoice generated successfully');
      // Reload orders to get updated status
      loadOrders(currentPage, filters).catch((err) => {
        console.error('Error reloading orders:', err);
      });
    } catch (e: any) {
      const status = e.response?.status;
      const errorMsg = e.response?.data?.message || e.message || 'Failed to generate invoice';
      if (status === 404 || status === 500 || !e.response) {
        toastError('Invoice generation endpoint is not available yet. Backend needs to be implemented.');
      } else {
        toastError(errorMsg);
      }
    } finally {
      setGeneratingInvoice(null);
    }
  };

  const handleDownloadInvoice = async (orderId: string) => {
    try {
      await orderService.downloadInvoice(orderId);
      toastSuccess('Invoice downloaded');
    } catch (e: any) {
      const status = e.response?.status;
      const errorMsg = e.response?.data?.message || e.message || 'Failed to download invoice';
      if (status === 404 || status === 500 || !e.response) {
        toastError('Invoice download endpoint is not available yet. Backend needs to be implemented.');
      } else {
        toastError(errorMsg);
      }
    }
  };

  const handleCancelOrder = (orderId: string) => {
    setOrderToCancel(orderId);
    setCancelDialogOpen(true);
  };

  const handleViewOrder = async (orderId: string) => {
    try {
      const orderData = await orderService.getById(orderId);
      setViewOrderData(orderData);
      setViewDialogOpen(true);
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Failed to load order details');
    }
  };

  const confirmCancelOrder = async () => {
    if (!orderToCancel) return;

    try {
      await orderService.cancel(orderToCancel);
      toastSuccess('Order cancelled');
      setCancelDialogOpen(false);
      setOrderToCancel(null);
      // Reload orders to get updated status
      loadOrders(currentPage, filters).catch((err) => {
        console.error('Error reloading orders:', err);
      });
    } catch (e: any) {
      const status = e.response?.status;
      const errorMsg = e.response?.data?.message || e.message || 'Failed to cancel order';
      if (status === 404 || status === 500 || !e.response) {
        toastError('Order cancel endpoint is not available yet.');
      } else {
        toastError(errorMsg);
      }
    }
  };

  const handleRetryOrder = async (orderId: string) => {
    try {
      setRetryingOrderId(orderId);

      // Products will load via debounced search
      setProductSearchQuery('');

      const orderData = await orderService.getById(orderId);

      // Convert order items to form format
      const lines = (orderData.items || []).map(item => ({
        productId: item.productId,
        quantity: item.quantity,
        mrp: item.mrp,
        lineTotal: item.lineTotal,
        productName: item.productName,
        barcode: item.barcode,
      }));

      setOrderForm({ lines });
      setRetryDialogOpen(true);
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Failed to load order details');
    }
  };

  const handleRetrySubmit = async () => {
    try {
      if (!retryingOrderId) return;

      if (orderForm.lines.length === 0) {
        toastError('Add at least one product line');
        return;
      }
      for (const line of orderForm.lines) {
        if (!line.productId) {
          toastError('Please select a product.');
          return;
        }
        if (line.quantity <= 0) {
          toastError('Quantity must be positive');
          return;
        }
        if (line.mrp < 0) {
          toastError('MRP cannot be negative');
          return;
        }
      }

      const result = await orderService.retry(retryingOrderId, orderForm);

      if (result.status === 'PLACED') {
        toastSuccess('Order retry successful! Order is now PLACED.');
      } else if (result.status === 'UNFULFILLABLE') {
        toastWarning('Order is still unfulfillable. Check inventory.');
      }

      setRetryDialogOpen(false);
      setRetryingOrderId(null);
      setOrderForm({
        lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
      });
      setCurrentPage(0);
      loadOrders(0, filters).catch((err) => {
        console.error('Error reloading orders:', err);
      });
    } catch (e: any) {
      const status = e.response?.status;
      const errorMsg = e.response?.data?.message || e.message || 'Failed to retry order';
      toastError(errorMsg);
    }
  };


  return (
    <StyledContainer maxWidth="lg">
      <HeaderBox>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5, color: '#111827' }}>
            Orders
          </Typography>
          <Typography variant="body2" sx={{ color: '#6b7280' }}>
            Track order lifecycle, generate invoices, and cancel non‑invoiced orders.
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => {
            setOrderForm({
              lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
            });
            setProductSearchQuery('');
            setCreateDialogOpen(true);
          }}
          sx={{ borderRadius: '999px', px: 3, py: 1 }}
        >
          Create Order
        </Button>
      </HeaderBox>

      {/* FILTERS */}
      <SearchBox>
        {/* Filter Type Selector */}
        <TextField
          label="Filter By"
          select
          size="small"
          value={filterType}
          onChange={(e) => {
            const newType = e.target.value as 'orderId' | 'dateStatus';
            setFilterType(newType);
            // Clear filters when switching types
            setFilters({});
            setCurrentPage(0);
            // Load all orders when switching
            loadOrders(0, {}).catch((err) => {
              console.error('Error loading orders:', err);
            });
          }}
          sx={{ minWidth: 180 }}
        >
          <MenuItem value="dateStatus">Date Range</MenuItem>
          <MenuItem value="orderId">Order ID</MenuItem>
        </TextField>

        {/* Conditional Filters */}
        {filterType === 'dateStatus' ? (
          <>
            <TextField
              label="Start Date"
              type="date"
              size="small"
              InputLabelProps={{ shrink: true }}
              value={filters.fromDate || ''}
              onChange={(e) => handleFilterChange({ fromDate: e.target.value })}
            />
            <TextField
              label="End Date"
              type="date"
              size="small"
              InputLabelProps={{ shrink: true }}
              value={filters.toDate || ''}
              onChange={(e) => handleFilterChange({ toDate: e.target.value })}
            />
            <TextField
              label="Status"
              select
              size="small"
              sx={{ minWidth: 140 }}
              value={filters.status || ''}
              onChange={(e) =>
                handleFilterChange({
                  status: (e.target.value || '') as OrderStatus | '',
                })
              }
              InputLabelProps={{ shrink: true }}
              SelectProps={{
                displayEmpty: true,
                renderValue: (value) => {
                  if (!value) return 'All';
                  return value as string;
                },
              }}
            >
              <MenuItem value="">All</MenuItem>
              <MenuItem value="PLACED">Placed</MenuItem>
              <MenuItem value="INVOICED">Invoiced</MenuItem>
              <MenuItem value="CANCELLED">Cancelled</MenuItem>
              <MenuItem value="UNFULFILLABLE">Unfulfillable</MenuItem>
            </TextField>
            <Button
              variant="contained"
              onClick={applyFilters}
              disabled={loading}
              sx={{ borderRadius: '999px' }}
            >
              Apply
            </Button>
            <Button
              variant="text"
              onClick={clearFilters}
              disabled={loading || (!filters.fromDate && !filters.toDate && !filters.status)}
            >
              Clear
            </Button>
          </>
        ) : (
          <>
            <TextField
              label="Order ID"
              size="small"
              placeholder="Type to search..."
              value={filters.orderId || ''}
              onChange={(e) => handleFilterChange({ orderId: e.target.value })}
              sx={{ minWidth: 250 }}
            />
            <Button
              variant="text"
              onClick={clearFilters}
              disabled={loading || !filters.orderId}
            >
              Clear
            </Button>
          </>
        )}
      </SearchBox>

      {/* ORDERS LIST */}
      <SectionCard>
        <Typography variant="h6" sx={{ mb: 2, fontWeight: 600, color: '#111827' }}>
          Orders
        </Typography>
        {loading && (
          <Box display="flex" justifyContent="center" p={4}>
            <CircularProgress />
          </Box>
        )}
        {!loading && orders.length === 0 && (
          <Box p={4} textAlign="center">
            <Typography variant="body1" color="text.secondary">
              {totalPages === 0 && currentPage === 0
                ? 'No orders found.'
                : 'No orders match the current filters.'}
            </Typography>
          </Box>
        )}
        {!loading && orders.length > 0 && (
          <>
            <Grid container spacing={3}>
              {orders.map((order) => {
                const isExpanded = expandedOrderId === order.orderId;
                const createdAt = formatDateTimeText(order.createdAt);
                return (
                  <Grid item xs={12} md={6} key={order.id}>
                    <Card
                      sx={{
                        borderRadius: '16px',
                        backgroundColor: '#ffffff',
                        border: '1px solid #e5e7eb',
                        boxShadow: '0 4px 16px rgba(15,23,42,0.08)',
                      }}
                    >
                      <CardHeader
                        sx={{
                          pb: 1,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                        }}
                        title={
                          <Box
                            sx={{
                              display: 'flex',
                              alignItems: 'center',
                              gap: 1,
                              cursor: 'pointer',
                            }}
                            onClick={() => toggleExpand(order.orderId)}
                          >
                            <IconButton size="small">
                              {isExpanded ? <ExpandLess /> : <ExpandMore />}
                            </IconButton>
                            <Typography
                              variant="subtitle1"
                              sx={{ fontWeight: 600, color: '#111827' }}
                            >
                              {order.orderId}
                            </Typography>
                          </Box>
                        }
                        subheader={
                          <Typography variant="caption" sx={{ color: '#6b7280' }}>
                            {createdAt}
                          </Typography>
                        }
                        action={
                          <Chip
                            label={order.status}
                            size="small"
                            sx={{
                              borderRadius: '999px',
                              fontSize: 11,
                              bgcolor:
                                order.status === 'INVOICED'
                                  ? 'rgba(22,163,74,0.18)'
                                  : order.status === 'CANCELLED'
                                    ? 'rgba(239,68,68,0.18)'
                                    : order.status === 'UNFULFILLABLE'
                                      ? 'rgba(234,179,8,0.18)'
                                      : 'rgba(59,130,246,0.18)', // PLACED
                              color:
                                order.status === 'INVOICED'
                                  ? '#16a34a'  // Darker green for better readability
                                  : order.status === 'CANCELLED'
                                    ? '#dc2626'  // Darker red
                                    : order.status === 'UNFULFILLABLE'
                                      ? '#ca8a04'  // Darker yellow
                                      : '#2563eb', // Darker blue for PLACED
                            }}
                          />
                        }
                      />
                      <CardContent sx={{ pt: 1, pb: 1.75 }}>
                        <Box
                          sx={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            mb: 0.75,
                          }}
                        >
                          <Typography variant="caption" sx={{ color: '#6b7280' }}>
                            Items
                          </Typography>
                          <Typography
                            variant="body2"
                            sx={{ fontWeight: 600, color: '#111827' }}
                          >
                            {order.totalItems || 0}
                          </Typography>
                        </Box>
                        <Box
                          sx={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            mb: 0.5,
                          }}
                        >
                          <Typography variant="caption" sx={{ color: '#9ca3af' }}>
                            Total
                          </Typography>
                          <Typography
                            variant="body2"
                            sx={{ fontWeight: 600, color: '#111827' }}
                          >
                            {formatINR(order.totalAmount || 0)}
                          </Typography>
                        </Box>
                      </CardContent>
                      <CardActions
                        sx={{
                          px: 1.75,
                          pb: 1.25,
                          pt: 0,
                          display: 'flex',
                          justifyContent: 'flex-end',
                          gap: 1,
                        }}
                      >
                        {order.status === 'PLACED' && (
                          <>
                            <Tooltip title="Edit Order">
                              <span>
                                <IconButton
                                  size="small"
                                  color="primary"
                                  onClick={() => handleEditOrder(order.orderId)}
                                  sx={{
                                    border: '1px solid',
                                    borderColor: 'primary.main',
                                  }}
                                >
                                  <EditIcon fontSize="small" />
                                </IconButton>
                              </span>
                            </Tooltip>
                            <Tooltip title="Generate Invoice">
                              <span>
                                <IconButton
                                  size="small"
                                  color="primary"
                                  onClick={() => handleGenerateInvoice(order.orderId)}
                                  disabled={generatingInvoice === order.orderId}
                                  sx={{
                                    border: '1px solid',
                                    borderColor: 'primary.main',
                                  }}
                                >
                                  {generatingInvoice === order.orderId ? (
                                    <CircularProgress size={16} />
                                  ) : (
                                    <Receipt fontSize="small" />
                                  )}
                                </IconButton>
                              </span>
                            </Tooltip>
                            <Tooltip title="Cancel Order">
                              <span>
                                <IconButton
                                  size="small"
                                  color="error"
                                  onClick={() => handleCancelOrder(order.orderId)}
                                  disabled={generatingInvoice === order.orderId}
                                  sx={{
                                    border: '1px solid',
                                    borderColor: 'error.main',
                                  }}
                                >
                                  <Cancel fontSize="small" />
                                </IconButton>
                              </span>
                            </Tooltip>
                          </>
                        )}
                        {order.status === 'INVOICED' && order.hasInvoice && (
                          <Tooltip title="Download Invoice">
                            <IconButton
                              size="small"
                              color="primary"
                              onClick={() => handleDownloadInvoice(order.orderId)}
                              sx={{
                                bgcolor: 'primary.main',
                                color: 'white',
                                '&:hover': {
                                  bgcolor: 'primary.dark',
                                },
                              }}
                            >
                              <Download fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                        {order.status === 'UNFULFILLABLE' && (
                          <Tooltip title="Retry Order">
                            <IconButton
                              size="small"
                              sx={{
                                border: '1px solid',
                                borderColor: '#eab308',
                                color: '#eab308',
                                '&:hover': {
                                  bgcolor: 'rgba(234,179,8,0.08)',
                                },
                              }}
                              onClick={() => handleRetryOrder(order.orderId)}
                            >
                              <RetryIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                      </CardActions>
                      <Collapse in={isExpanded} timeout="auto" unmountOnExit>
                        <Box sx={{ px: 2, pb: 2 }}>
                          <OrderItemsTable items={order.items || []} />
                        </Box>
                      </Collapse>
                    </Card>
                  </Grid>
                );
              })}
            </Grid>

            {totalPages > 1 && (
              <PaginationBox>
                <Tooltip title="First Page">
                  <span>
                    <StyledIconButton
                      disabled={currentPage === 0 || loading}
                      onClick={() => setCurrentPage(0)}
                    >
                      <FirstPage />
                    </StyledIconButton>
                  </span>
                </Tooltip>

                <Tooltip title="Previous Page">
                  <span>
                    <StyledIconButton
                      disabled={currentPage === 0 || loading}
                      onClick={() => setCurrentPage((p) => p - 1)}
                    >
                      <ChevronLeft />
                    </StyledIconButton>
                  </span>
                </Tooltip>

                <Pagination
                  count={totalPages}
                  page={currentPage + 1}
                  onChange={(_, v) => setCurrentPage(v - 1)}
                  disabled={loading}
                  hidePrevButton
                  hideNextButton
                />

                <Tooltip title="Next Page">
                  <span>
                    <StyledIconButton
                      disabled={currentPage >= totalPages - 1 || loading}
                      onClick={() => setCurrentPage((p) => p + 1)}
                    >
                      <ChevronRight />
                    </StyledIconButton>
                  </span>
                </Tooltip>

                <Tooltip title="Last Page">
                  <span>
                    <StyledIconButton
                      disabled={currentPage >= totalPages - 1 || loading}
                      onClick={() => setCurrentPage(totalPages - 1)}
                    >
                      <LastPage />
                    </StyledIconButton>
                  </span>
                </Tooltip>
              </PaginationBox>
            )}
          </>
        )}
      </SectionCard>

      {/* CREATE ORDER DIALOG */}
      <Dialog
        open={createDialogOpen}
        onClose={(event, reason) => {
          if (reason === 'backdropClick') return;
          setCreateDialogOpen(false);
          setOrderForm({
            lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
          });
          setSelectedProducts(new Map()); // Clear cached products
        }}
        fullWidth
        maxWidth="lg"
      >
        <DialogTitle>Create New Order</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 3 }}>


            {/* PRODUCT LINES */}
            <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
              Order Items
            </Typography>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Product</TableCell>
                    <TableCell width={120}>Quantity</TableCell>
                    <TableCell width={120}>MRP</TableCell>
                    <TableCell width={120}>Selling Price</TableCell>
                    <TableCell width={120} align="right">Line Total</TableCell>
                    <TableCell width={100} align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {orderForm.lines.map((line, index) => {
                    const selectedProduct = selectedProducts.get(index) ||
                      products.find((p) => p.id === line.productId) ||
                      null;
                    return (
                      <TableRow key={index}>
                        <TableCell>
                          <Autocomplete
                            options={products}
                            loading={loadingProducts}
                            value={selectedProduct}
                            onChange={(event, newValue) => {
                              handleProductSelect(index, newValue);
                            }}
                            onInputChange={(event, newInputValue) => {
                              setProductSearchQuery(newInputValue);
                            }}
                            getOptionLabel={(option) =>
                              `${option.name} (${option.barcode})`
                            }
                            renderInput={(params) => (
                              <TextField
                                {...params}
                                size="small"
                                placeholder="Type to search products..."
                                InputProps={{
                                  ...params.InputProps,
                                  endAdornment: (
                                    <>
                                      {loadingProducts ? (
                                        <CircularProgress color="inherit" size={16} />
                                      ) : null}
                                      {params.InputProps.endAdornment}
                                    </>
                                  ),
                                }}
                              />
                            )}
                            filterOptions={(options) => options}
                            isOptionEqualToValue={(option, value) => option.id === value.id}
                            noOptionsText={productSearchQuery ? "No products found" : "Type to search..."}
                          />
                        </TableCell>
                        <TableCell>
                          <TextField
                            size="small"
                            type="number"
                            fullWidth
                            inputProps={{ min: 1 }}
                            value={line.quantity}
                            onChange={(e) =>
                              handleQuantityChange(
                                index,
                                Number(e.target.value) || 1
                              )
                            }
                          />
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" sx={{ color: '#1976d2', fontWeight: 600 }}>
                            {selectedProduct ? formatINR(selectedProduct.mrp) : '-'}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <TextField
                            size="small"
                            type="number"
                            fullWidth
                            inputProps={{ min: 0, step: 0.01 }}
                            value={line.mrp}
                            onChange={(e) => {
                              const newPrice = Number(e.target.value) || 0;
                              const productMRP = selectedProduct?.mrp || 0;
                              if (newPrice > productMRP) {
                                toastError(`Selling price cannot exceed MRP (${formatINR(productMRP)})`);
                                return;
                              }
                              handleMrpChange(index, newPrice);
                            }}
                          />
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2" sx={{ fontWeight: 500 }}>
                            {formatINR(line.lineTotal)}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Button
                            color="error"
                            size="small"
                            disabled={orderForm.lines.length === 1}
                            onClick={() => handleRemoveLine(index)}
                          >
                            Remove
                          </Button>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>

            {/* TOTAL */}
            <Box
              sx={{
                display: 'flex',
                justifyContent: 'flex-end',
                pt: 2,
                borderTop: '2px solid #e0e0e0',
              }}
            >
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Total: ₹
                {orderForm.lines
                  .reduce((sum, line) => sum + line.lineTotal, 0)
                  .toFixed(2)}
              </Typography>
            </Box>

            <Box sx={{ display: 'flex', justifyContent: 'flex-start' }}>
              <Button variant="outlined" onClick={handleAddLine}>
                Add Product Line
              </Button>
            </Box>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              setCreateDialogOpen(false);
              setOrderForm({
                lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
              });
              setSelectedProducts(new Map()); // Clear cached products
            }}
          >
            Cancel
          </Button>
          <Button variant="contained" onClick={handleCreateOrder}>
            Create Order
          </Button>
        </DialogActions>
      </Dialog>

      {/* EDIT ORDER DIALOG */}
      <Dialog
        open={editDialogOpen}
        onClose={(event, reason) => {
          if (reason === 'backdropClick') return;
          setEditDialogOpen(false);
          setEditingOrderId(null);
          setOrderForm({
            lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
          });
          setSelectedProducts(new Map()); // Clear cached products
        }}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Edit Order {editingOrderId}</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            {orderForm.lines.map((line, index) => {
              const selectedProduct = selectedProducts.get(index) ||
                products.find((p) => p.id === line.productId) ||
                (line.productName && line.barcode ? {
                  id: line.productId,
                  name: line.productName,
                  barcode: line.barcode,
                  clientId: '',
                  mrp: line.mrp,
                } : null);

              return (
                <Box
                  key={index}
                  sx={{
                    display: 'flex',
                    gap: 2,
                    mb: 2,
                    alignItems: 'flex-start',
                  }}
                >
                  <Autocomplete
                    options={products}
                    getOptionLabel={(option) => `${option.name} (${option.barcode})`}
                    value={selectedProduct}
                    onChange={(_, newValue) => handleProductSelect(index, newValue)}
                    onInputChange={(event, newInputValue) => {
                      setProductSearchQuery(newInputValue);
                    }}
                    loading={loadingProducts}
                    sx={{ flex: 2 }}
                    isOptionEqualToValue={(option, value) => option.id === value.id}
                    filterOptions={(options) => options}
                    noOptionsText={productSearchQuery ? "No products found" : "Type to search..."}
                    renderInput={(params) => (
                      <TextField
                        {...params}
                        label="Product"
                        size="small"
                        placeholder="Type to search products..."
                        InputProps={{
                          ...params.InputProps,
                          endAdornment: (
                            <>
                              {loadingProducts ? (
                                <CircularProgress color="inherit" size={16} />
                              ) : null}
                              {params.InputProps.endAdornment}
                            </>
                          ),
                        }}
                      />
                    )}
                  />
                  <TextField
                    label="Quantity"
                    type="number"
                    size="small"
                    value={line.quantity}
                    onChange={(e) => handleQuantityChange(index, parseInt(e.target.value) || 1)}
                    sx={{ width: 100 }}
                  />
                  <TextField
                    label="Selling Price"
                    type="number"
                    size="small"
                    value={line.mrp}
                    onChange={(e) => {
                      const newPrice = parseFloat(e.target.value) || 0;
                      const productMRP = selectedProduct?.mrp || 0;

                      if (newPrice > productMRP) {
                        toastError(`Selling price cannot exceed MRP (${formatINR(productMRP)})`);
                        return;
                      }
                      handleMrpChange(index, newPrice);
                    }}
                    sx={{ width: 120 }}
                    InputProps={{ startAdornment: '₹' }}
                  />
                  <TextField
                    label="Line Total"
                    size="small"
                    value={line.lineTotal.toFixed(2)}
                    InputProps={{ readOnly: true, startAdornment: '₹' }}
                    sx={{ width: 120 }}
                  />
                  <IconButton
                    onClick={() => handleRemoveLine(index)}
                    disabled={orderForm.lines.length === 1}
                    color="error"
                  >
                    <Cancel />
                  </IconButton>
                </Box>
              );
            })}

            <Box sx={{ display: 'flex', justifyContent: 'flex-start' }}>
              <Button variant="outlined" onClick={handleAddLine}>
                Add Product Line
              </Button>
            </Box>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              setEditDialogOpen(false);
              setEditingOrderId(null);
              setOrderForm({
                lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
              });
              setSelectedProducts(new Map()); // Clear cached products
            }}
          >
            Cancel
          </Button>
          <Button variant="contained" onClick={handleUpdateOrder}>
            Update Order
          </Button>
        </DialogActions>
      </Dialog>

      {/* RETRY ORDER DIALOG */}
      <Dialog
        open={retryDialogOpen}
        onClose={(event, reason) => {
          if (reason === 'backdropClick') return;
          setRetryDialogOpen(false);
          setRetryingOrderId(null);
          setOrderForm({
            lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
          });
          setSelectedProducts(new Map()); // Clear cached products
        }}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Retry Order {retryingOrderId}</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            {orderForm.lines.map((line, index) => {
              const selectedProduct = selectedProducts.get(index) ||
                products.find((p) => p.id === line.productId) ||
                null;

              return (
                <Box
                  key={index}
                  sx={{
                    display: 'flex',
                    gap: 2,
                    mb: 2,
                    alignItems: 'flex-start',
                  }}
                >
                  <Autocomplete
                    options={products}
                    getOptionLabel={(option) =>
                      `${option.name} (${option.barcode})`
                    }
                    value={selectedProduct}
                    onChange={(_, newValue) =>
                      handleProductSelect(index, newValue)
                    }
                    onInputChange={(event, newInputValue) => {
                      setProductSearchQuery(newInputValue);
                    }}
                    filterOptions={(options) => options}
                    noOptionsText={productSearchQuery ? "No products found" : "Type to search..."}
                    renderInput={(params) => (
                      <TextField {...params} label="Product" placeholder="Type to search products..." required />
                    )}
                    sx={{ flex: 2 }}
                    loading={loadingProducts}
                  />
                  <TextField
                    label="Quantity"
                    type="number"
                    value={line.quantity}
                    onChange={(e) =>
                      handleQuantityChange(index, parseInt(e.target.value) || 1)
                    }
                    required
                    sx={{ flex: 1 }}
                  />
                  <TextField
                    label="Selling Price"
                    type="number"
                    value={line.mrp}
                    onChange={(e) => {
                      const newPrice = parseFloat(e.target.value) || 0;
                      const productMRP = selectedProduct?.mrp || 0;

                      if (newPrice > productMRP) {
                        toastError(`Selling price cannot exceed MRP (${formatINR(productMRP)})`);
                        return;
                      }
                      handleMrpChange(index, newPrice);
                    }}
                    required
                    sx={{ flex: 1 }}
                  />
                  <TextField
                    label="Line Total"
                    value={line.lineTotal.toFixed(2)}
                    disabled
                    sx={{ flex: 1 }}
                  />
                  <IconButton
                    color="error"
                    onClick={() => handleRemoveLine(index)}
                    disabled={orderForm.lines.length === 1}
                  >
                    <Cancel />
                  </IconButton>
                </Box>
              );
            })}
            <Button
              variant="outlined"
              startIcon={<Add />}
              onClick={handleAddLine}
              sx={{ mt: 1 }}
            >
              Add Line
            </Button>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              setRetryDialogOpen(false);
              setRetryingOrderId(null);
              setOrderForm({
                lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
              });
              setSelectedProducts(new Map()); // Clear cached products
            }}
          >
            Cancel
          </Button>
          <Button variant="contained" onClick={handleRetrySubmit}>
            Retry Order
          </Button>
        </DialogActions>
      </Dialog>

      {/* VIEW ORDER DETAILS DIALOG */}
      <Dialog
        open={viewDialogOpen}
        onClose={(event, reason) => {
          if (reason === 'backdropClick') return;
          setViewDialogOpen(false);
          setViewOrderData(null);
        }}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Order Details - {viewOrderData?.orderId}</DialogTitle>
        <DialogContent>
          {viewOrderData && (
            <Box sx={{ mt: 2 }}>
              {/* Order Summary */}
              <Box sx={{ mb: 3 }}>
                <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                  Order Summary
                </Typography>
                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      Order ID
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>
                      {viewOrderData.orderId}
                    </Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      Status
                    </Typography>
                    <Box>
                      <Chip
                        label={viewOrderData.status}
                        size="small"
                        sx={{
                          mt: 0.5,
                          bgcolor:
                            viewOrderData.status === 'INVOICED'
                              ? 'rgba(22,163,74,0.18)'
                              : viewOrderData.status === 'CANCELLED'
                                ? 'rgba(239,68,68,0.18)'
                                : viewOrderData.status === 'UNFULFILLABLE'
                                  ? 'rgba(234,179,8,0.18)'
                                  : 'rgba(59,130,246,0.18)', // PLACED,
                          color:
                            viewOrderData.status === 'INVOICED'
                              ? '#16a34a'  // Darker green for better readability
                              : viewOrderData.status === 'CANCELLED'
                                ? '#dc2626'  // Darker red
                                : viewOrderData.status === 'UNFULFILLABLE'
                                  ? '#ca8a04'  // Darker yellow
                                  : '#2563eb', // Darker blue for PLACED
                        }}
                      />
                    </Box>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      Order Date
                    </Typography>
                    <Typography variant="body1">
                      {viewOrderData.createdAt
                        ? formatDateTimeText(new Date(viewOrderData.createdAt))
                        : '-'}
                    </Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="caption" color="text.secondary">
                      Total Items
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>
                      {viewOrderData.totalItems || 0}
                    </Typography>
                  </Grid>
                  <Grid item xs={12}>
                    <Typography variant="caption" color="text.secondary">
                      Total Amount
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
                      {formatINR(viewOrderData.totalAmount || 0)}
                    </Typography>
                  </Grid>
                </Grid>
              </Box>

              {/* Order Items */}
              <Box>
                <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                  Order Items
                </Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 600 }}>Product</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Barcode</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 600 }}>Quantity</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 600 }}>MRP</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 600 }}>Line Total</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {(viewOrderData.items || []).map((item, index) => (
                        <TableRow key={index}>
                          <TableCell>{item.productName || '-'}</TableCell>
                          <TableCell>{item.barcode || '-'}</TableCell>
                          <TableCell align="right">{item.quantity}</TableCell>
                          <TableCell align="right" sx={{ fontWeight: 600, color: '#1976d2' }}>{formatINR(item.mrp)}</TableCell>
                          <TableCell align="right" sx={{ fontWeight: 600 }}>
                            {formatINR(item.lineTotal || 0)}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              setViewDialogOpen(false);
              setViewOrderData(null);
            }}
          >
            Close
          </Button>
        </DialogActions>
      </Dialog>

      {/* CANCEL CONFIRMATION DIALOG */}
      <Dialog
        open={cancelDialogOpen}
        onClose={(event, reason) => {
          if (reason === 'backdropClick') return;
          setCancelDialogOpen(false);
        }}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Cancel Order</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to cancel this order? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>
            No, Keep Order
          </Button>
          <Button
            variant="contained"
            color="error"
            onClick={confirmCancelOrder}
          >
            Yes, Cancel Order
          </Button>
        </DialogActions>
      </Dialog>
    </StyledContainer >
  );
}
