import { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Pagination,
  IconButton,
  MenuItem,
  Avatar,
  Input,
  CircularProgress,
  Autocomplete,
} from '@mui/material';
import {
  ChevronLeft,
  ChevronRight,
  Upload,
  Edit,
  CloudUpload,
  Add,
} from '@mui/icons-material';
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
  paddingTop: '2rem',
  paddingBottom: '2rem',
  minHeight: 'calc(100vh - 64px)',
});

const HeaderBox = styled(Box)({
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  marginBottom: '1.5rem',
  padding: '1.5rem',
  backgroundColor: '#f5f5f5',
  borderRadius: '12px',
  boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
});

const ActionBox = styled(Box)({
  display: 'flex',
  gap: '1rem',
  alignItems: 'center',
});

const SearchBox = styled(Box)({
  display: 'flex',
  gap: '1rem',
  alignItems: 'center',
  padding: '1rem 1.5rem',
  marginBottom: '2rem',
  backgroundColor: '#fafafa',
  borderRadius: '10px',
  border: '1px solid #e0e0e0',
});

const StyledTableContainer = styled(TableContainer)({
  borderRadius: '12px',
  boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
});

const StyledTableHead = styled(TableHead)({
  backgroundColor: '#1976d2',
  '& .MuiTableCell-head': {
    color: 'white',
    fontWeight: 600,
  },
});

const StyledTableRow = styled(TableRow)({
  '&:nth-of-type(odd)': {
    backgroundColor: '#f9f9f9',
  },
  '&:hover': {
    backgroundColor: '#e3f2fd',
  },
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

const ImageCell = styled(TableCell)({
  width: '80px',
  padding: '8px',
});

const HiddenInput = styled('input')({
  display: 'none',
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
      }
      return;
    }

    // UI-level filtering for other fields
    const filtered = products.filter((p) => {
      const value = searchValue.toLowerCase();
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


  const handleFileUpload = async (file: File, isInventory: boolean) => {
    if (!file) return;

    // Check file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error('File size must be less than 5MB');
      return;
    }

    setLoading(true);
    try {
      const reader = new FileReader();
      reader.onload = async (e) => {
        try {
          const text = e.target?.result as string;
          // Convert to base64
          const base64 = btoa(unescape(encodeURIComponent(text)));

          if (isInventory) {
            const resultTsv = await productService.uploadInventoryTsvWithResults(base64);
            const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
            if (tsvHasErrors(resultTsv)) {
              downloadTsvFile(resultTsv, `inventory-upload-results-${timestamp}.tsv`);
              toast.error('Inventory upload completed with errors.');
            } else {
              toast.success('Inventory uploaded successfully.');
            }

          } else {
            const resultTsv = await productService.uploadProductsTsvWithResults(base64);
            const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
            if (tsvHasErrors(resultTsv)) {
              downloadTsvFile(resultTsv, `products-upload-results-${timestamp}.tsv`);
              toast.error('Upload completed with errors.');
            } else {
              toast.success('Products uploaded successfully.');
            }

          }
          // Reset to first page and reload to show updated data
          setCurrentPage(0);
          await loadProducts(0);
        } catch (err: any) {
          toast.error(err.response?.data?.message || 'Upload failed');
        } finally {
          setLoading(false);
        }
      };
      reader.readAsText(file);
    } catch (e: any) {
      toast.error('Failed to read file');
      setLoading(false);
    }
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
        <Typography variant="h4" sx={{ fontWeight: 600, color: '#1976d2' }}>
          Product Management
        </Typography>

        <ActionBox>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => {
              setEditingId(null);
              setForm({ barcode: '', clientId: '', name: '', mrp: 0, imageUrl: '' });
              setInventoryForm({ productId: '', quantity: 0 });
              loadClients(); // Reload clients when opening dialog
              setOpen(true);
            }}
          >
            Add Product
          </Button>

          {isSupervisor && (
            <>
              <HiddenInput
                id="products-tsv-upload"
                type="file"
                accept=".tsv,.txt"
                onChange={(e) => {
                  const file = (e.target as HTMLInputElement).files?.[0];
                  if (file) handleFileUpload(file, false);
                  // Reset input to allow uploading the same or another file again
                  (e.target as HTMLInputElement).value = '';
                }}
              />
              <label htmlFor="products-tsv-upload">
                <Button
                  variant="outlined"
                  component="span"
                  startIcon={<CloudUpload />}
                  disabled={loading}
                >
                  Upload Products TSV
                </Button>
              </label>

              <HiddenInput
                  id="inventory-tsv-upload"
                  type="file"
                  accept=".tsv,.txt"
                  onChange={(e) => {
                    const file = (e.target as HTMLInputElement).files?.[0];
                    if (file) {
                      handleFileUpload(file, true);
                    }
                    // Reset input to allow uploading the same or another file again
                    (e.target as HTMLInputElement).value = '';
                  }}
              />

              <label htmlFor="inventory-tsv-upload">
                <Button
                  variant="outlined"
                  component="span"
                  startIcon={<CloudUpload />}
                  disabled={loading}
                >
                  Upload Inventory TSV
                </Button>
              </label>
            </>
          )}
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

        <Button variant="contained" onClick={handleSearch} disabled={loading}>
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

      {/* TABLE */}
      {loading && (
        <Box display="flex" justifyContent="center" p={4}>
          <CircularProgress />
        </Box>
      )}

      {!loading && (
        <StyledTableContainer>
          <Table>
            <StyledTableHead>
              <TableRow>
                <TableCell>Image</TableCell>
                <TableCell>Barcode</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Client</TableCell>
                <TableCell>MRP</TableCell>
                <TableCell>Inventory</TableCell>
                <TableCell align="center">Actions</TableCell>
              </TableRow>
            </StyledTableHead>
            <TableBody>
              {products.map((p) => (
                <StyledTableRow key={p.id}>
                  <ImageCell>
                    {p.imageUrl ? (
                      <Avatar
                        src={p.imageUrl}
                        alt={p.name}
                        sx={{ width: 56, height: 56 }}
                        variant="rounded"
                      />
                    ) : (
                      <Avatar sx={{ width: 56, height: 56 }} variant="rounded">
                        No Image
                      </Avatar>
                    )}
                  </ImageCell>
                  <TableCell>{p.barcode}</TableCell>
                  <TableCell>{p.name}</TableCell>
                  <TableCell>{p.clientName || p.clientId}</TableCell>
                  <TableCell>₹{p.mrp.toFixed(2)}</TableCell>
                  <TableCell>{p.quantity || 0}</TableCell>
                  <TableCell align="center">
                    {isSupervisor ? (
                      <Button
                        startIcon={<Edit />}
                        onClick={() => handleEdit(p)}
                      >
                        Edit
                      </Button>
                    ) : (
                      <Typography variant="body2" color="text.secondary">
                        View Only
                      </Typography>
                    )}
                  </TableCell>
                </StyledTableRow>
              ))}
            </TableBody>
          </Table>
        </StyledTableContainer>
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
