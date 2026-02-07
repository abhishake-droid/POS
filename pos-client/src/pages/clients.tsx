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
  CircularProgress,
} from '@mui/material';
import {
  ChevronLeft,
  ChevronRight,
  FirstPage,
  LastPage,
  Add,
  Search,
  Edit,
} from '@mui/icons-material';
import { Tooltip } from '@mui/material';
import { styled } from '@mui/material/styles';
import { toastSuccess, toastError } from '../utils/toast';
import { clientService } from '../services/client.service';
import { ClientData, ClientForm } from '../types/client.types';
import { useAuth } from '../contexts/AuthContext';
import { formatDateText } from '../utils/dateFormat';

type ClientSearchFilter = 'clientId' | 'name' | 'email';

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
  padding: '1.25rem 1.5rem',
  borderRadius: '16px',
  backgroundColor: '#ffffff',
  border: '1px solid #e5e7eb',
  boxShadow: '0 2px 8px rgba(15,23,42,0.08)',
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

const StyledTableContainer = styled(TableContainer)({
  borderRadius: '12px',
  boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
});

const StyledTableHead = styled(TableHead)({
  backgroundColor: '#f1f5f9',
  '& .MuiTableCell-head': {
    color: '#0f172a',
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
  backgroundColor: '#1e3a8a',
  color: 'white',
  '&:hover': {
    backgroundColor: '#1e40af',
  },
});

/* ================= COMPONENT ================= */

export default function Clients() {
  const { isSupervisor } = useAuth();
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
      toastError(e.response?.data?.message || 'Failed to load clients');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchValue.trim()) {
      loadClients(0);
      setCurrentPage(0);
      return;
    }

    if (searchFilter === 'clientId') {
      try {
        const client = await clientService.getById(searchValue.trim());
        setClients([client]);
        setTotalPages(1);
        setTotalElements(1);
        setCurrentPage(0);
      } catch {
        toastError('Client not found');
        setClients([]);
        setTotalPages(0);
        setTotalElements(0);
      }
      return;
    }

    setLoading(true);
    try {
      let allClients: ClientData[] = [];
      let page = 0;
      const pageSize = 100; // Use larger page size for fetching all
      let hasMore = true;

      while (hasMore) {
        const res = await clientService.getAll(page, pageSize);
        const clientList = res.content || [];
        allClients = [...allClients, ...clientList];

        hasMore = res.totalPages > page + 1;
        page++;
      }

      const value = searchValue.toLowerCase();

      const filtered = allClients.filter((c) => {
        switch (searchFilter) {
          case 'name':
            return c.name.toLowerCase().includes(value);

          case 'email':
            return c.email.toLowerCase().includes(value);

          default:
            return true;
        }
      });



      setClients(filtered);
      setTotalPages(1);
      setTotalElements(filtered.length);
      setCurrentPage(0);
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Search failed');
      setClients([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    try {
      if (editingId) {
        await clientService.update(editingId, form);
        toastSuccess('Client updated');
      } else {
        await clientService.create(form);
        toastSuccess('Client created');
      }
      setOpen(false);
      setEditingId(null);
      setForm({ name: '', email: '', phone: '' });
      loadClients(currentPage);
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Operation failed');
    }
  };

  /* ================= RENDER ================= */

  return (
    <StyledContainer maxWidth="lg">

      {/* HEADER */}
      <HeaderBox>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5, color: '#111827' }}>
            Client Management
          </Typography>
          <Typography variant="body2" sx={{ color: '#6b7280' }}>
            Manage client information and contacts.
          </Typography>
        </Box>

        {isSupervisor && (
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => {
              setEditingId(null);
              setForm({ name: '', email: '', phone: '' });
              setOpen(true);
            }}
            sx={{ borderRadius: '999px', px: 3, py: 1 }}
          >
            Add Client
          </Button>
        )}
      </HeaderBox>

      {/* SEARCH BAR (BELOW HEADER) */}
      <SearchBox>
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
          <MenuItem value="name">Name </MenuItem>
          <MenuItem value="email">Email</MenuItem>
        </TextField>

        <TextField
          label="Search"
          size="small"
          value={searchValue}
          onChange={(e) => setSearchValue(e.target.value)}
          placeholder={
            searchFilter === 'clientId' ? 'Enter client ID' :
              searchFilter === 'name' ? 'Enter client name' :
                'Enter email address'
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
              {isSupervisor && <TableCell align="center">Actions</TableCell>}
            </TableRow>
          </StyledTableHead>
          <TableBody>
            {clients.map((c) => (
              <StyledTableRow key={c.id}>
                <TableCell>{c.clientId}</TableCell>
                <TableCell>{c.name}</TableCell>
                <TableCell>{c.phone}</TableCell>
                <TableCell>{c.email}</TableCell>
                {isSupervisor && (
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
                )}
              </StyledTableRow>
            ))}
          </TableBody>
        </Table>
      </StyledTableContainer>

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

      {/* DIALOG */}
      <Dialog open={open} onClose={(event, reason) => { if (reason === 'backdropClick') return; setOpen(false); }} fullWidth maxWidth="sm">
        <DialogTitle>
          {editingId ? 'Edit Client' : 'Add Client'}
        </DialogTitle>
        <DialogContent>
          {editingId && (
            <TextField
              fullWidth
              label="Client ID"
              margin="normal"
              value={clients.find(c => c.id === editingId)?.clientId || ''}
              InputProps={{
                readOnly: true,
              }}
              sx={{
                '& .MuiInputBase-input': {
                  backgroundColor: '#f9fafb',
                  cursor: 'not-allowed',
                },
              }}
            />
          )}
          <TextField
            fullWidth
            label={
              <>
                Name <Box component="span" sx={{ color: 'error.main' }}>*</Box>
              </>
            }
            margin="normal"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />
          <TextField
            fullWidth
            label={
              <>
                Phone <Box component="span" sx={{ color: 'error.main' }}>*</Box>
              </>
            }
            margin="normal"
            value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
          />
          <TextField
            fullWidth
            label={
              <>
                Email <Box component="span" sx={{ color: 'error.main' }}>*</Box>
              </>
            }
            margin="normal"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setOpen(false)}
            sx={{
              '&:hover': {
                backgroundColor: 'rgba(0, 0, 0, 0.04)',
              },
            }}
          >
            Cancel
          </Button>
          <Button variant="contained" onClick={handleSubmit}>
            Save
          </Button>
        </DialogActions>
      </Dialog>

    </StyledContainer>
  );
}
