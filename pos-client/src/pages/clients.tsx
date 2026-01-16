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
  MenuItem,
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

type ClientSearchFilter = 'clientId' | 'name' | 'email' | 'phone';

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

/* ================= COMPONENT ================= */

export default function Clients() {
  const [clients, setClients] = useState<ClientData[]>([]);
  const [searchValue, setSearchValue] = useState('');
  const [searchFilter, setSearchFilter] =
      useState<ClientSearchFilter>('clientId');

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);

  const [open, setOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form, setForm] = useState<ClientForm>({
    name: '',
    phone: '',
    email: '',
  });

  useEffect(() => {
    loadClients(currentPage);
  }, [currentPage]);

  /* ================= API ================= */

  const loadClients = async (page: number) => {
    setLoading(true);
    try {
      const res = await clientService.getAll(page, PAGE_SIZE);
      setClients(res.content || []);
      setTotalPages(res.totalPages || 0);
      setTotalElements(res.totalElements || 0);
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to load clients');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchValue.trim()) {
      loadClients(0);
      return;
    }

    // ONLY clientId search hits backend
    if (searchFilter === 'clientId') {
      try {
        const client = await clientService.getById(searchValue.trim());
        setClients([client]);
        setTotalPages(1);
        setTotalElements(1);
        setCurrentPage(0);
      } catch {
        toast.error('Client not found');
        setClients([]);
      }
      return;
    }

    // UI-level filtering for name/email/phone
    const filtered = clients.filter((c) =>
        c[searchFilter]
            .toLowerCase()
            .includes(searchValue.toLowerCase())
    );
    setClients(filtered);
  };

  const handleSubmit = async () => {
    try {
      if (editingId) {
        await clientService.update(editingId, form);
        toast.success('Client updated');
      } else {
        await clientService.create(form);
        toast.success('Client created');
      }
      setOpen(false);
      setEditingId(null);
      setForm({ name: '', email: '', phone: '' });
      loadClients(currentPage);
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Operation failed');
    }
  };

  /* ================= RENDER ================= */

  return (
      <StyledContainer maxWidth="lg">

        {/* HEADER */}
        <HeaderBox>
          <Typography variant="h4" sx={{ fontWeight: 600, color: '#1976d2' }}>
            Client Management
          </Typography>

          <Button
              variant="contained"
              startIcon={<Add />}
              onClick={() => {
                setEditingId(null);
                setForm({ name: '', email: '', phone: '' });
                setOpen(true);
              }}
          >
            Add Client
          </Button>
        </HeaderBox>

        {/* SEARCH BAR (BELOW HEADER) */}
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
              onChange={(e) =>
                  setSearchFilter(e.target.value as ClientSearchFilter)
              }
              sx={{ width: 160 }}
          >
            <MenuItem value="clientId">Client ID</MenuItem>
            <MenuItem value="name">Name</MenuItem>
            <MenuItem value="email">Email</MenuItem>
            <MenuItem value="phone">Phone</MenuItem>
          </TextField>

          <Button variant="contained" onClick={handleSearch}>
            Search
          </Button>

          <Button
              variant="text"
              onClick={() => {
                setSearchValue('');
                setSearchFilter('clientId');
                loadClients(0);
              }}
          >
            Clear
          </Button>
        </SearchBox>

        {/* TABLE */}
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
              {clients.map((c) => (
                  <StyledTableRow key={c.id}>
                    <TableCell>{c.clientId}</TableCell>
                    <TableCell>{c.name}</TableCell>
                    <TableCell>{c.phone}</TableCell>
                    <TableCell>{c.email}</TableCell>
                    <TableCell align="center">
                      <Button
                          startIcon={<Edit />}
                          onClick={() => {
                            setEditingId(c.id);
                            setForm({
                              name: c.name,
                              phone: c.phone,
                              email: c.email,
                            });
                            setOpen(true);
                          }}
                      >
                        Edit
                      </Button>
                    </TableCell>
                  </StyledTableRow>
              ))}
            </TableBody>
          </Table>
        </StyledTableContainer>

        {/* PAGINATION */}
        {totalPages > 1 && (
            <PaginationBox>
              <StyledIconButton
                  disabled={currentPage === 0}
                  onClick={() => setCurrentPage((p) => p - 1)}
              >
                <ChevronLeft />
              </StyledIconButton>

              <Pagination
                  count={totalPages}
                  page={currentPage + 1}
                  onChange={(_, v) => setCurrentPage(v - 1)}
              />

              <StyledIconButton
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => setCurrentPage((p) => p + 1)}
              >
                <ChevronRight />
              </StyledIconButton>
            </PaginationBox>
        )}

        {/* DIALOG */}
        <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
          <DialogTitle>
            {editingId ? 'Edit Client' : 'Add Client'}
          </DialogTitle>
          <DialogContent>
            <TextField
                fullWidth
                label="Name"
                margin="normal"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
            <TextField
                fullWidth
                label="Phone"
                margin="normal"
                value={form.phone}
                onChange={(e) => setForm({ ...form, phone: e.target.value })}
            />
            <TextField
                fullWidth
                label="Email"
                margin="normal"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpen(false)}>Cancel</Button>
            <Button variant="contained" onClick={handleSubmit}>
              Save
            </Button>
          </DialogActions>
        </Dialog>

      </StyledContainer>
  );
}
