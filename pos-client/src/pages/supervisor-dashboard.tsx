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
  Grid,
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
import { toast } from 'react-toastify';
import AuthGuard from '../components/AuthGuard';

const PAGE_SIZE = 10;

const StyledContainer = styled(Container)({
  paddingTop: '2rem',
  paddingBottom: '2rem',
  minHeight: 'calc(100vh - 64px)',
});

const StyledCard = styled(Card)({
  borderRadius: '12px',
  boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
  marginBottom: '2rem',
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
      toast.error(e.response?.data?.message || 'Failed to load operators');
    } finally {
      setLoading(false);
    }
  };

  const loadAuditLogs = async () => {
    try {
      const logs = await auditLogService.getAllList();
      setAuditLogs(logs.slice(0, 50)); // Show latest 50
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to load audit logs');
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
        <Typography variant="h4" sx={{ fontWeight: 600, color: '#1976d2', mb: 3 }}>
          Supervisor Dashboard
        </Typography>

        {/* Operators List */}
        <StyledCard>
          <CardContent>
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
                          <TableCell>{getOperatorLastActivity(op.email)}</TableCell>
                        </StyledTableRow>
                      ))}
                    </TableBody>
                  </Table>
                </StyledTableContainer>

                {totalPages > 1 && (
                  <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1, mt: 2 }}>
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
          <CardContent>
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
                      <TableCell>{formatDate(log.timestamp)}</TableCell>
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