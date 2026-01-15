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
  Chip,
} from '@mui/material';
import {
  ChevronLeft,
  ChevronRight,
  Add,
  Edit,
} from '@mui/icons-material';
import { styled } from '@mui/material/styles';
import { clientService } from '../services/client.service';
import { ClientData, ClientForm } from '../types/client.types';
import { toast } from 'react-toastify';

const PAGE_SIZE = 10;

const StyledContainer = styled(Container)(({ theme }) => ({
  paddingTop: '2rem',
  paddingBottom: '2rem',
  minHeight: 'calc(100vh - 64px)',
}));

const HeaderBox = styled(Box)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  marginBottom: '2rem',
  padding: '1.5rem',
  backgroundColor: '#f5f5f5',
  borderRadius: '12px',
  boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
}));

const StyledTableContainer = styled(TableContainer)(({ theme }) => ({
  borderRadius: '12px',
  boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
  overflow: 'hidden',
}));

const StyledTableHead = styled(TableHead)(({ theme }) => ({
  backgroundColor: '#1976d2',
  '& .MuiTableCell-head': {
    color: 'white',
    fontWeight: 600,
    fontSize: '1rem',
  },
}));

const StyledTableRow = styled(TableRow)(({ theme }) => ({
  '&:nth-of-type(odd)': {
    backgroundColor: '#f9f9f9',
  },
  '&:hover': {
    backgroundColor: '#e3f2fd',
    transition: 'background-color 0.2s ease',
  },
}));

const StyledButton = styled(Button)(({ theme }) => ({
  borderRadius: '8px',
  textTransform: 'none',
  fontWeight: 500,
  padding: '0.5rem 1.5rem',
  boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
  '&:hover': {
    boxShadow: '0 4px 8px rgba(0,0,0,0.3)',
    transform: 'translateY(-2px)',
    transition: 'all 0.2s ease',
  },
}));

const PaginationBox = styled(Box)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  gap: '1rem',
  marginTop: '2rem',
  padding: '1.5rem',
  backgroundColor: '#f5f5f5',
  borderRadius: '12px',
  boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
}));

const StyledPagination = styled(Pagination)(({ theme }) => ({
  '& .MuiPaginationItem-root': {
    borderRadius: '8px',
    fontWeight: 500,
    '&.Mui-selected': {
      backgroundColor: '#1976d2',
      color: 'white',
      '&:hover': {
        backgroundColor: '#1565c0',
      },
    },
  },
}));

const StyledIconButton = styled(IconButton)(({ theme }) => ({
  backgroundColor: '#1976d2',
  color: 'white',
  '&:hover': {
    backgroundColor: '#1565c0',
    transform: 'scale(1.1)',
    transition: 'all 0.2s ease',
  },
  '&:disabled': {
    backgroundColor: '#e0e0e0',
    color: '#9e9e9e',
  },
}));

export default function Clients() {
  const [clients, setClients] = useState<ClientData[]>([]);
  const [searchClientId, setSearchClientId] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<ClientForm>({
    name: '',
    phone: '',
    email: '',
  });
  const [editingId, setEditingId] = useState<string | null>(null);

  useEffect(() => {
    loadClients(currentPage);
  }, [currentPage]);

  const loadClients = async (page: number) => {
    setLoading(true);
    try {
      const response = await clientService.getAll(page, PAGE_SIZE);
      setClients(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
    } catch (error: any) {
      console.error('Error loading clients:', error);
      toast.error(error.response?.data?.message || 'Failed to load clients');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    try {
      if (editingId) {
        await clientService.update(editingId, form);
        toast.success('Client updated successfully');
      } else {
        await clientService.create(form);
        toast.success('Client created successfully');
      }

      setOpen(false);
      setForm({ name: '', email: '', phone: '' });
      setEditingId(null);

      // Reload current page or go to last page if new client added
      await loadClients(currentPage);
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleSearch = async () => {
    if (!searchClientId.trim()) {
      loadClients(0);
      return;
    }

    setLoading(true);
    try {
      const response = await clientService.getById(searchClientId.trim());
      setClients([response]);
      setTotalPages(1);
      setTotalElements(1);
      setCurrentPage(0);
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Client not found');
      setClients([]);
    } finally {
      setLoading(false);
    }
  };


  const handleEdit = (client: ClientData) => {
    setForm({
      name: client.name,
      phone: client.phone,
      email: client.email,
    });
    setEditingId(client.id);
    setOpen(true);
  };

  const handlePageChange = (event: React.ChangeEvent<unknown>, value: number) => {
    setCurrentPage(value - 1); // MUI Pagination is 1-indexed, backend is 0-indexed
  };

  const handlePrevious = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };

  const handleNext = () => {
    if (currentPage < totalPages - 1) {
      setCurrentPage(currentPage + 1);
    }
  };

  const startIndex = currentPage * PAGE_SIZE + 1;
  const endIndex = Math.min((currentPage + 1) * PAGE_SIZE, totalElements);

  return (
    <StyledContainer maxWidth="lg">
      <HeaderBox>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 600, color: '#1976d2', mb: 0.5 }}>
            Client Management
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          <TextField
              label="Search by Client ID"
              size="small"
              value={searchClientId}
              onChange={(e) => setSearchClientId(e.target.value)}
          />
          <Button
              variant="contained"
              onClick={handleSearch}
              sx={{ textTransform: 'none' }}
          >
            Search
          </Button>
        </Box>
        <StyledButton
          variant="contained"
          startIcon={<Add />}
          onClick={() => {
            setForm({ name: '', email: '', phone: '' });
            setEditingId(null);
            setOpen(true);
          }}
        >
          Add Client
        </StyledButton>
      </HeaderBox>

      {totalElements > 0 && (
        <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Chip
            label={`Showing ${startIndex}-${endIndex} of ${totalElements} clients`}
            color="primary"
            sx={{ fontWeight: 500 }}
          />
        </Box>
      )}

      <StyledTableContainer>
        <Table>
          <StyledTableHead>
            <TableRow>
              <TableCell>Client ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Phone</TableCell>
              <TableCell>Email</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </StyledTableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ padding: '3rem' }}>
                  <Typography>Loading...</Typography>
                </TableCell>
              </TableRow>
            ) : clients.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ padding: '3rem' }}>
                  <Typography variant="h6" color="text.secondary">
                    No clients found. Click "Add Client" to create one.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              clients.map((client) => (
                <StyledTableRow key={client.id}>
                  <TableCell>{client.clientId}</TableCell>
                  <TableCell sx={{ fontWeight: 500 }}>{client.name}</TableCell>
                  <TableCell>{client.phone}</TableCell>
                  <TableCell>{client.email}</TableCell>
                  <TableCell align="center">
                    <Button
                      variant="outlined"
                      startIcon={<Edit />}
                      onClick={() => handleEdit(client)}
                      sx={{
                        borderRadius: '8px',
                        textTransform: 'none',
                        borderColor: '#1976d2',
                        color: '#1976d2',
                        '&:hover': {
                          backgroundColor: '#e3f2fd',
                          borderColor: '#1565c0',
                        },
                      }}
                    >
                      Edit
                    </Button>
                  </TableCell>
                </StyledTableRow>
              ))
            )}
          </TableBody>
        </Table>
      </StyledTableContainer>

      {totalPages > 1 && (
        <PaginationBox>
          <StyledIconButton
            onClick={handlePrevious}
            disabled={currentPage === 0 || loading}
            aria-label="previous page"
          >
            <ChevronLeft />
          </StyledIconButton>

          <StyledPagination
            count={totalPages}
            page={currentPage + 1} // Convert to 1-indexed for MUI
            onChange={handlePageChange}
            color="primary"
            shape="rounded"
            size="large"
            showFirstButton
            showLastButton
          />

          <StyledIconButton
            onClick={handleNext}
            disabled={currentPage >= totalPages - 1 || loading}
            aria-label="next page"
          >
            <ChevronRight />
          </StyledIconButton>
        </PaginationBox>
      )}

      <Dialog
        open={open}
        onClose={() => setOpen(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: '12px',
            boxShadow: '0 8px 24px rgba(0,0,0,0.15)',
          },
        }}
      >
        <DialogTitle sx={{ fontWeight: 600, fontSize: '1.5rem', pb: 1 }}>
          {editingId ? 'Edit Client' : 'Add New Client'}
        </DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Name"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            margin="normal"
            sx={{ mt: 2 }}
            variant="outlined"
          />
          <TextField
            fullWidth
            label="Phone Number"
            value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
            margin="normal"
            variant="outlined"
          />
          <TextField
            fullWidth
            label="Email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            margin="normal"
            variant="outlined"
            type="email"
          />
        </DialogContent>
        <DialogActions sx={{ padding: '1.5rem', gap: 1 }}>
          <Button
            onClick={() => setOpen(false)}
            variant="outlined"
            sx={{ borderRadius: '8px', textTransform: 'none' }}
          >
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            sx={{ borderRadius: '8px', textTransform: 'none' }}
          >
            {editingId ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </StyledContainer>
  );
}
