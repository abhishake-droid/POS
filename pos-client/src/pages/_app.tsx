import type { AppProps } from 'next/app';
import { useRouter } from 'next/router';
import { useEffect } from 'react';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import '../styles/globals.css';
import { Box } from '@mui/material';
import Navbar from '../components/Navbar';
import AuthGuard from '../components/AuthGuard';
import { AuthProvider, useAuth } from '../contexts/AuthContext';

function AppContent({ Component, pageProps }: AppProps) {
  const { user, isLoading, checkAuth } = useAuth();
  const router = useRouter();

  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        checkAuth();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [checkAuth]);

  useEffect(() => {
    if (!isLoading && !user && router.pathname !== '/login') {
      router.push('/login');
    }
  }, [user, isLoading, router]);

  if (isLoading || (!user && router.pathname !== '/login')) {
    return null;
  }

  if (router.pathname === '/login') {
    return (
      <>
        <Component {...pageProps} />
        <ToastContainer position="top-right" autoClose={false} closeButton={true} />
      </>
    );
  }

  return (
    <AuthGuard>
      <Navbar />
      <Box sx={{ paddingTop: '64px' }}>
        <Component {...pageProps} />
      </Box>
      <ToastContainer position="top-right" />
    </AuthGuard>
  );
}

export default function App(props: AppProps) {
  return (
    <AuthProvider>
      <AppContent {...props} />
    </AuthProvider>
  );
}
