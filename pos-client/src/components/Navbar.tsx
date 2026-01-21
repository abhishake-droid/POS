import { AppBar, Toolbar, Typography, Button, Box, Chip } from '@mui/material';
import { useRouter } from 'next/router';
import { styled } from '@mui/material/styles';
import { useAuth } from '../contexts/AuthContext';
import { Logout } from '@mui/icons-material';

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
  const { user, logout, isSupervisor } = useAuth();

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
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <NavButton onClick={() => router.push('/')}>
            Home
          </NavButton>
          <NavButton onClick={() => router.push('/clients')}>
            Clients
          </NavButton>
          <NavButton onClick={() => router.push('/products')}>
            Products
          </NavButton>
          {isSupervisor && (
            <>
              <NavButton onClick={() => router.push('/supervisor-dashboard')}>
                Dashboard
              </NavButton>
              <NavButton onClick={() => router.push('/operators')}>
                Operators
              </NavButton>
            </>
          )}
          {user && (
            <>
              <Chip
                label={user.role}
                color={isSupervisor ? 'secondary' : 'default'}
                sx={{ color: 'white', fontWeight: 600 }}
              />
              <NavButton
                startIcon={<Logout />}
                onClick={() => {
                  logout().catch(console.error);
                }}
              >
                Logout
              </NavButton>
            </>
          )}
        </Box>
      </StyledToolbar>
    </StyledAppBar>
  );
}
