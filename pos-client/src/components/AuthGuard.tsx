import { ReactNode, useEffect } from 'react';
import { useRouter } from 'next/router';
import { useAuth } from '../contexts/AuthContext';
import { Box, CircularProgress, Typography } from '@mui/material';

interface AuthGuardProps {
  children: ReactNode;
  requiredRole?: 'SUPERVISOR' | 'USER';
  requireSupervisor?: boolean;
}

export default function AuthGuard({ 
  children, 
  requiredRole, 
  requireSupervisor = false 
}: AuthGuardProps) {
  const { user, isLoading, isSupervisor } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading) {
      // If no user, redirect to login
      if (!user) {
        router.push('/login');
        return;
      }

      // Check role requirements
      if (requireSupervisor && !isSupervisor) {
        router.push('/');
        return;
      }

      if (requiredRole === 'SUPERVISOR' && !isSupervisor) {
        router.push('/');
        return;
      }
    }
  }, [user, isLoading, isSupervisor, router, requiredRole, requireSupervisor]);

  // Show loading while checking auth
  if (isLoading) {
    return (
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="100vh"
      >
        <CircularProgress />
        <Typography variant="body2" sx={{ mt: 2 }}>
          Checking authentication...
        </Typography>
      </Box>
    );
  }

  // If no user, don't render children (will redirect)
  if (!user) {
    return null;
  }

  // Check role requirements
  if (requireSupervisor && !isSupervisor) {
    return (
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="100vh"
      >
        <Typography variant="h5" color="error">
          Access Denied
        </Typography>
        <Typography variant="body1" sx={{ mt: 2 }}>
          This page requires supervisor privileges.
        </Typography>
      </Box>
    );
  }

  if (requiredRole === 'SUPERVISOR' && !isSupervisor) {
    return (
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="100vh"
      >
        <Typography variant="h5" color="error">
          Access Denied
        </Typography>
        <Typography variant="body1" sx={{ mt: 2 }}>
          This page requires supervisor privileges.
        </Typography>
      </Box>
    );
  }

  // User has required role, render children
  return <>{children}</>;
}
