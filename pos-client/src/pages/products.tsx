import { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Pagination,
  IconButton,
  MenuItem,
  Avatar,
  CircularProgress,
  Autocomplete,
  Grid,
  Card,
  CardContent,
  CardHeader,
  CardActions,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import { ChevronLeft, ChevronRight, FirstPage, LastPage, Edit, CloudUpload, Add, Remove, Check, Close } from '@mui/icons-material';
import { Tooltip } from '@mui/material';
import { styled } from '@mui/material/styles';
import { productService } from '../services/product.service';
import { clientService } from '../services/client.service';
import { ProductData, ProductForm, ProductSearchFilter, InventoryForm } from '../types/product.types';
import { formatINR } from '../utils/formatNumber';
import { ClientData } from '../types/client.types';
import { useAuth } from '../contexts/AuthContext';
import { toastError, toastSuccess, toastWarning } from '../utils/toast';

const PAGE_SIZE = 12;

/* ================= STYLES ================= */

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

const ActionBox = styled(Box)({
  display: 'flex',
  gap: '1rem',
  alignItems: 'center',
});

const SearchBox = styled(Box)({
  display: 'flex',
  flexWrap: 'wrap',
  gap: '0.75rem',
  alignItems: 'center',
  padding: '1rem 1.25rem',
  marginBottom: '1.75rem',
  borderRadius: '14px',
  backgroundColor: '#ffffff',
  border: '1px solid #e5e7eb',
});

const PaginationBox = styled(Box)({
  display: 'flex',
  justifyContent: 'center',
  gap: '1rem',
  marginTop: '2rem',
});

const StyledIconButton = styled(IconButton)({
  backgroundColor: '#1d4ed8',
  color: 'white',
  '&:hover': {
    backgroundColor: '#1e40af',
  },
});

/* ================= COMPONENT ================= */

export default function Products() {
  const { isSupervisor } = useAuth();
  const [products, setProducts] = useState<ProductData[]>([]);
  const [clients, setClients] = useState<ClientData[]>([]);
  const [loadingClients, setLoadingClients] = useState(false);
  const [searchValue, setSearchValue] = useState('');
  const [searchFilter, setSearchFilter] = useState<ProductSearchFilter>('barcode');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);

  const [open, setOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form, setForm] = useState<ProductForm>({
    barcode: '',
    clientId: '',
    name: '',
    mrp: 0,
    imageUrl: '',
  });
  const [inventoryForm, setInventoryForm] = useState<InventoryForm>({
    productId: '',
    quantity: 0,
  });

  const [editingInventoryId, setEditingInventoryId] = useState<string | null>(null);
  const [tempInventoryValue, setTempInventoryValue] = useState<number>(0);

  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [uploadType, setUploadType] = useState<'products' | 'inventory' | null>(null);
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadResultTsv, setUploadResultTsv] = useState<string | null>(null);
  const [uploadFailures, setUploadFailures] = useState<
    { rowNumber: string; error: string; data: string }[]
  >([]);

  useEffect(() => {
    loadProducts(currentPage);
    loadClients();
  }, [currentPage]);

  /* ================= API ================= */

  const loadClients = async () => {
    setLoadingClients(true);
    try {
      let allClients: ClientData[] = [];
      let page = 0;
      const pageSize = 100;
      let hasMore = true;

      while (hasMore) {
        const res = await clientService.getAll(page, pageSize);
        const clientList = res.content || [];
        allClients = [...allClients, ...clientList];

        hasMore = res.totalPages > page + 1;
        page++;
      }

      setClients(allClients);
      if (allClients.length === 0) {
        toastWarning('No clients found. Please create clients first.');
      }
    } catch (e: any) {
      console.error('Failed to load clients:', e);
      toastError('Failed to load clients: ' + (e.response?.data?.message || e.message));
      setClients([]);
    } finally {
      setLoadingClients(false);
    }
  };

  const loadProducts = async (page: number) => {
    setLoading(true);
    try {
      const res = await productService.getAll(page, PAGE_SIZE);
      setProducts(res.content || []);
      setTotalPages(res.totalPages || 0);
      setTotalElements(res.totalElements || 0);
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Failed to load products');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchValue.trim()) {
      loadProducts(0);
      setCurrentPage(0);
      return;
    }

    if (searchFilter === 'barcode') {
      try {
        const product = await productService.getByBarcode(searchValue.trim());
        setProducts([product]);
        setTotalPages(1);
        setTotalElements(1);
        setCurrentPage(0);
      } catch {
        toastError('Product not found');
        setProducts([]);
        setTotalPages(0);
        setTotalElements(0);
      }
      return;
    }

    setLoading(true);
    try {
      let allProducts: ProductData[] = [];
      let page = 0;
      const pageSize = 100; // Use larger page size for fetching all
      let hasMore = true;

      while (hasMore) {
        const res = await productService.getAll(page, pageSize);
        const productList = res.content || [];
        allProducts = [...allProducts, ...productList];

        hasMore = res.totalPages > page + 1;
        page++;
      }

      const value = searchValue.toLowerCase();
      const filtered = allProducts.filter((p) => {
        switch (searchFilter) {
          case 'name':
            return p.name.toLowerCase().includes(value);
          case 'clientId':
            return p.clientId.toLowerCase().includes(value);
          case 'clientName':
            return (p.clientName || '').toLowerCase().includes(value);
          default:
            return true;
        }
      });

      setProducts(filtered);
      setTotalPages(1);
      setTotalElements(filtered.length);
      setCurrentPage(0);
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Search failed');
      setProducts([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    try {
      if (editingId) {
        await productService.update(editingId, form);
        toastSuccess('Product updated');
      } else {
        await productService.create(form);
        toastSuccess('Product created');
      }
      setOpen(false);
      setEditingId(null);
      setForm({ barcode: '', clientId: '', name: '', mrp: 0, imageUrl: '' });
      loadProducts(currentPage);
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Operation failed');
    }
  };

  const handleInventorySubmit = async () => {
    try {
      await productService.updateInventory(inventoryForm.productId, inventoryForm);
      toastSuccess('Inventory updated');
      setOpen(false);
      setInventoryForm({ productId: '', quantity: 0 });
      loadProducts(currentPage);
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Failed to update inventory');
    }
  };

  const downloadTsvFile = (content: string, filename: string) => {
    const decodedContent = atob(content);
    const blob = new Blob([decodedContent], { type: 'text/tab-separated-values' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  };

  const tsvHasErrors = (base64Tsv: string): boolean => {
    const decoded = atob(base64Tsv);
    const lines = decoded.split('\n');

    for (let i = 1; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue; // skip empty lines

      const columns = line.split('\t');
      if (columns.length > 1) {
        const status = columns[1].trim();
        if (status === 'FAILED' || status === 'SKIPPED') {
          return true;
        }
      }
    }
    return false;
  };

  const extractFailures = (
    base64Tsv: string
  ): { rowNumber: string; error: string; data: string }[] => {
    const decoded = atob(base64Tsv);
    const lines = decoded.split('\n');
    const failures: { rowNumber: string; error: string; data: string }[] = [];

    if (lines.length < 2) return failures;

    const header = lines[0].split('\t');
    const rowNumberIndex = header.findIndex(h => h.trim().toLowerCase().includes('row'));
    const statusIndex = header.findIndex(h => h.trim().toLowerCase() === 'status');
    const errorIndex = header.findIndex(h => h.trim().toLowerCase().includes('error'));
    const dataIndex = header.findIndex(h => h.trim().toLowerCase().includes('data'));

    for (let i = 1; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;

      const columns = line.split('\t');
      const status = statusIndex >= 0 ? columns[statusIndex]?.trim() : '';

      if (status === 'FAILED' || status === 'SKIPPED') {
        const rowNum = rowNumberIndex >= 0 ? columns[rowNumberIndex]?.trim() : String(i);
        const error = errorIndex >= 0 ? columns[errorIndex]?.trim() : '';
        const data = dataIndex >= 0 ? columns[dataIndex]?.trim() : '';

        failures.push({
          rowNumber: rowNum,
          error: error || (status === 'SKIPPED' ? 'Product already exists' : 'Unknown error'),
          data: data || 'N/A',
        });
      }
    }
    return failures;
  };

  const downloadTemplateTsv = (type: 'products' | 'inventory') => {
    let content = '';
    let filename = '';

    if (type === 'products') {
      content =
        'barcode\tclientId\tname\tmrp\timageUrl\n' +
        'ABC-123\tclient-001\tSample Product\t199.99\thttps://example.com/image.png\n';
      filename = 'products-template.tsv';
    } else {
      content = 'barcode\tquantity\nABC-123\t10\n';
      filename = 'inventory-template.tsv';
    }

    const blob = new Blob([content], { type: 'text/tab-separated-values' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  };

  const handleUploadConfirm = () => {
    if (!uploadType || !uploadFile) {
      toastError('Please select a TSV file to upload');
      return;
    }

    if (uploadFile.size > 5 * 1024 * 1024) {
      toastError('File size must be less than 5MB');
      return;
    }

    setUploading(true);
    const reader = new FileReader();
    reader.onload = async (e) => {
      try {
        const text = e.target?.result as string;

        const lines = text.split('\n');
        if (lines.length === 0) {
          toastError('File is empty');
          setUploading(false);
          return;
        }

        const headerLine = lines[0].trim();
        const headers = headerLine.split('\t').map(h => h.trim().toLowerCase());

        if (uploadType === 'products') {
          const requiredHeaders = ['barcode', 'clientid', 'name', 'mrp'];
          const missingHeaders = requiredHeaders.filter(h => !headers.includes(h));

          if (missingHeaders.length > 0) {
            toastError(`Missing required headers: ${missingHeaders.join(', ')}`);
            setUploading(false);
            return;
          }

          const clientIdIndex = headers.indexOf('clientid');
          for (let i = 1; i < lines.length; i++) {
            const line = lines[i].trim();
            if (!line) continue;

            const columns = line.split('\t');
            const clientId = columns[clientIdIndex]?.trim();

            if (!clientId || clientId === '') {
              toastError(`Row ${i + 1}: Client ID cannot be blank. Please provide a valid client ID for all products.`);
              setUploading(false);
              return;
            }
          }
        } else if (uploadType === 'inventory') {
          const requiredHeaders = ['barcode', 'quantity'];
          const missingHeaders = requiredHeaders.filter(h => !headers.includes(h));

          if (missingHeaders.length > 0) {
            toastError(`Missing required headers: ${missingHeaders.join(', ')}`);
            setUploading(false);
            return;
          }
        }

        const base64 = btoa(unescape(encodeURIComponent(text)));

        let resultTsv: string;
        if (uploadType === 'inventory') {
          resultTsv = await productService.uploadInventoryTsv(base64);
        } else {
          resultTsv = await productService.uploadProductsTsv(base64);
        }

        setUploadResultTsv(resultTsv);
        const hasErrors = tsvHasErrors(resultTsv);
        const failures = extractFailures(resultTsv);
        setUploadFailures(failures);

        if (hasErrors) {
          toastError('Upload completed with some errors.');
        } else {
          toastSuccess(
            uploadType === 'inventory'
              ? 'Inventory uploaded successfully.'
              : 'Products uploaded successfully.'
          );
          setUploadDialogOpen(false);
        }

        setCurrentPage(0);
        await loadProducts(0);
      } catch (err: any) {
        toastError(err?.response?.data?.message || 'Upload failed');
      } finally {
        setUploading(false);
      }
    };
    reader.readAsText(uploadFile);
  };

  const handleOpenUploadDialog = (type: 'products' | 'inventory') => {
    setUploadType(type);
    setUploadFile(null);
    setUploadResultTsv(null);
    setUploadFailures([]);
    setUploadDialogOpen(true);
  };

  const handleCloseUploadDialog = () => {
    if (uploading) return;
    setUploadDialogOpen(false);
  };

  const handleEdit = (product: ProductData) => {
    setEditingId(product.id);
    setForm({
      barcode: product.barcode,
      clientId: product.clientId,
      name: product.name,
      mrp: product.mrp,
      imageUrl: product.imageUrl || '',
    });
    setInventoryForm({
      productId: product.id,
      quantity: product.quantity || 0,
    });
    loadClients(); // Reload clients when opening dialog
    setOpen(true);
  };

  /* ================= INLINE INVENTORY HANDLERS ================= */

  const handleStartInventoryEdit = (product: ProductData) => {
    setEditingInventoryId(product.id);
    setTempInventoryValue(product.quantity || 0);
  };

  const handleInventoryIncrement = () => {
    setTempInventoryValue((prev) => {
      if (prev >= 5000) {
        toastError('Inventory cannot exceed 5000');
        return prev;
      }
      return prev + 1;
    });
  };

  const handleInventoryDecrement = () => {
    setTempInventoryValue((prev) => Math.max(0, prev - 1));
  };

  const handleInventoryManualChange = (value: string) => {
    const numValue = parseInt(value) || 0;
    if (numValue > 5000) {
      toastError('Inventory cannot exceed 5000');
      setTempInventoryValue(5000);
      return;
    }
    setTempInventoryValue(Math.max(0, numValue));
  };

  const handleInventorySave = async (productId: string) => {
    try {
      await productService.updateInventory(productId, {
        productId,
        quantity: tempInventoryValue,
      });
      toastSuccess('Inventory updated');
      setEditingInventoryId(null);
      loadProducts(currentPage);
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Failed to update inventory');
    }
  };

  const handleInventoryCancel = () => {
    setEditingInventoryId(null);
    setTempInventoryValue(0);
  };

  /* ================= RENDER ================= */

  return (
    <StyledContainer maxWidth="lg">
      {/* HEADER */}
      <HeaderBox>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5, color: '#111827' }}>
            Products
          </Typography>
          <Typography variant="body2" sx={{ color: '#6b7280' }}>
            Manage product catalog and inventory.
          </Typography>
        </Box>

        <ActionBox>
          {isSupervisor && (
            <>
              <Button
                variant="outlined"
                startIcon={<CloudUpload />}
                disabled={loading}
                sx={{
                  borderRadius: '999px',
                  borderColor: '#10b981',
                  color: '#10b981',
                  '&:hover': {
                    borderColor: '#059669',
                    backgroundColor: 'rgba(16, 185, 129, 0.04)',
                  },
                }}
                onClick={() => handleOpenUploadDialog('products')}
              >
                Products TSV
              </Button>

              <Button
                variant="outlined"
                startIcon={<CloudUpload />}
                disabled={loading}
                sx={{
                  borderRadius: '999px',
                  borderColor: '#f59e0b',
                  color: '#f59e0b',
                  '&:hover': {
                    borderColor: '#d97706',
                    backgroundColor: 'rgba(245, 158, 11, 0.04)',
                  },
                }}
                onClick={() => handleOpenUploadDialog('inventory')}
              >
                Inventory TSV
              </Button>
            </>
          )}

          {isSupervisor && (
            <Button
              variant="contained"
              startIcon={<Add />}
              onClick={() => {
                setEditingId(null);
                setForm({ barcode: '', clientId: '', name: '', mrp: 0, imageUrl: '' });
                setInventoryForm({ productId: '', quantity: 0 });
                loadClients();
                setOpen(true);
              }}
              sx={{ borderRadius: '999px', px: 3, py: 1 }}
            >
              Add Product
            </Button>
          )}
        </ActionBox>
      </HeaderBox>

      {/* SEARCH BAR */}
      <SearchBox>
        <TextField
          select
          label="Filter By"
          size="small"
          value={searchFilter}
          onChange={(e) => setSearchFilter(e.target.value as ProductSearchFilter)}
          sx={{ width: 160 }}
        >
          <MenuItem value="barcode">Barcode</MenuItem>
          <MenuItem value="name">Product Name</MenuItem>
          <MenuItem value="clientId">Client ID</MenuItem>
          <MenuItem value="clientName">Client Name</MenuItem>
        </TextField>

        <TextField
          label="Search"
          size="small"
          value={searchValue}
          onChange={(e) => setSearchValue(e.target.value)}
          placeholder={
            searchFilter === 'barcode' ? 'Enter barcode' :
              searchFilter === 'name' ? 'Enter product name' :
                searchFilter === 'clientId' ? 'Enter client ID' :
                  'Enter client name'
          }
          sx={{ width: 260 }}
        />

        <Button
          variant="contained"
          onClick={handleSearch}
          disabled={loading}
          sx={{ borderRadius: '999px' }}
        >
          Search
        </Button>

        <Button
          variant="text"
          onClick={() => {
            setSearchValue('');
            setSearchFilter('barcode');
            loadProducts(0);
          }}
          disabled={!searchValue}
        >
          Clear
        </Button>
      </SearchBox>

      {/* PRODUCT CARDS */}
      {loading && (
        <Box display="flex" justifyContent="center" p={4}>
          <CircularProgress />
        </Box>
      )}

      {/* TSV UPLOAD DIALOG */}
      <Dialog
        open={uploadDialogOpen}
        onClose={(event, reason) => { if (reason === 'backdropClick') return; handleCloseUploadDialog(); }}
        fullWidth
        maxWidth="sm"
        PaperProps={{
          sx: {
            borderRadius: 3,
            boxShadow: '0 20px 60px rgba(0,0,0,0.15)',
          }
        }}
      >
        <DialogTitle sx={{
          pb: 1,
          pt: 3,
          px: 3,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#111827' }}>
            {uploadType === 'inventory'
              ? 'Bulk Upload Inventory'
              : uploadType === 'products'
                ? 'Bulk Upload Products'
                : 'Bulk Upload'}
          </Typography>
          <IconButton
            onClick={handleCloseUploadDialog}
            disabled={uploading}
            size="small"
            sx={{
              color: '#6b7280',
              '&:hover': { bgcolor: '#f3f4f6' }
            }}
          >
            <Close fontSize="small" />
          </IconButton>
        </DialogTitle>

        <DialogContent sx={{ px: 3, pb: 2 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>

            {/* Download Template Section */}
            <Box sx={{
              bgcolor: '#f9fafb',
              borderRadius: 2,
              p: 2,
              border: '1px solid #e5e7eb'
            }}>
              <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                <Box sx={{
                  bgcolor: 'white',
                  borderRadius: 1.5,
                  p: 1,
                  display: 'flex',
                  border: '1px solid #e5e7eb'
                }}>
                  <CloudUpload sx={{ fontSize: 20, color: '#6b7280' }} />
                </Box>
                <Box sx={{ flex: 1 }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#111827', mb: 0.5 }}>
                    Download template
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#6b7280', display: 'block', mb: 1.5 }}>
                    Use this TSV template to ensure correct formatting.
                  </Typography>
                  <Tooltip
                    title={
                      <TableContainer sx={{ maxWidth: 400 }}>
                        <Typography variant="caption" sx={{ fontWeight: 600, display: 'block', mb: 1, px: 1 }}>
                          Template Preview:
                        </Typography>
                        <Table size="small">
                          <TableHead>
                            <TableRow>
                              {uploadType === 'inventory' ? (
                                <>
                                  <TableCell sx={{ color: 'white', fontWeight: 600, borderBottom: '1px solid rgba(255,255,255,0.3)' }}>barcode</TableCell>
                                  <TableCell sx={{ color: 'white', fontWeight: 600, borderBottom: '1px solid rgba(255,255,255,0.3)' }}>quantity</TableCell>
                                </>
                              ) : (
                                <>
                                  <TableCell sx={{ color: 'white', fontWeight: 600, borderBottom: '1px solid rgba(255,255,255,0.3)' }}>barcode</TableCell>
                                  <TableCell sx={{ color: 'white', fontWeight: 600, borderBottom: '1px solid rgba(255,255,255,0.3)' }}>name</TableCell>
                                  <TableCell sx={{ color: 'white', fontWeight: 600, borderBottom: '1px solid rgba(255,255,255,0.3)' }}>mrp</TableCell>
                                  <TableCell sx={{ color: 'white', fontWeight: 600, borderBottom: '1px solid rgba(255,255,255,0.3)' }}>clientId</TableCell>
                                </>
                              )}
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {uploadType === 'inventory' ? (
                              <>
                                <TableRow>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>BC001</TableCell>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>100</TableCell>
                                </TableRow>
                                <TableRow>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>BC002</TableCell>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>50</TableCell>
                                </TableRow>
                              </>
                            ) : (
                              <>
                                <TableRow>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>BC001</TableCell>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>Product 1</TableCell>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>99.99</TableCell>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>CLIENT001</TableCell>
                                </TableRow>
                                <TableRow>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>BC002</TableCell>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>Product 2</TableCell>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>149.99</TableCell>
                                  <TableCell sx={{ color: 'rgba(255,255,255,0.9)', border: 'none' }}>CLIENT001</TableCell>
                                </TableRow>
                              </>
                            )}
                          </TableBody>
                        </Table>
                      </TableContainer>
                    }
                    placement="right"
                    arrow
                  >
                    <Button
                      variant="text"
                      size="small"
                      startIcon={<CloudUpload sx={{ fontSize: 16 }} />}
                      onClick={() =>
                        uploadType && downloadTemplateTsv(uploadType === 'inventory' ? 'inventory' : 'products')
                      }
                      sx={{
                        color: '#3b82f6',
                        fontWeight: 600,
                        fontSize: '0.8125rem',
                        textTransform: 'none',
                        px: 0,
                        '&:hover': {
                          bgcolor: 'transparent',
                          color: '#2563eb',
                        },
                      }}
                    >
                      Download TSV Template
                    </Button>
                  </Tooltip>
                </Box>
              </Box>
            </Box>

            {/* Choose TSV File Section */}
            <Box>
              <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#111827', mb: 1.5 }}>
                Choose TSV file
              </Typography>
              <Box
                sx={{
                  border: '2px solid',
                  borderColor: uploadFile ? '#3b82f6' : '#e5e7eb',
                  borderRadius: 2,
                  p: 2.5,
                  textAlign: 'center',
                  bgcolor: uploadFile ? '#eff6ff' : '#fafafa',
                  transition: 'all 0.2s',
                  cursor: 'pointer',
                  '&:hover': {
                    borderColor: '#3b82f6',
                    bgcolor: '#f5f9ff',
                  },
                }}
                onClick={() => document.getElementById('tsv-file-input')?.click()}
              >
                <input
                  type="file"
                  accept=".tsv,.txt"
                  id="tsv-file-input"
                  style={{ display: 'none' }}
                  onChange={(e) => {
                    const file = (e.target as HTMLInputElement).files?.[0] || null;
                    setUploadFile(file);
                  }}
                />
                {uploadFile ? (
                  <Box>
                    <Typography variant="body2" sx={{ fontWeight: 600, color: '#111827', mb: 0.5 }}>
                      {uploadFile.name}
                    </Typography>
                    <Typography variant="caption" sx={{ color: '#6b7280' }}>
                      {(uploadFile.size / 1024).toFixed(2)} KB • Click to change file
                    </Typography>
                  </Box>
                ) : (
                  <Box>
                    <CloudUpload sx={{ fontSize: 40, color: '#9ca3af', mb: 1 }} />
                    <Typography variant="body2" sx={{ fontWeight: 600, color: '#111827', mb: 0.5 }}>
                      Click to select TSV file
                    </Typography>
                    <Typography variant="caption" sx={{ color: '#6b7280' }}>
                      or drag and drop
                    </Typography>
                  </Box>
                )}
              </Box>
              <Typography variant="caption" sx={{ color: '#ef4444', display: 'block', mt: 1 }}>
                Only .tsv files are supported (Maximum Limit: 5000 rows per file)
              </Typography>
            </Box>

            {/* Failed Rows Section */}
            {uploadFailures.length > 0 && (
              <Box
                sx={{
                  p: 2,
                  borderRadius: 2,
                  border: '1px solid #fecaca',
                  bgcolor: '#fef2f2',
                  maxHeight: 220,
                  overflowY: 'auto',
                }}
              >
                <Typography
                  variant="subtitle2"
                  sx={{ fontWeight: 600, mb: 1.5, color: '#dc2626' }}
                >
                  Failed rows ({uploadFailures.length})
                </Typography>
                {uploadFailures.map((f) => (
                  <Box key={f.rowNumber + f.data} sx={{ mb: 1.5, pb: 1.5, borderBottom: '1px solid #fee2e2', '&:last-child': { borderBottom: 'none', mb: 0, pb: 0 } }}>
                    <Typography variant="caption" sx={{ color: '#991b1b', fontWeight: 600, display: 'block', mb: 0.5 }}>
                      Row {f.rowNumber}
                    </Typography>
                    <Typography variant="caption" sx={{ color: '#dc2626', display: 'block' }}>
                      {f.error}
                    </Typography>
                    {f.data && (
                      <Typography
                        variant="caption"
                        sx={{ display: 'block', color: '#b91c1c', fontFamily: 'monospace', mt: 0.5 }}
                      >
                        {f.data}
                      </Typography>
                    )}
                  </Box>
                ))}
              </Box>
            )}
          </Box>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 3, pt: 1, gap: 1.5 }}>
          {uploadResultTsv && (
            <Button
              onClick={() =>
                downloadTsvFile(
                  uploadResultTsv,
                  uploadType === 'inventory'
                    ? 'inventory-upload-results.tsv'
                    : 'products-upload-results.tsv'
                )
              }
              size="small"
              sx={{
                textTransform: 'none',
                color: '#6b7280',
                fontWeight: 600,
              }}
            >
              Download Results
            </Button>
          )}
          <Box sx={{ flex: 1 }} />
          <Button
            onClick={handleCloseUploadDialog}
            disabled={uploading}
            sx={{
              textTransform: 'none',
              color: '#6b7280',
              fontWeight: 600,
              px: 2.5,
              '&:hover': {
                bgcolor: '#f3f4f6',
              }
            }}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={handleUploadConfirm}
            disabled={uploading || !uploadFile || !uploadType}
            sx={{
              textTransform: 'none',
              bgcolor: '#111827',
              color: 'white',
              fontWeight: 600,
              px: 3,
              py: 1,
              borderRadius: 2,
              boxShadow: 'none',
              '&:hover': {
                bgcolor: '#1f2937',
                boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
              },
              '&:disabled': {
                bgcolor: '#e5e7eb',
                color: '#9ca3af',
              }
            }}
          >
            {uploading ? 'Uploading...' : 'Upload & Process'}
          </Button>
        </DialogActions>
      </Dialog>

      {!loading && (
        <Grid container spacing={3}>
          {products.map((p) => (
            <Grid item xs={12} sm={6} md={6} lg={4} key={p.id}>
              <Card
                sx={{
                  height: '100%',
                  minHeight: 260,
                  display: 'flex',
                  flexDirection: 'column',
                  borderRadius: '16px',
                  backgroundColor: '#ffffff',
                  border: '1px solid #e5e7eb',
                  boxShadow: '0 2px 8px rgba(15,23,42,0.04), 0 1px 2px rgba(15,23,42,0.06)',
                  transition: 'all 0.2s ease',
                  '&:hover': {
                    borderColor: '#cbd5e1',
                    boxShadow: '0 8px 16px rgba(15,23,42,0.08), 0 2px 4px rgba(15,23,42,0.06)',
                    transform: 'translateY(-2px)',
                  },
                }}
              >
                <CardHeader
                  sx={{
                    pb: 1.5,
                    pt: 2,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1.5,
                  }}
                  avatar={
                    <Avatar
                      src={p.imageUrl || undefined}
                      alt={p.name}
                      sx={{
                        width: 72,
                        height: 72,
                        borderRadius: 3,
                        bgcolor: '#f3f4f6',
                        fontSize: 13,
                        fontWeight: 500,
                        color: '#9ca3af',
                        border: '1px solid #e5e7eb',
                      }}
                      variant="rounded"
                    >
                      {p.imageUrl ? null : 'No Img'}
                    </Avatar>
                  }
                  title={
                    <Typography
                      variant="subtitle1"
                      sx={{
                        fontWeight: 600,
                        color: '#0f172a',
                        mb: 0.25,
                        fontSize: '0.95rem',
                      }}
                    >
                      {p.name}
                    </Typography>
                  }
                  subheader={
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                      <Typography
                        variant="caption"
                        sx={{ color: '#6b7280', fontFamily: 'monospace' }}
                      >
                        {p.barcode}
                      </Typography>
                      <Typography variant="caption" sx={{ color: '#4b5563' }}>
                        {p.clientName || p.clientId}
                      </Typography>
                    </Box>
                  }
                />
                <CardContent sx={{ pt: 1, pb: 2, px: 2.25, flexGrow: 1 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1.25 }}>
                    <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 500, fontSize: '0.75rem' }}>
                      MRP
                    </Typography>
                    <Typography
                      variant="body2"
                      sx={{ fontWeight: 700, color: '#0f172a', fontSize: '0.95rem' }}
                    >
                      {formatINR(p.mrp)}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 500, fontSize: '0.75rem' }}>
                      Inventory
                    </Typography>
                    {isSupervisor && editingInventoryId === p.id ? (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                        <IconButton
                          size="small"
                          onClick={handleInventoryDecrement}
                          sx={{
                            width: 24,
                            height: 24,
                            backgroundColor: '#f3f4f6',
                            '&:hover': { backgroundColor: '#e5e7eb' },
                          }}
                        >
                          <Remove sx={{ fontSize: 14 }} />
                        </IconButton>
                        <TextField
                          size="small"
                          type="number"
                          value={tempInventoryValue}
                          onChange={(e) => handleInventoryManualChange(e.target.value)}
                          sx={{
                            width: 60,
                            '& input': {
                              textAlign: 'center',
                              padding: '4px 6px',
                              fontSize: 13,
                            },
                          }}
                          inputProps={{ min: 0, max: 5000 }}
                        />
                        <IconButton
                          size="small"
                          onClick={handleInventoryIncrement}
                          sx={{
                            width: 24,
                            height: 24,
                            backgroundColor: '#f3f4f6',
                            '&:hover': { backgroundColor: '#e5e7eb' },
                          }}
                        >
                          <Add sx={{ fontSize: 14 }} />
                        </IconButton>
                        <IconButton
                          size="small"
                          onClick={() => handleInventorySave(p.id)}
                          sx={{
                            width: 24,
                            height: 24,
                            backgroundColor: '#22c55e',
                            color: 'white',
                            ml: 0.5,
                            '&:hover': { backgroundColor: '#16a34a' },
                          }}
                        >
                          <Check sx={{ fontSize: 14 }} />
                        </IconButton>
                        <IconButton
                          size="small"
                          onClick={handleInventoryCancel}
                          sx={{
                            width: 24,
                            height: 24,
                            backgroundColor: '#ef4444',
                            color: 'white',
                            '&:hover': { backgroundColor: '#dc2626' },
                          }}
                        >
                          <Close sx={{ fontSize: 14 }} />
                        </IconButton>
                      </Box>
                    ) : (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                        <Chip
                          size="small"
                          label={p.quantity ?? 0}
                          sx={{
                            height: 24,
                            borderRadius: '8px',
                            backgroundColor:
                              (p.quantity ?? 0) === 0
                                ? '#fef2f2'
                                : '#f0fdf4',
                            color:
                              (p.quantity ?? 0) === 0 ? '#dc2626' : '#16a34a',
                            fontSize: '0.8rem',
                            fontWeight: 600,
                            px: 1.25,
                            border: (p.quantity ?? 0) === 0 ? '1px solid #fecaca' : '1px solid #bbf7d0',
                          }}
                        />
                        {isSupervisor && (
                          <IconButton
                            size="small"
                            onClick={() => handleStartInventoryEdit(p)}
                            sx={{
                              width: 20,
                              height: 20,
                              '&:hover': { backgroundColor: '#f3f4f6' },
                            }}
                          >
                            <Edit sx={{ fontSize: 12, color: '#6b7280' }} />
                          </IconButton>
                        )}
                      </Box>
                    )}
                  </Box>
                </CardContent>
                <CardActions
                  sx={{
                    px: 2.25,
                    pb: 2,
                    pt: 1,
                    display: 'flex',
                    justifyContent: 'space-between',
                    borderTop: '1px solid #f1f5f9',
                  }}
                >
                  {isSupervisor ? (
                    <Button
                      size="small"
                      startIcon={<Edit sx={{ fontSize: 16 }} />}
                      onClick={() => handleEdit(p)}
                      sx={{
                        borderRadius: '999px',
                        textTransform: 'none',
                        px: 1.8,
                        py: 0.4,
                      }}
                    >
                      Edit
                    </Button>
                  ) : null}
                </CardActions>
              </Card>
            </Grid>
          ))}
          {!loading && products.length === 0 && (
            <Box
              sx={{
                width: '100%',
                py: 6,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                color: '#9ca3af',
              }}
            >
              <Typography variant="body1">
                No products found. Try adjusting your filters or add a new product.
              </Typography>
            </Box>
          )}
        </Grid>
      )}

      {/* PAGINATION */}
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

      {/* EDIT PRODUCT DIALOG */}
      <Dialog
        open={open}
        onClose={(event, reason) => {
          if (reason === 'backdropClick') return;
          setOpen(false);
        }}
        fullWidth
        maxWidth="sm"
        PaperProps={{
          sx: {
            borderRadius: '16px',
            boxShadow: '0 20px 40px rgba(15,23,42,0.15)',
          }
        }}
      >
        <DialogTitle sx={{
          pb: 1,
          pt: 3,
          px: 3,
          borderBottom: '1px solid #f1f5f9',
        }}>
          <Typography variant="h5" sx={{ fontWeight: 700, color: '#0f172a' }}>
            {editingId ? 'Edit Product Details' : 'Add New Product'}
          </Typography>
          <Typography variant="caption" sx={{ color: '#64748b', mt: 0.5, display: 'block' }}>
            {editingId ? 'Update product information below' : 'Fill in the product details to add to catalog'}
          </Typography>
        </DialogTitle>
        <DialogContent sx={{ pt: 3, pb: 2, px: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
            {/* Barcode */}
            <Box>
              <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 600, mb: 0.75, display: 'block', textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: '0.05em' }}>
                Barcode {!editingId && <span style={{ color: 'red' }}>*</span>}
              </Typography>
              <TextField
                fullWidth
                placeholder="Enter product barcode"
                value={form.barcode}
                onChange={(e) => setForm({ ...form, barcode: e.target.value })}
                disabled={!!editingId}
                required
                size="small"
                sx={{
                  '& .MuiOutlinedInput-root': {
                    borderRadius: '10px',
                    backgroundColor: editingId ? '#f8fafc' : '#ffffff',
                  }
                }}
              />
              {editingId && (
                <Typography variant="caption" sx={{ color: '#94a3b8', mt: 0.5, display: 'block', fontSize: '0.7rem' }}>
                  Barcode cannot be changed after creation
                </Typography>
              )}
            </Box>

            {/* Client */}
            <Box>
              <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 600 }}>
                Client / Brand <span style={{ color: 'red' }}>*</span>
              </Typography>
              <Autocomplete
                fullWidth
                options={clients}
                loading={loadingClients}
                value={clients.find(c => c.clientId === form.clientId) || null}
                onChange={(event, newValue) => {
                  setForm({ ...form, clientId: newValue ? newValue.clientId : '' });
                }}
                getOptionLabel={(option) => `${option.name} (${option.clientId})`}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    placeholder="Select client or brand"
                    required
                    size="small"
                    helperText={loadingClients ? 'Loading clients...' : clients.length === 0 ? 'No clients available' : 'Search by name or ID'}
                    InputProps={{
                      ...params.InputProps,
                      sx: { borderRadius: '10px' },
                      endAdornment: (
                        <>
                          {loadingClients ? <CircularProgress color="inherit" size={18} /> : null}
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
                      option.clientId.toLowerCase().includes(searchTerm)
                  );
                }}
                noOptionsText={loadingClients ? 'Loading...' : 'No clients found'}
              />
            </Box>

            {/* Product Name */}
            <Box>
              <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 600, mb: 0.75, display: 'block', textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: '0.05em' }}>
                Product Name <span style={{ color: 'red' }}>*</span>
              </Typography>
              <TextField
                fullWidth
                placeholder="Enter product name"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                required
                size="small"
                sx={{
                  '& .MuiOutlinedInput-root': {
                    borderRadius: '10px',
                  }
                }}
              />
            </Box>

            {/* MRP */}
            <Box>
              <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 600, mb: 0.75, display: 'block', textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: '0.05em' }}>
                MRP (₹) <span style={{ color: 'red' }}>*</span>
              </Typography>
              <TextField
                fullWidth
                placeholder="0.00"
                type="number"
                value={form.mrp}
                onChange={(e) => setForm({ ...form, mrp: parseFloat(e.target.value) || 0 })}
                required
                size="small"
                inputProps={{ min: 0, step: 0.01 }}
                sx={{
                  '& .MuiOutlinedInput-root': {
                    borderRadius: '10px',
                  }
                }}
              />
            </Box>

            {/* Image URL */}
            <Box>
              <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 600, mb: 0.75, display: 'block', textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: '0.05em' }}>
                Image URL (Optional)
              </Typography>
              <TextField
                fullWidth
                placeholder="https://example.com/image.jpg"
                value={form.imageUrl}
                onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
                size="small"
                sx={{
                  '& .MuiOutlinedInput-root': {
                    borderRadius: '10px',
                  }
                }}
              />
              <Typography variant="caption" sx={{ color: '#94a3b8', mt: 0.5, display: 'block', fontSize: '0.7rem' }}>
                Provide a URL to display product image
              </Typography>
            </Box>

          </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3, pt: 2, gap: 1.5, borderTop: '1px solid #f1f5f9' }}>
          <Button
            onClick={() => setOpen(false)}
            sx={{
              borderRadius: '10px',
              px: 3,
              textTransform: 'none',
              fontWeight: 600,
              color: '#64748b',
              '&:hover': {
                backgroundColor: '#f1f5f9',
              }
            }}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={handleSubmit}
            sx={{
              borderRadius: '10px',
              px: 4,
              textTransform: 'none',
              fontWeight: 600,
              boxShadow: '0 4px 12px rgba(59,130,246,0.25)',
              '&:hover': {
                boxShadow: '0 6px 16px rgba(59,130,246,0.35)',
              }
            }}
          >
            {editingId ? 'Save Changes' : 'Add Product'}
          </Button>
        </DialogActions>
      </Dialog>
    </StyledContainer>
  );
}
