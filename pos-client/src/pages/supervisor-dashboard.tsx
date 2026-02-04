import { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Pagination,
  IconButton,
} from '@mui/material';
import {
  ChevronLeft,
  ChevronRight,
  People,
  Login as LoginIcon,
  Logout as LogoutIcon,
} from '@mui/icons-material';
import { styled } from '@mui/material/styles';
import { useAuth } from '../contexts/AuthContext';
import { operatorService } from '../services/operator.service';
import { auditLogService } from '../services/auditLog.service';
import { OperatorData, AuditLogData } from '../types/operator.types';
import { toastError, toastSuccess } from '../utils/toast';
import AuthGuard from '../components/AuthGuard';

const PAGE_SIZE = 10;

const StyledContainer = styled(Container)({
  paddingTop: '3rem',
  paddingBottom: '3rem',
  minHeight: 'calc(100vh - 64px)',
});

const HeaderBox = styled(Box)({
  marginBottom: '2rem',
  padding: '1.5rem',
  borderRadius: '12px',
  backgroundColor: '#ffffff',
  border: '1px solid #e5e7eb',
  boxShadow: '0 2px 8px rgba(15,23,42,0.08)',
});

const StyledCard = styled(Card)({
  borderRadius: '12px',
  boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
  marginBottom: '2rem',
});

const StyledTableContainer = styled(TableContainer)({
  borderRadius: '8px',
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

export default function SupervisorDashboard() {
  const { isSupervisor } = useAuth();
  const [operators, setOperators] = useState<OperatorData[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLogData[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isSupervisor) {
      loadOperators(currentPage);
      loadAuditLogs();
    }
  }, [isSupervisor, currentPage]);

  const loadOperators = async (page: number) => {
    setLoading(true);
    try {
      const res = await operatorService.getAll(page, PAGE_SIZE);
      setOperators(res.content || []);
      setTotalPages(res.totalPages || 0);
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Failed to load operators');
    } finally {
      setLoading(false);
    }
  };

  const loadAuditLogs = async () => {
    try {
      const logs = await auditLogService.getAllList();
      setAuditLogs(logs.slice(0, 50));
    } catch (e: any) {
      toastError(e.response?.data?.message || 'Failed to load audit logs');
    }
  };

  const formatDate = (timestamp: string) => {
    return new Date(timestamp).toLocaleString();
  };

  const getOperatorLastActivity = (operatorEmail: string) => {
    const operatorLogs = auditLogs.filter(log => log.operatorEmail === operatorEmail);
    if (operatorLogs.length === 0) return 'Never';
    const latest = operatorLogs[0];
    return `${latest.action} - ${formatDate(latest.timestamp)}`;
  };

  return (
    <AuthGuard requireSupervisor>
      <StyledContainer maxWidth="lg">
        <HeaderBox>
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5, color: '#111827' }}>
            Supervisor Dashboard
          </Typography>
          <Typography variant="body2" sx={{ color: '#6b7280' }}>
            Monitor operators and track system activity
          </Typography>
        </HeaderBox>

        {/* Operators List */}
        <StyledCard>
          <CardContent sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <People sx={{ fontSize: 32, color: '#1976d2', mr: 1 }} />
              <Typography variant="h5" sx={{ fontWeight: 600 }}>
                Registered Operators
              </Typography>
            </Box>

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
                        <TableCell>Last Activity</TableCell>
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
                          <TableCell sx={{ fontSize: '0.875rem' }}>
                            {getOperatorLastActivity(op.email)}
                          </TableCell>
                        </StyledTableRow>
                      ))}
                    </TableBody>
                  </Table>
                </StyledTableContainer>

                {totalPages > 1 && (
                  <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1, mt: 3 }}>
                    <IconButton
                      disabled={currentPage === 0}
                      onClick={() => setCurrentPage((p) => p - 1)}
                    >
                      <ChevronLeft />
                    </IconButton>
                    <Pagination
                      count={totalPages}
                      page={currentPage + 1}
                      onChange={(_, v) => setCurrentPage(v - 1)}
                      hidePrevButton
                      hideNextButton
                    />
                    <IconButton
                      disabled={currentPage >= totalPages - 1}
                      onClick={() => setCurrentPage((p) => p + 1)}
                    >
                      <ChevronRight />
                    </IconButton>
                  </Box>
                )}
              </>
            )}
          </CardContent>
        </StyledCard>

        {/* Activity Logs */}
        <StyledCard>
          <CardContent sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <LoginIcon sx={{ fontSize: 32, color: '#1976d2', mr: 1 }} />
              <Typography variant="h5" sx={{ fontWeight: 600 }}>
                Recent Activity Logs
              </Typography>
            </Box>

            <StyledTableContainer>
              <Table>
                <StyledTableHead>
                  <TableRow>
                    <TableCell>Operator</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell>Action</TableCell>
                    <TableCell>Timestamp</TableCell>
                  </TableRow>
                </StyledTableHead>
                <TableBody>
                  {auditLogs.slice(0, 20).map((log) => (
                    <StyledTableRow key={log.id}>
                      <TableCell>{log.operatorName}</TableCell>
                      <TableCell>{log.operatorEmail}</TableCell>
                      <TableCell>
                        <Chip
                          icon={log.action === 'LOGIN' ? <LoginIcon /> : <LogoutIcon />}
                          label={log.action}
                          color={log.action === 'LOGIN' ? 'success' : 'error'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell sx={{ fontSize: '0.875rem' }}>
                        {formatDate(log.timestamp)}
                      </TableCell>
                    </StyledTableRow>
                  ))}
                </TableBody>
              </Table>
            </StyledTableContainer>
          </CardContent>
        </StyledCard>
      </StyledContainer>
    </AuthGuard>
  );
}
