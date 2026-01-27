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
} from '@mui/material';
import { ChevronLeft, ChevronRight, Edit, CloudUpload, Add } from '@mui/icons-material';
import { styled } from '@mui/material/styles';
import { productService } from '../services/product.service';
import { clientService } from '../services/client.service';
import { ProductData, ProductForm, ProductSearchFilter, InventoryForm } from '../types/product.types';
import { ClientData } from '../types/client.types';
import { useAuth } from '../contexts/AuthContext';
import { toast } from 'react-toastify';

const PAGE_SIZE = 10;

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

  // TSV upload dialog state
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
      // Load clients in batches (max 100 per page)
      let allClients: ClientData[] = [];
      let page = 0;
      const pageSize = 100;
      let hasMore = true;

      while (hasMore) {
        const res = await clientService.getAll(page, pageSize);
        const clientList = res.content || [];
        allClients = [...allClients, ...clientList];
        
        // Check if there are more pages
        hasMore = res.totalPages > page + 1;
        page++;
      }

      setClients(allClients);
      if (allClients.length === 0) {
        toast.warning('No clients found. Please create clients first.');
      }
    } catch (e: any) {
      console.error('Failed to load clients:', e);
      toast.error('Failed to load clients: ' + (e.response?.data?.message || e.message));
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
      toast.error(e.response?.data?.message || 'Failed to load products');
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

    // Only barcode search hits backend
    if (searchFilter === 'barcode') {
      try {
        const product = await productService.getByBarcode(searchValue.trim());
        setProducts([product]);
        setTotalPages(1);
        setTotalElements(1);
        setCurrentPage(0);
      } catch {
        toast.error('Product not found');
        setProducts([]);
        setTotalPages(0);
        setTotalElements(0);
      }
      return;
    }

    // For other fields, load all pages and filter
    setLoading(true);
    try {
      let allProducts: ProductData[] = [];
      let page = 0;
      const pageSize = 100; // Use larger page size for fetching all
      let hasMore = true;

      // Fetch all pages
      while (hasMore) {
        const res = await productService.getAll(page, pageSize);
        const productList = res.content || [];
        allProducts = [...allProducts, ...productList];
        
        // Check if there are more pages
        hasMore = res.totalPages > page + 1;
        page++;
      }

      // Filter across all products
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
      toast.error(e.response?.data?.message || 'Search failed');
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
        toast.success('Product updated');
      } else {
        await productService.create(form);
        toast.success('Product created');
      }
      setOpen(false);
      setEditingId(null);
      setForm({ barcode: '', clientId: '', name: '', mrp: 0, imageUrl: '' });
      loadProducts(currentPage);
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Operation failed');
    }
  };

  const handleInventorySubmit = async () => {
    try {
      await productService.updateInventory(inventoryForm.productId, inventoryForm);
      toast.success('Inventory updated');
      setOpen(false);
      setInventoryForm({ productId: '', quantity: 0 });
      loadProducts(currentPage);
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to update inventory');
    }
  };

  const downloadTsvFile = (content: string, filename: string) => {
    // Decode base64 content
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

    // skip header, check status column
    for (let i = 1; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue; // skip empty lines

      const columns = line.split('\t');
      if (columns.length > 1) {
        const status = columns[1].trim();
        if (status === 'FAILED') {
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

    for (let i = 1; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;
      const columns = line.split('\t');
      if (columns.length >= 4) {
        const [rowNumber, status, error, data] = columns;
        if (status.trim() === 'FAILED') {
          failures.push({
            rowNumber: rowNumber.trim(),
            error: error.trim(),
            data: data.trim(),
          });
        }
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
      toast.error('Please select a TSV file to upload');
      return;
    }

    if (uploadFile.size > 5 * 1024 * 1024) {
      toast.error('File size must be less than 5MB');
      return;
    }

    setUploading(true);
    const reader = new FileReader();
    reader.onload = async (e) => {
      try {
        const text = e.target?.result as string;
        const base64 = btoa(unescape(encodeURIComponent(text)));

        let resultTsv: string;
        if (uploadType === 'inventory') {
          resultTsv = await productService.uploadInventoryTsvWithResults(base64);
        } else {
          resultTsv = await productService.uploadProductsTsvWithResults(base64);
        }

        setUploadResultTsv(resultTsv);
        const hasErrors = tsvHasErrors(resultTsv);
        const failures = extractFailures(resultTsv);
        setUploadFailures(failures);

        if (hasErrors) {
          toast.error('Upload completed with some errors. See details below.');
        } else {
          toast.success(
            uploadType === 'inventory'
              ? 'Inventory uploaded successfully.'
              : 'Products uploaded successfully.'
          );
          setUploadDialogOpen(false);
        }

        setCurrentPage(0);
        await loadProducts(0);
      } catch (err: any) {
        toast.error(err?.response?.data?.message || 'Upload failed');
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
            Manage product catalog and inventory in a clean, card-based view.
          </Typography>
        </Box>

        <ActionBox>
          {isSupervisor && (
            <>
              <Button
                variant="outlined"
                startIcon={<CloudUpload />}
                disabled={loading}
                sx={{ borderRadius: '999px' }}
                onClick={() => handleOpenUploadDialog('products')}
              >
                Products TSV
              </Button>

              <Button
                variant="outlined"
                startIcon={<CloudUpload />}
                disabled={loading}
                sx={{ borderRadius: '999px' }}
                onClick={() => handleOpenUploadDialog('inventory')}
              >
                Inventory TSV
              </Button>
            </>
          )}

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
        </ActionBox>
      </HeaderBox>

      {/* SEARCH BAR */}
      <SearchBox>
        <TextField
          label="Search"
          size="small"
          value={searchValue}
          onChange={(e) => setSearchValue(e.target.value)}
          sx={{ width: 260 }}
        />

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
      <Dialog open={uploadDialogOpen} onClose={handleCloseUploadDialog} fullWidth maxWidth="sm">
        <DialogTitle>
          {uploadType === 'inventory'
            ? 'Upload Inventory TSV'
            : uploadType === 'products'
            ? 'Upload Products TSV'
            : 'Upload TSV'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <Typography variant="body2" color="text.secondary">
              Select a TSV file in the expected format. You can download a ready-to-use template
              below.
            </Typography>

            <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center', flexWrap: 'wrap' }}>
              <Button
                variant="outlined"
                size="small"
                onClick={() =>
                  uploadType && downloadTemplateTsv(uploadType === 'inventory' ? 'inventory' : 'products')
                }
              >
                Download template
              </Button>

              <Button
                variant="outlined"
                size="small"
                component="label"
                disabled={uploading}
              >
                {uploadFile ? uploadFile.name : 'Select TSV file'}
                <input
                  type="file"
                  accept=".tsv,.txt"
                  hidden
                  onChange={(e) => {
                    const file = (e.target as HTMLInputElement).files?.[0] || null;
                    setUploadFile(file);
                  }}
                />
              </Button>
            </Box>

            {uploadFailures.length > 0 && (
              <Box
                sx={{
                  mt: 2,
                  p: 1.5,
                  borderRadius: 1,
                  border: '1px solid rgba(248,113,113,0.4)',
                  bgcolor: 'rgba(127,29,29,0.1)',
                  maxHeight: 220,
                  overflowY: 'auto',
                }}
              >
                <Typography
                  variant="subtitle2"
                  sx={{ fontWeight: 600, mb: 1, color: '#fecaca' }}
                >
                  Failed rows
                </Typography>
                {uploadFailures.map((f) => (
                  <Box key={f.rowNumber + f.data} sx={{ mb: 1 }}>
                    <Typography variant="caption" sx={{ color: '#fecaca' }}>
                      Row {f.rowNumber}: {f.error}
                    </Typography>
                    {f.data && (
                      <Typography
                        variant="caption"
                        sx={{ display: 'block', color: '#f97373' }}
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
        <DialogActions>
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
            >
              Download results TSV
            </Button>
          )}
          <Box sx={{ flex: 1 }} />
          <Button onClick={handleCloseUploadDialog} disabled={uploading}>
            Close
          </Button>
          <Button
            variant="contained"
            onClick={handleUploadConfirm}
            disabled={uploading || !uploadFile || !uploadType}
          >
            {uploading ? 'Uploading...' : 'Upload'}
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
                  boxShadow: '0 4px 16px rgba(15,23,42,0.08)',
                  '&:hover': {
                    borderColor: '#1976d2',
                    boxShadow: '0 10px 24px rgba(15,23,42,0.12)',
                    transform: 'translateY(-3px)',
                    transition: 'all 0.15s ease-out',
                  },
                }}
              >
                <CardHeader
                  sx={{
                    pb: 1,
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
                        bgcolor: '#e5e7eb',
                        fontSize: 14,
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
                      sx={{ fontWeight: 600, color: '#111827', mb: 0.25 }}
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
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="caption" sx={{ color: '#6b7280' }}>
                      MRP
                    </Typography>
                    <Typography
                      variant="body2"
                      sx={{ fontWeight: 600, color: '#111827' }}
                    >
                      ₹{p.mrp.toFixed(2)}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="caption" sx={{ color: '#6b7280' }}>
                      Inventory
                    </Typography>
                    <Chip
                      size="small"
                      label={p.quantity ?? 0}
                      sx={{
                        height: 22,
                        borderRadius: '999px',
                        backgroundColor:
                          (p.quantity ?? 0) === 0
                            ? 'rgba(248,113,113,0.12)'
                            : 'rgba(34,197,94,0.1)',
                        color:
                          (p.quantity ?? 0) === 0 ? '#f97373' : 'rgba(74,222,128,0.95)',
                        fontSize: 11,
                        px: 1,
                      }}
                    />
                  </Box>
                </CardContent>
                <CardActions
                  sx={{
                    px: 2.25,
                    pb: 2,
                    pt: 0.5,
                    display: 'flex',
                    justifyContent: 'space-between',
                  }}
                >
                  <Typography variant="caption" sx={{ color: '#9ca3af', fontStyle: 'italic' }}>
                    {isSupervisor ? 'Editable' : 'View only'}
                  </Typography>
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

      {/* DIALOG */}
      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="md">
        <DialogTitle>
          {editingId ? 'Edit Product & Inventory' : 'Add Product'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <TextField
              fullWidth
              label="Barcode"
              value={form.barcode}
              onChange={(e) => setForm({ ...form, barcode: e.target.value })}
              disabled={!!editingId}
              required
            />
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
                  label="Client"
                  required
                  helperText={loadingClients ? 'Loading clients...' : clients.length === 0 ? 'No clients available. Please create clients first.' : 'Type to search by name or client ID'}
                  InputProps={{
                    ...params.InputProps,
                    endAdornment: (
                      <>
                        {loadingClients ? <CircularProgress color="inherit" size={20} /> : null}
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
            <TextField
              fullWidth
              label="Product Name"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
            <TextField
              fullWidth
              label="MRP"
              type="number"
              value={form.mrp}
              onChange={(e) => setForm({ ...form, mrp: parseFloat(e.target.value) || 0 })}
              required
            />
            <TextField
              fullWidth
              label="Image URL"
              value={form.imageUrl}
              onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
            />
            {editingId && (
              <TextField
                fullWidth
                label="Inventory Quantity"
                type="number"
                value={inventoryForm.quantity}
                onChange={(e) =>
                  setInventoryForm({
                    ...inventoryForm,
                    quantity: parseInt(e.target.value) || 0,
                  })
                }
                required
              />
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          {editingId ? (
            <>
              <Button
                variant="contained"
                onClick={handleInventorySubmit}
                color="secondary"
              >
                Update Inventory Only
              </Button>
              <Button variant="contained" onClick={handleSubmit}>
                Update Product
              </Button>
            </>
          ) : (
            <Button variant="contained" onClick={handleSubmit}>
              Save
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </StyledContainer>
  );
}
