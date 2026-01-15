import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { useRouter } from 'next/router';
import { styled } from '@mui/material/styles';

const StyledAppBar = styled(AppBar)(({ theme }) => ({
  backgroundColor: '#1976d2',
  boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
  top: 0,
  zIndex: 1100,
  width: '100%',
}));

const StyledToolbar = styled(Toolbar)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'space-between',
  padding: '0.75rem 2rem',
}));

const NavButton = styled(Button)(({ theme }) => ({
  color: 'white',
  fontWeight: 500,
  textTransform: 'none',
  fontSize: '1rem',
  padding: '0.5rem 1.5rem',
  borderRadius: '8px',
  transition: 'all 0.3s ease',
  '&:hover': {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    transform: 'translateY(-2px)',
  },
}));

export default function Navbar() {
  const router = useRouter();

  return (
    <StyledAppBar position="sticky">
      <StyledToolbar>
        <Typography
          variant="h5"
          component="div"
          sx={{
            fontWeight: 600,
            cursor: 'pointer',
            letterSpacing: '0.5px',
          }}
          onClick={() => router.push('/')}
        >
          POS System
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <NavButton onClick={() => router.push('/')}>
            Home
          </NavButton>
          <NavButton onClick={() => router.push('/clients')}>
            Clients
          </NavButton>
        </Box>
      </StyledToolbar>
    </StyledAppBar>
  );
}
