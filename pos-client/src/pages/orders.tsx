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
} from '@mui/material';
import {
  Add,
  ChevronLeft,
  ChevronRight,
  Download,
  ExpandLess,
  ExpandMore,
  Receipt,
} from '@mui/icons-material';
import { styled } from '@mui/material/styles';
import { toast } from 'react-toastify';
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

  const [filters, setFilters] = useState<OrderSearchFilters>({});
  const [expandedOrderId, setExpandedOrderId] = useState<string | null>(null);
  const [generatingInvoice, setGeneratingInvoice] = useState<string | null>(null);

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

  useEffect(() => {
    if (createDialogOpen) {
      loadProducts();
    }
  }, [createDialogOpen]);

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
          toast.error(errorMsg);
        }
        setOrders([]);
        setTotalPages(0);
      }
    } finally {
      setLoading(false);
    }
  };

  const loadProducts = async () => {
    setLoadingProducts(true);
    try {
      let allProducts: ProductData[] = [];
      let page = 0;
      const pageSize = 100;
      let hasMore = true;

      while (hasMore) {
        const res = await productService.getAll(page, pageSize);
        const productList = res.content || [];
        allProducts = [...allProducts, ...productList];
        hasMore = res.totalPages > page + 1;
        page++;
      }

      setProducts(allProducts);
    } catch (e: any) {
      console.error('Failed to load products:', e);
      setProducts([]);
      // Don't show toast for products - it's expected if backend is not ready
    } finally {
      setLoadingProducts(false);
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
        toast.error('Add at least one product line');
        return;
      }
      for (const line of orderForm.lines) {
        if (!line.productId) {
          toast.error('Please select a product for all lines');
          return;
        }
        if (line.quantity <= 0) {
          toast.error('Quantity must be positive');
          return;
        }
        if (line.mrp < 0) {
          toast.error('MRP cannot be negative');
          return;
        }
      }

      await orderService.create(orderForm);
      toast.success('Order created successfully');
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
        toast.error('Order creation endpoint is not available yet. Backend needs to be implemented.');
      } else {
        toast.error(errorMsg);
      }
    }
  };

  const handleFilterChange = (patch: Partial<OrderSearchFilters>) => {
    setFilters((prev) => ({ ...prev, ...patch }));
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

  const toggleExpand = async (orderId: string) => {
    if (expandedOrderId === orderId) {
      setExpandedOrderId(null);
    } else {
      setExpandedOrderId(orderId);
      // Load order details if not already loaded
      const order = orders.find((o) => o.orderId === orderId);
      if (order && !order.items) {
        try {
          const orderDetails = await orderService.getById(orderId);
          setOrders((prev) =>
            prev.map((o) => (o.orderId === orderId ? orderDetails : o))
          );
        } catch (e: any) {
          const status = e.response?.status;
          if (status === 404 || status === 500 || !e.response) {
            // Silently handle - endpoint not available
            console.warn('Order details endpoint not available');
          } else {
            toast.error('Failed to load order details');
          }
        }
      }
    }
  };

  const handleGenerateInvoice = async (orderId: string) => {
    setGeneratingInvoice(orderId);
    try {
      await orderService.generateInvoice(orderId);
      toast.success('Invoice generated successfully');
      // Reload orders to get updated status
      loadOrders(currentPage, filters).catch((err) => {
        console.error('Error reloading orders:', err);
      });
    } catch (e: any) {
      const status = e.response?.status;
      const errorMsg = e.response?.data?.message || e.message || 'Failed to generate invoice';
      if (status === 404 || status === 500 || !e.response) {
        toast.error('Invoice generation endpoint is not available yet. Backend needs to be implemented.');
      } else {
        toast.error(errorMsg);
      }
    } finally {
      setGeneratingInvoice(null);
    }
  };

  const handleDownloadInvoice = async (orderId: string) => {
    try {
      await orderService.downloadInvoice(orderId);
      toast.success('Invoice downloaded');
    } catch (e: any) {
      const status = e.response?.status;
      const errorMsg = e.response?.data?.message || e.message || 'Failed to download invoice';
      if (status === 404 || status === 500 || !e.response) {
        toast.error('Invoice download endpoint is not available yet. Backend needs to be implemented.');
      } else {
        toast.error(errorMsg);
      }
    }
  };

  const handleCancelOrder = async (orderId: string) => {
    try {
      await orderService.cancel(orderId);
      toast.success('Order cancelled');
      // Reload orders to get updated status
      loadOrders(currentPage, filters).catch((err) => {
        console.error('Error reloading orders:', err);
      });
    } catch (e: any) {
      const status = e.response?.status;
      const errorMsg = e.response?.data?.message || e.message || 'Failed to cancel order';
      if (status === 404 || status === 500 || !e.response) {
        toast.error('Order cancel endpoint is not available yet.');
      } else {
        toast.error(errorMsg);
      }
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
          onClick={() => setCreateDialogOpen(true)}
          sx={{ borderRadius: '999px', px: 3, py: 1 }}
        >
          Create Order
        </Button>
      </HeaderBox>

      {/* FILTERS */}
      <SearchBox>
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
        >
          <MenuItem value="">All</MenuItem>
          <MenuItem value="CREATED">Created</MenuItem>
          <MenuItem value="INVOICED">Invoiced</MenuItem>
          <MenuItem value="CANCELLED">Cancelled</MenuItem>
        </TextField>
        <TextField
          label="Order ID"
          size="small"
          value={filters.orderId || ''}
          onChange={(e) => handleFilterChange({ orderId: e.target.value })}
        />
        <Button
          variant="contained"
          onClick={applyFilters}
          disabled={loading}
          sx={{ borderRadius: '999px' }}
        >
          Apply
        </Button>
        <Button variant="text" onClick={clearFilters} disabled={loading}>
          Clear
        </Button>
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
                ? 'No orders found. Create your first order to get started.'
                : 'No orders match the current filters.'}
            </Typography>
          </Box>
        )}
        {!loading && orders.length > 0 && (
          <>
            <Grid container spacing={3}>
              {orders.map((order) => {
                const isExpanded = expandedOrderId === order.orderId;
                const createdAt = order.createdAt
                  ? new Date(order.createdAt).toLocaleString()
                  : '-';
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
                                  : 'rgba(30,64,175,0.25)',
                              color:
                                order.status === 'INVOICED'
                                  ? '#4ade80'
                                  : order.status === 'CANCELLED'
                                  ? '#f97373'
                                  : '#93c5fd',
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
                            sx={{ fontWeight: 600, color: '#e5e7eb' }}
                          >
                            ₹{order.totalAmount?.toFixed(2) || '0.00'}
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
                        {order.status === 'CREATED' && (
                          <>
                            <Button
                              size="small"
                              variant="outlined"
                              startIcon={
                                generatingInvoice === order.orderId ? (
                                  <CircularProgress size={16} />
                                ) : (
                                  <Receipt />
                                )
                              }
                              onClick={() => handleGenerateInvoice(order.orderId)}
                              disabled={generatingInvoice === order.orderId}
                              sx={{
                                borderRadius: '999px',
                                textTransform: 'none',
                              }}
                            >
                              Invoice
                            </Button>
                            <Button
                              size="small"
                              variant="outlined"
                              color="error"
                              onClick={() => handleCancelOrder(order.orderId)}
                              disabled={generatingInvoice === order.orderId}
                              sx={{ borderRadius: '999px', textTransform: 'none' }}
                            >
                              Cancel
                            </Button>
                          </>
                        )}
                        {order.status === 'INVOICED' && order.hasInvoice && (
                          <Button
                            size="small"
                            variant="contained"
                            startIcon={<Download />}
                            onClick={() => handleDownloadInvoice(order.orderId)}
                            sx={{ borderRadius: '999px', textTransform: 'none' }}
                          >
                            Download
                          </Button>
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
                <StyledIconButton
                  disabled={currentPage === 0 || loading}
                  onClick={() => setCurrentPage((p) => p - 1)}
                >
                  <ChevronLeft />
                </StyledIconButton>

                <Pagination
                  count={totalPages}
                  page={currentPage + 1}
                  onChange={(_, v) => setCurrentPage(v - 1)}
                  disabled={loading}
                />

                <StyledIconButton
                  disabled={currentPage >= totalPages - 1 || loading}
                  onClick={() => setCurrentPage((p) => p + 1)}
                >
                  <ChevronRight />
                </StyledIconButton>
              </PaginationBox>
            )}
          </>
        )}
      </SectionCard>

      {/* CREATE ORDER DIALOG */}
      <Dialog
        open={createDialogOpen}
        onClose={() => {
          setCreateDialogOpen(false);
          setOrderForm({
            lines: [{ productId: '', quantity: 1, mrp: 0, lineTotal: 0 }],
          });
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
                    <TableCell width={120} align="right">Line Total</TableCell>
                    <TableCell width={100} align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {orderForm.lines.map((line, index) => {
                    const selectedProduct = products.find((p) => p.id === line.productId);
                    return (
                      <TableRow key={index}>
                        <TableCell>
                          <Autocomplete
                            options={products}
                            loading={loadingProducts}
                            value={selectedProduct || null}
                            onChange={(event, newValue) => {
                              handleProductSelect(index, newValue);
                            }}
                            getOptionLabel={(option) =>
                              `${option.name} (${option.barcode})`
                            }
                            renderInput={(params) => (
                              <TextField
                                {...params}
                                size="small"
                                placeholder="Select product"
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
                            filterOptions={(options, { inputValue }) => {
                              const searchTerm = inputValue.toLowerCase();
                              return options.filter(
                                (option) =>
                                  option.name.toLowerCase().includes(searchTerm) ||
                                  option.barcode.toLowerCase().includes(searchTerm)
                              );
                            }}
                            noOptionsText="No products found"
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
                          <TextField
                            size="small"
                            type="number"
                            fullWidth
                            inputProps={{ min: 0, step: 0.01 }}
                            value={line.mrp}
                            onChange={(e) =>
                              handleMrpChange(index, Number(e.target.value) || 0)
                            }
                          />
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2" sx={{ fontWeight: 500 }}>
                            ₹{line.lineTotal.toFixed(2)}
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
            }}
          >
            Cancel
          </Button>
          <Button variant="contained" onClick={handleCreateOrder}>
            Create Order
          </Button>
        </DialogActions>
      </Dialog>
    </StyledContainer>
  );
}
