import { useRouter } from 'next/router';
import { Button, Container, Box, Typography, Card, CardContent, Grid } from '@mui/material';
import { styled } from '@mui/material/styles';
import { People, Inventory, Dashboard, SupervisorAccount, ShoppingCart, Assessment } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

const StyledContainer = styled(Container)(({ theme }) => ({
  paddingTop: '3rem',
  paddingBottom: '3rem',
  minHeight: 'calc(100vh - 64px)',
}));

const StyledCard = styled(Card)(({ theme }) => ({
  height: '100%',
  borderRadius: '12px',
  boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
  transition: 'transform 0.3s ease, box-shadow 0.3s ease',
  display: 'flex',
  flexDirection: 'column',
  '&:hover': {
    transform: 'translateY(-8px)',
    boxShadow: '0 8px 20px rgba(0,0,0,0.15)',
  },
}));

const StyledButton = styled(Button)(({ theme }) => ({
  borderRadius: '8px',
  textTransform: 'none',
  fontWeight: 600,
  padding: '0.75rem 1.5rem',
  fontSize: '0.95rem',
  boxShadow: '0 2px 8px rgba(25, 118, 210, 0.3)',
  '&:hover': {
    boxShadow: '0 4px 12px rgba(25, 118, 210, 0.4)',
    transform: 'translateY(-2px)',
  },
}));

export default function Home() {
  const router = useRouter();
  const { isSupervisor } = useAuth();

  return (
    <StyledContainer maxWidth="lg">
      <Box sx={{ textAlign: 'center', mb: 5 }}>
        <Typography
          variant="h2"
          sx={{
            fontWeight: 700,
            background: 'linear-gradient(45deg, #1976d2 30%, #42a5f5 90%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            mb: 2,
          }}
        >
          POS System
        </Typography>
        <Typography variant="h6" color="text.secondary">
          Point of Sale Management System
        </Typography>
      </Box>

      {isSupervisor && (
        <Box sx={{ mb: 5 }}>
          <Typography variant="h5" sx={{ fontWeight: 600, mb: 3, color: '#1976d2' }}>
            Supervisor Tools
          </Typography>
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6}>
              <StyledCard>
                <CardContent sx={{
                  padding: '2.5rem !important',
                  textAlign: 'center',
                  display: 'flex',
                  flexDirection: 'column',
                  height: '100%',
                }}>
                  <Box sx={{ mb: 2 }}>
                    <Dashboard sx={{ fontSize: 56, color: '#1976d2' }} />
                  </Box>
                  <Typography variant="h5" sx={{ fontWeight: 600, mb: 1 }}>
                    Supervisor Dashboard
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 3, flexGrow: 1 }}>
                    Monitor operators and system activity
                  </Typography>
                  <StyledButton
                    variant="contained"
                    fullWidth
                    onClick={() => router.push('/supervisor-dashboard')}
                    startIcon={<Dashboard />}
                  >
                    Open Dashboard
                  </StyledButton>
                </CardContent>
              </StyledCard>
            </Grid>

            <Grid item xs={12} sm={6}>
              <StyledCard>
                <CardContent sx={{
                  padding: '2.5rem !important',
                  textAlign: 'center',
                  display: 'flex',
                  flexDirection: 'column',
                  height: '100%',
                }}>
                  <Box sx={{ mb: 2 }}>
                    <SupervisorAccount sx={{ fontSize: 56, color: '#1976d2' }} />
                  </Box>
                  <Typography variant="h5" sx={{ fontWeight: 600, mb: 1 }}>
                    Operator Management
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 3, flexGrow: 1 }}>
                    Create and manage system operators
                  </Typography>
                  <StyledButton
                    variant="contained"
                    fullWidth
                    onClick={() => router.push('/operators')}
                    startIcon={<SupervisorAccount />}
                  >
                    Manage Operators
                  </StyledButton>
                </CardContent>
              </StyledCard>
            </Grid>
          </Grid>
        </Box>
      )}

      <Box>
        <Typography variant="h5" sx={{ fontWeight: 600, mb: 3, color: '#1976d2' }}>
          Core Modules
        </Typography>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6} md={4}>
            <StyledCard>
              <CardContent sx={{
                padding: '2rem !important',
                textAlign: 'center',
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
              }}>
                <Box sx={{ mb: 2 }}>
                  <People sx={{ fontSize: 56, color: '#1976d2' }} />
                </Box>
                <Typography variant="h6" sx={{ fontWeight: 600, mb: 1, minHeight: 48 }}>
                  Client Management
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3, flexGrow: 1 }}>
                  Manage customer information
                </Typography>
                <StyledButton
                  variant="contained"
                  fullWidth
                  onClick={() => router.push('/clients')}
                  startIcon={<People />}
                >
                  Open
                </StyledButton>
              </CardContent>
            </StyledCard>
          </Grid>

          <Grid item xs={12} sm={6} md={4}>
            <StyledCard>
              <CardContent sx={{
                padding: '2rem !important',
                textAlign: 'center',
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
              }}>
                <Box sx={{ mb: 2 }}>
                  <Inventory sx={{ fontSize: 56, color: '#1976d2' }} />
                </Box>
                <Typography variant="h6" sx={{ fontWeight: 600, mb: 1, minHeight: 48 }}>
                  Product Management
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3, flexGrow: 1 }}>
                  Manage inventory and products
                </Typography>
                <StyledButton
                  variant="contained"
                  fullWidth
                  onClick={() => router.push('/products')}
                  startIcon={<Inventory />}
                >
                  Open
                </StyledButton>
              </CardContent>
            </StyledCard>
          </Grid>

          <Grid item xs={12} sm={6} md={4}>
            <StyledCard>
              <CardContent sx={{
                padding: '2rem !important',
                textAlign: 'center',
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
              }}>
                <Box sx={{ mb: 2 }}>
                  <ShoppingCart sx={{ fontSize: 56, color: '#1976d2' }} />
                </Box>
                <Typography variant="h6" sx={{ fontWeight: 600, mb: 1, minHeight: 48 }}>
                  Order Management
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3, flexGrow: 1 }}>
                  Create orders and generate invoices
                </Typography>
                <StyledButton
                  variant="contained"
                  fullWidth
                  onClick={() => router.push('/orders')}
                  startIcon={<ShoppingCart />}
                >
                  Open
                </StyledButton>
              </CardContent>
            </StyledCard>
          </Grid>

          {isSupervisor && (
            <Grid item xs={12} sm={6} md={4}>
              <StyledCard>
                <CardContent sx={{
                  padding: '2rem !important',
                  textAlign: 'center',
                  display: 'flex',
                  flexDirection: 'column',
                  height: '100%',
                }}>
                  <Box sx={{ mb: 2 }}>
                    <Assessment sx={{ fontSize: 56, color: '#1976d2' }} />
                  </Box>
                  <Typography variant="h6" sx={{ fontWeight: 600, mb: 1, minHeight: 48 }}>
                    Sales Report
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 3, flexGrow: 1 }}>
                    View comprehensive sales analytics
                  </Typography>
                  <StyledButton
                    variant="contained"
                    fullWidth
                    onClick={() => router.push('/sales-report')}
                    startIcon={<Assessment />}
                  >
                    Open
                  </StyledButton>
                </CardContent>
              </StyledCard>
            </Grid>
          )}
        </Grid>
      </Box>
    </StyledContainer>
  );
}
