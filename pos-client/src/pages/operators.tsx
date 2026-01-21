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
  Add,
} from '@mui/icons-material';
import { styled } from '@mui/material/styles';
import { operatorService } from '../services/operator.service';
import { OperatorData, OperatorForm } from '../types/operator.types';
import { useAuth } from '../contexts/AuthContext';
import { toast } from 'react-toastify';
import AuthGuard from '../components/AuthGuard';

const PAGE_SIZE = 10;

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
      setForm({ email: '', name: '' });
      loadOperators(currentPage);
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to create operator');
    }
  };

  return (
    <AuthGuard requireSupervisor>
      <StyledContainer maxWidth="lg">
        <HeaderBox>
          <Typography variant="h4" sx={{ fontWeight: 600, color: '#1976d2' }}>
            Operator Management
          </Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => {
              setForm({ email: '', name: '' });
              setOpen(true);
            }}
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
          </>
        )}

        <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
          <DialogTitle>Add Operator</DialogTitle>
          <DialogContent>
            <TextField
              fullWidth
              label="Email"
              type="email"
              margin="normal"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
            />
            <TextField
              fullWidth
              label="Name"
              margin="normal"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
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
