import { useState } from 'react';
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
} from '@mui/material';
import { styled } from '@mui/material/styles';
import { toast } from 'react-toastify';
import { SalesReportRow } from '../types/order.types';
import { reportService } from '../services/report.service';

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

const SectionCard = styled(Paper)({
  padding: '1.5rem',
  borderRadius: '12px',
  boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
  marginBottom: '1.5rem',
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

export default function SalesReportPage() {
  const [reportRows, setReportRows] = useState<SalesReportRow[]>([]);
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [brand, setBrand] = useState('');
  const [loading, setLoading] = useState(false);

  const loadSalesReport = async () => {
    if (!fromDate || !toDate) {
      toast.error('Please select both start and end dates for the report');
      return;
    }
    setLoading(true);
    try {
      const rows = await reportService.getSalesReport(
        fromDate,
        toDate,
        brand || undefined
      );
      setReportRows(rows);
      if (rows.length === 0) {
        toast.info('No sales data found for the selected criteria');
      }
    } catch (e: any) {
      const errorMsg = e.response?.data?.message || e.message || 'Failed to load sales report';
      if (e.response?.status === 404) {
        toast.error('Sales report endpoint is not available yet. Backend needs to be implemented.');
      } else {
        toast.error(errorMsg);
      }
      setReportRows([]);
    } finally {
      setLoading(false);
    }
  };

  const totalQuantity = reportRows.reduce((sum, row) => sum + row.quantitySum, 0);
  const totalRevenue = reportRows.reduce((sum, row) => sum + row.revenueSum, 0);

  return (
    <StyledContainer maxWidth="lg">
      <HeaderBox>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5, color: '#111827' }}>
            Sales Report
          </Typography>
          <Typography variant="body2" sx={{ color: '#6b7280' }}>
            Generate sales reports by date range and brand.
          </Typography>
        </Box>
      </HeaderBox>

      <SectionCard>
        <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
          Report Filters
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
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
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
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              label="Brand (Optional)"
              size="small"
              fullWidth
              value={brand}
              onChange={(e) => setBrand(e.target.value)}
              placeholder="Filter by brand"
            />
          </Grid>
        </Grid>
        <Button variant="contained" onClick={loadSalesReport} disabled={loading} sx={{ borderRadius: '999px', px: 3, py: 1 }}>
          {loading ? <CircularProgress size={20} /> : 'Generate Report'}
        </Button>
      </SectionCard>

      {reportRows.length > 0 && (
        <SectionCard>
          <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
            Report Results
          </Typography>
          <TableContainer>
            <Table>
              <StyledTableHead>
                <TableRow>
                  <TableCell>Brand</TableCell>
                  <TableCell>Category</TableCell>
                  <TableCell align="right">Total Quantity</TableCell>
                  <TableCell align="right">Total Revenue</TableCell>
                </TableRow>
              </StyledTableHead>
              <TableBody>
                {reportRows.map((row, idx) => (
                  <StyledTableRow key={idx}>
                    <TableCell>{row.brand || '-'}</TableCell>
                    <TableCell>{row.category || '-'}</TableCell>
                    <TableCell align="right">{row.quantitySum}</TableCell>
                    <TableCell align="right">
                      ₹{row.revenueSum.toFixed(2)}
                    </TableCell>
                  </StyledTableRow>
                ))}
                <StyledTableRow sx={{ backgroundColor: '#e3f2fd', fontWeight: 600 }}>
                  <TableCell colSpan={2} sx={{ fontWeight: 600 }}>
                    <strong>Total</strong>
                  </TableCell>
                  <TableCell align="right" sx={{ fontWeight: 600 }}>
                    <strong>{totalQuantity}</strong>
                  </TableCell>
                  <TableCell align="right" sx={{ fontWeight: 600 }}>
                    <strong>₹{totalRevenue.toFixed(2)}</strong>
                  </TableCell>
                </StyledTableRow>
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
      )}

      {!loading && reportRows.length === 0 && fromDate && toDate && (
        <SectionCard>
          <Box p={4} textAlign="center">
            <Typography variant="body1" color="text.secondary">
              No sales data found for the selected date range and filters.
            </Typography>
          </Box>
        </SectionCard>
      )}
    </StyledContainer>
  );
}
