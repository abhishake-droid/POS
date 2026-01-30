import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  Chip,
  Menu,
  MenuItem,
  IconButton,
  Divider,
} from '@mui/material';
import { useRouter } from 'next/router';
import { styled } from '@mui/material/styles';
import { useAuth } from '../contexts/AuthContext';
import {
  Logout,
  Dashboard,
  SupervisorAccount,
  ExpandMore,
  AccountCircle,
} from '@mui/icons-material';
import { useState } from 'react';

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
  padding: '0.75rem 2.5rem',
  minHeight: '64px',
}));

const NavButton = styled(Button)(({ theme }) => ({
  color: 'white',
  fontWeight: 500,
  textTransform: 'none',
  fontSize: '0.95rem',
  padding: '0.5rem 1.2rem',
  borderRadius: '8px',
  transition: 'all 0.3s ease',
  '&:hover': {
    backgroundColor: 'rgba(255, 255, 255, 0.15)',
    transform: 'translateY(-2px)',
  },
}));

export default function Navbar() {
  const router = useRouter();
  const { user, logout, isSupervisor } = useAuth();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [userMenuAnchor, setUserMenuAnchor] = useState<null | HTMLElement>(null);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleUserMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setUserMenuAnchor(event.currentTarget);
  };

  const handleUserMenuClose = () => {
    setUserMenuAnchor(null);
  };

  const handleNavigation = (path: string) => {
    router.push(path);
    handleMenuClose();
  };

  return (
    <StyledAppBar position="fixed">
      <StyledToolbar>
        <Typography
          variant="h5"
          component="div"
          sx={{
            fontWeight: 700,
            cursor: 'pointer',
            letterSpacing: '0.5px',
          }}
          onClick={() => router.push('/')}
        >
          POS System
        </Typography>

        <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center' }}>
          <NavButton onClick={() => router.push('/')}>
            Home
          </NavButton>
          <NavButton onClick={() => router.push('/clients')}>
            Clients
          </NavButton>
          <NavButton onClick={() => router.push('/products')}>
            Products
          </NavButton>
          <NavButton onClick={() => router.push('/orders')}>
            Orders
          </NavButton>
          {isSupervisor && (
            <NavButton onClick={() => router.push('/sales-report')}>
              Sales Report
            </NavButton>
          )}

          {isSupervisor && (
            <>
              <NavButton
                endIcon={<ExpandMore />}
                onClick={handleMenuOpen}
              >
                Supervisor
              </NavButton>

              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleMenuClose}
                PaperProps={{
                  sx: {
                    mt: 1,
                    borderRadius: '8px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                    minWidth: 180,
                  }
                }}
              >
                <MenuItem
                  onClick={() => handleNavigation('/supervisor-dashboard')}
                  sx={{ py: 1.25, px: 2 }}
                >
                  <Dashboard sx={{ mr: 1.5, fontSize: 20, color: '#1976d2' }} />
                  Dashboard
                </MenuItem>
                <MenuItem
                  onClick={() => handleNavigation('/operators')}
                  sx={{ py: 1.25, px: 2 }}
                >
                  <SupervisorAccount sx={{ mr: 1.5, fontSize: 20, color: '#1976d2' }} />
                  Operators
                </MenuItem>
              </Menu>
            </>
          )}

          {user && (
            <>
              <Divider orientation="vertical" flexItem sx={{ bgcolor: 'rgba(255,255,255,0.3)', mx: 1 }} />

              <IconButton
                onClick={handleUserMenuOpen}
                sx={{
                  color: 'white',
                  '&:hover': {
                    bgcolor: 'rgba(255, 255, 255, 0.15)',
                  }
                }}
              >
                <AccountCircle sx={{ fontSize: 32 }} />
              </IconButton>

              <Menu
                anchorEl={userMenuAnchor}
                open={Boolean(userMenuAnchor)}
                onClose={handleUserMenuClose}
                PaperProps={{
                  sx: {
                    mt: 1,
                    borderRadius: '8px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                    minWidth: 220,
                  }
                }}
              >
                <Box sx={{ px: 2, py: 1.5 }}>
                  <Chip
                    label={isSupervisor ? 'SUPERVISOR' : 'OPERATOR'}
                    color={isSupervisor ? 'secondary' : 'primary'}
                    size="small"
                    sx={{ fontWeight: 600, mb: 1 }}
                  />
                  <Typography variant="body2" sx={{ fontWeight: 600, mt: 0.5 }}>
                    {user.name || 'User'}
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#666' }}>
                    {user.email}
                  </Typography>
                </Box>
                <Divider />
                <MenuItem
                  onClick={() => {
                    handleUserMenuClose();
                    logout().catch(console.error);
                  }}
                  sx={{ py: 1.25, px: 2, color: '#d32f2f' }}
                >
                  <Logout sx={{ mr: 1.5, fontSize: 20 }} />
                  Logout
                </MenuItem>
              </Menu>

              {/* <IconButton
                onClick={() => {
                  logout().catch(console.error);
                }}
                sx={{
                  color: 'white',
                  '&:hover': {
                    bgcolor: 'rgba(255, 255, 255, 0.1)',
                  },
                }}
              >
                <Logout />
              </IconButton> */}
            </>
          )}
        </Box>
      </StyledToolbar>
    </StyledAppBar>
  );
}
