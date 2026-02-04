import { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Container,
  Grid,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  CircularProgress,
  Tabs,
  Tab,
  Autocomplete,
  Card,
  CardContent,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Divider,
} from '@mui/material';
import { styled } from '@mui/material/styles';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { toastError, toastSuccess, toastWarning } from '../utils/toast';
import AuthGuard from '../components/AuthGuard';
import { DailySalesData, ClientSalesReport } from '../types/report.types';
import { reportService } from '../services/report.service';
import { clientService } from '../services/client.service';
import { ClientData } from '../types/client.types';
import { formatINR } from '../utils/formatNumber';

const StyledContainer = styled(Container)({
  paddingTop: '2rem',
  paddingBottom: '2rem',
});

const HeaderBox = styled(Box)({
  marginBottom: '2rem',
  padding: '1.5rem',
  borderRadius: '12px',
  backgroundColor: '#1976d2',
  color: 'white',
});

const SectionCard = styled(Paper)({
  padding: '1.5rem',
  borderRadius: '12px',
  marginBottom: '1.5rem',
});

const StyledTableHead = styled(TableHead)({
  backgroundColor: '#1976d2',
  '& .MuiTableCell-head': {
    color: 'white',
    fontWeight: 600,
  },
});

const formatDateWords = (dateStr: string) => {
  if (!dateStr) return '';

  const date = new Date(dateStr);
  const day = date.getDate();
  const month = date.toLocaleString('en-IN', { month: 'short' });
  const year = date.getFullYear();

  const suffix =
    day % 10 === 1 && day !== 11 ? 'st' :
      day % 10 === 2 && day !== 12 ? 'nd' :
        day % 10 === 3 && day !== 13 ? 'rd' : 'th';

  return `${day}${suffix} ${month} ${year}`;
};

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index } = props;
  return (
    <div role="tabpanel" hidden={value !== index}>
      {value === index && <Box>{children}</Box>}
    </div>
  );
}

interface ProductDetailsModalProps {
  open: boolean;
  onClose: () => void;
  client: ClientSalesReport;
}

function ProductDetailsModal({ open, onClose, client }: ProductDetailsModalProps) {
  return (
    <Dialog open={open} onClose={(event, reason) => { if (reason === 'backdropClick') return; onClose(); }} maxWidth="md" fullWidth>
      <DialogTitle>
        <Typography variant="h5" sx={{ fontWeight: 600 }}>
          {client.clientName} - Product Details
        </Typography>
      </DialogTitle>
      <DialogContent>
        <Box sx={{ mb: 2, mt: 1 }}>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <Typography variant="body2" color="text.secondary">Total Quantity</Typography>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>{client.totalQuantity}</Typography>
            </Grid>
            <Grid item xs={6}>
              <Typography variant="body2" color="text.secondary">Total Revenue</Typography>
              <Typography variant="h6" sx={{ fontWeight: 600, color: '#1976d2' }}>
                {formatINR(client.totalRevenue)}
              </Typography>
            </Grid>
          </Grid>
        </Box>
        <Divider sx={{ my: 2 }} />
        <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>Products Sold</Typography>
        <TableContainer component={Paper} variant="outlined">
          <Table>
            <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 600 }}>Barcode</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Product Name</TableCell>
                <TableCell align="right" sx={{ fontWeight: 600 }}>Quantity</TableCell>
                <TableCell align="right" sx={{ fontWeight: 600 }}>Revenue</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {client.products.map((product, idx) => (
                <TableRow key={idx} hover>
                  <TableCell>{product.barcode}</TableCell>
                  <TableCell>{product.productName}</TableCell>
                  <TableCell align="right">{product.quantity}</TableCell>
                  <TableCell align="right">{formatINR(product.revenue)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} variant="contained">Close</Button>
      </DialogActions>
    </Dialog>
  );
}

export default function SalesReportPage() {
  const [tabValue, setTabValue] = useState(0);

  const [dailySales, setDailySales] = useState<DailySalesData[]>([]);
  const [selectedDate, setSelectedDate] = useState('');
  const [dailyClientId, setDailyClientId] = useState('');

  const [dateRangeReport, setDateRangeReport] = useState<ClientSalesReport[]>([]);
  const [rangeFromDate, setRangeFromDate] = useState('');
  const [rangeToDate, setRangeToDate] = useState('');
  const [rangeClientId, setRangeClientId] = useState('');

  const [loading, setLoading] = useState(false);
  const [clients, setClients] = useState<any[]>([]);

  const [modalOpen, setModalOpen] = useState(false);
  const [selectedClient, setSelectedClient] = useState<ClientSalesReport | null>(null);

  useEffect(() => {
    loadClients();
  }, []);

  const handleViewProducts = (client: ClientSalesReport) => {
    setSelectedClient(client);
    setModalOpen(true);
  };

  const loadClients = async () => {
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

      console.log('Loaded all clients:', allClients.length, 'clients');
      setClients(allClients);

      if (allClients.length === 0) {
        console.warn('No clients found in the system');
      }
    } catch (e: any) {
      console.error('Failed to load clients:', e);
      setClients([]);
    }
  };

  const clearDailyFilters = () => {
    setSelectedDate('');
    setDailyClientId('');
    setDailySales([]);
  };

  const clearRangeFilters = () => {
    setRangeFromDate('');
    setRangeToDate('');
    setRangeClientId('');
    setDateRangeReport([]);
  };

  const loadDailySales = async () => {
    if (!selectedDate) {
      toastError('Please select a date');
      return;
    }
    setLoading(true);
    try {
      const data = await reportService.getDailySales(
        selectedDate,
        dailyClientId || undefined
      );
      setDailySales(data);
      if (data.length === 0) {
        toastWarning('No sales data found for this date');
      } else {
        toastSuccess(`Loaded sales data.`);
      }
    } catch (e: any) {
      const errorMsg = e.response?.data?.message || e.message || 'Failed to load daily sales';
      toastError(errorMsg);
      setDailySales([]);
    } finally {
      setLoading(false);
    }
  };

  const loadDateRangeReport = async () => {
    if (!rangeFromDate || !rangeToDate) {
      toastError('Please select both start and end dates');
      return;
    }
    setLoading(true);
    try {
      const data = await reportService.getSalesReport(
        rangeFromDate,
        rangeToDate,
        rangeClientId || undefined
      );
      setDateRangeReport(data);
      if (data.length === 0) {
        toastWarning('No sales data found');
      } else {
        toastSuccess(`Loaded report for ${data.length} clients`);
      }
    } catch (e: any) {
      const errorMsg = e.response?.data?.message || e.message || 'Failed to load report';
      toastError(errorMsg);
      setDateRangeReport([]);
    } finally {
      setLoading(false);
    }
  };

  const dailyTotalOrders = dailySales.reduce((sum, row) => sum + row.invoicedOrdersCount, 0);
  const dailyTotalItems = dailySales.reduce((sum, row) => sum + row.invoicedItemsCount, 0);
  const dailyTotalRevenue = dailySales.reduce((sum, row) => sum + row.totalRevenue, 0);

  const rangeTotalOrders = dateRangeReport.reduce((sum, client) => sum + client.invoicedOrdersCount, 0);
  const rangeTotalQuantity = dateRangeReport.reduce((sum, client) => sum + client.totalQuantity, 0);
  const rangeTotalRevenue = dateRangeReport.reduce((sum, client) => sum + client.totalRevenue, 0);

  return (
    <AuthGuard requireSupervisor={true}>
      <StyledContainer maxWidth="lg">
        <HeaderBox>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>
            Sales Reports
          </Typography>
          <Typography variant="body2" sx={{ mt: 0.5, opacity: 0.9 }}>
            View daily aggregated sales or detailed date range reports
          </Typography>
        </HeaderBox>

        <Paper sx={{ borderRadius: '12px', mb: 2 }}>
          <Tabs value={tabValue} onChange={(_, newValue) => setTabValue(newValue)}>
            <Tab label="Day on Day Sales Report" />
            <Tab label="Date Range Report" />
          </Tabs>
        </Paper>

        {/* Daily Sales Tab - SINGLE DATE */}
        <TabPanel value={tabValue} index={0}>
          <SectionCard>
            <Typography variant="h6" sx={{ mb: 1, fontWeight: 600 }}>
              Select Date
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              View pre-aggregated sales data for a specific date
            </Typography>
            <Grid container spacing={2} sx={{ mb: 2 }}>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Date"
                  type="date"
                  size="small"
                  fullWidth
                  required
                  InputLabelProps={{ shrink: true }}
                  value={selectedDate}
                  onChange={(e) => setSelectedDate(e.target.value)}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Autocomplete
                  options={[{ clientId: '', name: 'All Clients' }, ...clients]}
                  getOptionLabel={(option) => option.name}
                  value={
                    dailyClientId === ''
                      ? { clientId: '', name: 'All Clients' }
                      : clients.find((c) => c.clientId === dailyClientId) || { clientId: '', name: 'All Clients' }
                  }
                  onChange={(_, newValue) => setDailyClientId(newValue?.clientId || '')}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Client"
                      size="small"
                    />
                  )}
                  isOptionEqualToValue={(option, value) => option.clientId === value.clientId}
                />
              </Grid>
            </Grid>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button variant="contained" onClick={loadDailySales} disabled={loading}>
                {loading ? <CircularProgress size={20} /> : 'View Sales'}
              </Button>
              <Button variant="outlined" onClick={clearDailyFilters}>
                Clear
              </Button>
            </Box>
          </SectionCard>

          {dailySales.length > 0 && (
            <>
              <Grid container spacing={2} sx={{ mb: 2 }}>
                <Grid item xs={12} md={4}>
                  <Card>
                    <CardContent>
                      <Typography color="text.secondary" variant="body2">Invoiced Orders</Typography>
                      <Typography variant="h4" sx={{ fontWeight: 700, color: '#1976d2' }}>
                        {dailyTotalOrders}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Card>
                    <CardContent>
                      <Typography color="text.secondary" variant="body2">Items (Qty)</Typography>
                      <Typography variant="h4" sx={{ fontWeight: 700, color: '#1976d2' }}>
                        {dailyTotalItems}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Card>
                    <CardContent>
                      <Typography color="text.secondary" variant="body2">Total Revenue</Typography>
                      <Typography variant="h4" sx={{ fontWeight: 700, color: '#1976d2' }}>
                        {formatINR(dailyTotalRevenue)}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>

              <SectionCard>
                <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                  Sales by Client on {formatDateWords(selectedDate)}
                </Typography>
                <TableContainer>
                  <Table>
                    <StyledTableHead>
                      <TableRow>
                        <TableCell>Client</TableCell>
                        <TableCell align="right">Orders</TableCell>
                        <TableCell align="right">Items (Qty)</TableCell>
                        <TableCell align="right">Revenue</TableCell>
                      </TableRow>
                    </StyledTableHead>
                    <TableBody>
                      {dailySales.map((row) => (
                        <TableRow key={row.id}>
                          <TableCell>{row.clientName}</TableCell>
                          <TableCell align="right">{row.invoicedOrdersCount}</TableCell>
                          <TableCell align="right">{row.invoicedItemsCount}</TableCell>
                          <TableCell align="right">{formatINR(row.totalRevenue)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </SectionCard>
            </>
          )}
        </TabPanel>

        {/* Date Range Report Tab - DETAILED WITH PRODUCTS */}
        <TabPanel value={tabValue} index={1}>
          <SectionCard>
            <Typography variant="h6" sx={{ mb: 1, fontWeight: 600 }}>
              Select Date Range
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              View detailed product-level sales breakdown for a date range
            </Typography>
            <Grid container spacing={2} sx={{ mb: 2 }}>
              <Grid item xs={12} sm={4}>
                <TextField
                  label="Start Date"
                  type="date"
                  size="small"
                  fullWidth
                  required
                  InputLabelProps={{ shrink: true }}
                  value={rangeFromDate}
                  onChange={(e) => setRangeFromDate(e.target.value)}
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  label="End Date"
                  type="date"
                  size="small"
                  fullWidth
                  required
                  InputLabelProps={{ shrink: true }}
                  value={rangeToDate}
                  onChange={(e) => setRangeToDate(e.target.value)}
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <Autocomplete
                  options={[{ clientId: '', name: 'All Clients' }, ...clients]}
                  getOptionLabel={(option) => option.name}
                  value={
                    rangeClientId === ''
                      ? { clientId: '', name: 'All Clients' }
                      : clients.find((c) => c.clientId === rangeClientId) || { clientId: '', name: 'All Clients' }
                  }
                  onChange={(_, newValue) => setRangeClientId(newValue?.clientId || '')}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Client"
                      size="small"
                    />
                  )}
                  isOptionEqualToValue={(option, value) => option.clientId === value.clientId}
                />
              </Grid>
            </Grid>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button variant="contained" onClick={loadDateRangeReport} disabled={loading}>
                {loading ? <CircularProgress size={20} /> : 'Generate Report'}
              </Button>
              <Button variant="outlined" onClick={clearRangeFilters}>
                Clear
              </Button>
            </Box>
          </SectionCard>

          {dateRangeReport.length > 0 && (
            <>
              <Grid container spacing={2} sx={{ mb: 2 }}>
                <Grid item xs={12} md={4}>
                  <Card>
                    <CardContent>
                      <Typography color="text.secondary" variant="body2">Invoiced Orders</Typography>
                      <Typography variant="h4" sx={{ fontWeight: 700, color: '#1976d2' }}>
                        {rangeTotalOrders}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Card>
                    <CardContent>
                      <Typography color="text.secondary" variant="body2"> Items (Qty)</Typography>
                      <Typography variant="h4" sx={{ fontWeight: 700, color: '#1976d2' }}>
                        {rangeTotalQuantity}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Card>
                    <CardContent>
                      <Typography color="text.secondary" variant="body2">Total Revenue</Typography>
                      <Typography variant="h4" sx={{ fontWeight: 700, color: '#1976d2' }}>
                        {formatINR(rangeTotalRevenue)}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>

              <SectionCard>
                <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                  Client Sales Breakdown ({formatDateWords(rangeFromDate)} – {formatDateWords(rangeToDate)})
                </Typography>
                <TableContainer>
                  <Table>
                    <StyledTableHead>
                      <TableRow>
                        <TableCell>Client</TableCell>
                        <TableCell align="right">Orders</TableCell>
                        <TableCell align="right">Items (Qty)</TableCell>
                        <TableCell align="right">Revenue</TableCell>
                        <TableCell align="center">Actions</TableCell>
                      </TableRow>
                    </StyledTableHead>
                    <TableBody>
                      {dateRangeReport.map((client) => (
                        <TableRow key={client.clientId} hover>
                          <TableCell>{client.clientName}</TableCell>
                          <TableCell align="right">{client.invoicedOrdersCount}</TableCell>
                          <TableCell align="right">{client.totalQuantity}</TableCell>
                          <TableCell align="right">{formatINR(client.totalRevenue)}</TableCell>
                          <TableCell align="center">
                            <Button
                              variant="outlined"
                              size="small"
                              startIcon={<VisibilityIcon />}
                              onClick={() => handleViewProducts(client)}
                            >
                              View Products
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </SectionCard>

              {selectedClient && (
                <ProductDetailsModal
                  open={modalOpen}
                  onClose={() => setModalOpen(false)}
                  client={selectedClient}
                />
              )}
            </>
          )}
        </TabPanel>
      </StyledContainer>
    </AuthGuard>
  );
}
