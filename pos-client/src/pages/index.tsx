import { useRouter } from 'next/router';
import { Button, Container, Box, Typography, Card, CardContent } from '@mui/material';
import { styled } from '@mui/material/styles';
import { People } from '@mui/icons-material';

const StyledContainer = styled(Container)(({ theme }) => ({
  paddingTop: '4rem',
  paddingBottom: '4rem',
  minHeight: 'calc(100vh - 64px)',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
}));

const StyledCard = styled(Card)(({ theme }) => ({
  maxWidth: 600,
  borderRadius: '16px',
  boxShadow: '0 8px 24px rgba(0,0,0,0.12)',
  transition: 'transform 0.3s ease, box-shadow 0.3s ease',
  '&:hover': {
    transform: 'translateY(-8px)',
    boxShadow: '0 12px 32px rgba(0,0,0,0.18)',
  },
}));

const StyledButton = styled(Button)(({ theme }) => ({
  borderRadius: '12px',
  textTransform: 'none',
  fontWeight: 600,
  padding: '1rem 2rem',
  fontSize: '1.1rem',
  boxShadow: '0 4px 12px rgba(25, 118, 210, 0.3)',
  '&:hover': {
    boxShadow: '0 6px 16px rgba(25, 118, 210, 0.4)',
    transform: 'translateY(-2px)',
    transition: 'all 0.2s ease',
  },
}));

export default function Home() {
  const router = useRouter();

  return (
    <StyledContainer>
      <Box sx={{ textAlign: 'center', mb: 4 }}>
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
        <Typography variant="h6" color="text.secondary" sx={{ mb: 4 }}>
          Point of Sale Management System
        </Typography>
      </Box>

      <StyledCard>
        <CardContent sx={{ padding: '3rem !important' }}>
          <Box sx={{ textAlign: 'center', mb: 3 }}>
            <People sx={{ fontSize: 64, color: '#1976d2', mb: 2 }} />
            <Typography variant="h5" sx={{ fontWeight: 600, mb: 1 }}>
              Client Management
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
            <StyledButton
              variant="contained"
              size="large"
              onClick={() => router.push('/clients')}
              startIcon={<People />}
            >
              Go to Clients
            </StyledButton>
          </Box>
        </CardContent>
      </StyledCard>
    </StyledContainer>
  );
}
