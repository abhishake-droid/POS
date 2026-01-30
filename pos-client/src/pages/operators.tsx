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
  CircularProgress,
} from '@mui/material';
import {
  ChevronLeft,
  ChevronRight,
  FirstPage,
  LastPage,
  Add,
} from '@mui/icons-material';
import { Tooltip } from '@mui/material';
import { styled } from '@mui/material/styles';
import { operatorService } from '../services/operator.service';
import { OperatorData, OperatorForm } from '../types/operator.types';
import { useAuth } from '../contexts/AuthContext';
import { toast } from 'react-toastify';
import AuthGuard from '../components/AuthGuard';

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
  marginBottom: '2rem',
  padding: '1.5rem',
  borderRadius: '12px',
  backgroundColor: '#ffffff',
  border: '1px solid #e5e7eb',
  boxShadow: '0 2px 8px rgba(15,23,42,0.08)',
});

const StyledTableContainer = styled(TableContainer)({
  borderRadius: '8px',
  boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
});

const StyledTableHead = styled(TableHead)({
  backgroundColor: '#1976d2',
  '& .MuiTableCell-head': {
    color: 'white',
    fontWeight: 600,
    padding: '1rem',
  },
});

const StyledTableRow = styled(TableRow)({
  '&:nth-of-type(odd)': {
    backgroundColor: '#f9f9f9',
  },
  '&:hover': {
    backgroundColor: '#e3f2fd',
  },
  '& .MuiTableCell-root': {
    padding: '1rem',
  },
});

const PaginationBox = styled(Box)({
  display: 'flex',
  justifyContent: 'center',
  gap: '1rem',
  marginTop: '2rem',
});

export default function Operators() {
  const { isSupervisor } = useAuth();
  const [operators, setOperators] = useState<OperatorData[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<OperatorForm>({
    email: '',
    name: '',
    password: '',
  });

  useEffect(() => {
    if (isSupervisor) {
      loadOperators(currentPage);
    }
  }, [isSupervisor, currentPage]);

  const loadOperators = async (page: number) => {
    setLoading(true);
    try {
      const res = await operatorService.getAll(page, PAGE_SIZE);
      setOperators(res.content || []);
      setTotalPages(res.totalPages || 0);
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to load operators');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    try {
      await operatorService.create(form);
      toast.success('Operator created successfully');
      setOpen(false);
      setForm({ email: '', name: '', password: '' });
      loadOperators(currentPage);
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to create operator');
    }
  };

  return (
    <AuthGuard requireSupervisor>
      <StyledContainer maxWidth="lg">
        <HeaderBox>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5, color: '#111827' }}>
              Operator Management
            </Typography>
            <Typography variant="body2" sx={{ color: '#6b7280' }}>
              Manage system operators and their access.
            </Typography>
          </Box>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => {
              setForm({ email: '', name: '', password: '' });
              setOpen(true);
            }}
            sx={{ borderRadius: '999px', px: 3, py: 1 }}
          >
            Add Operator
          </Button>
        </HeaderBox>

        {loading && (
          <Box display="flex" justifyContent="center" p={4}>
            <CircularProgress />
          </Box>
        )}

        {!loading && (
          <>
            <StyledTableContainer>
              <Table>
                <StyledTableHead>
                  <TableRow>
                    <TableCell>Email</TableCell>
                    <TableCell>Name</TableCell>
                    <TableCell>Role</TableCell>
                  </TableRow>
                </StyledTableHead>
                <TableBody>
                  {operators.map((op) => (
                    <StyledTableRow key={op.id}>
                      <TableCell>{op.email}</TableCell>
                      <TableCell>{op.name}</TableCell>
                      <TableCell>
                        <Chip
                          label={op.role}
                          color={op.role === 'SUPERVISOR' ? 'secondary' : 'default'}
                          size="small"
                        />
                      </TableCell>
                    </StyledTableRow>
                  ))}
                </TableBody>
              </Table>
            </StyledTableContainer>

            {totalPages > 1 && (
              <PaginationBox>
                <Tooltip title="First Page">
                  <span>
                    <IconButton
                      disabled={currentPage === 0 || loading}
                      onClick={() => setCurrentPage(0)}
                    >
                      <FirstPage />
                    </IconButton>
                  </span>
                </Tooltip>

                <Tooltip title="Previous Page">
                  <span>
                    <IconButton
                      disabled={currentPage === 0 || loading}
                      onClick={() => setCurrentPage((p) => p - 1)}
                    >
                      <ChevronLeft />
                    </IconButton>
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
                    <IconButton
                      disabled={currentPage >= totalPages - 1 || loading}
                      onClick={() => setCurrentPage((p) => p + 1)}
                    >
                      <ChevronRight />
                    </IconButton>
                  </span>
                </Tooltip>

                <Tooltip title="Last Page">
                  <span>
                    <IconButton
                      disabled={currentPage >= totalPages - 1 || loading}
                      onClick={() => setCurrentPage(totalPages - 1)}
                    >
                      <LastPage />
                    </IconButton>
                  </span>
                </Tooltip>
              </PaginationBox>
            )}
          </>
        )}

        <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
          <DialogTitle>Add Operator</DialogTitle>
          <DialogContent>
            <TextField
              fullWidth
              label={
                <>
                  Email <Box component="span" sx={{ color: 'error.main' }}>*</Box>
                </>
              }
              type="email"
              margin="normal"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
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
                  Password <Box component="span" sx={{ color: 'error.main' }}>*</Box>
                </>
              }
              type="password"
              margin="normal"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              helperText="Minimum 6 characters"
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpen(false)}>Cancel</Button>
            <Button variant="contained" onClick={handleSubmit}>
              Create Operator
            </Button>
          </DialogActions>
        </Dialog>
      </StyledContainer>
    </AuthGuard>
  );
}
