import { createTheme } from '@mui/material/styles';

// Professional Navy + Teal Color Scheme
const theme = createTheme({
    palette: {
        primary: {
            main: '#1e3a8a',      // Deep Navy - Professional, trustworthy
            light: '#3b82f6',     // Lighter navy
            dark: '#1e40af',      // Darker navy
            contrastText: '#ffffff',
        },
        secondary: {
            main: '#0d9488',      // Teal - Modern, fresh
            light: '#14b8a6',     // Lighter teal
            dark: '#0f766e',      // Darker teal
            contrastText: '#ffffff',
        },
        error: {
            main: '#dc2626',      // Red
            light: '#ef4444',
            dark: '#b91c1c',
        },
        warning: {
            main: '#d97706',      // Amber
            light: '#f59e0b',
            dark: '#b45309',
        },
        info: {
            main: '#0891b2',      // Cyan
            light: '#06b6d4',
            dark: '#0e7490',
        },
        success: {
            main: '#059669',      // Emerald - Money, growth
            light: '#10b981',
            dark: '#047857',
        },
        background: {
            default: '#f8fafc',   // Cool gray - Clean background
            paper: '#ffffff',     // White - Cards, dialogs
        },
        text: {
            primary: '#0f172a',   // Slate - High contrast
            secondary: '#64748b', // Medium slate
        },
    },
    typography: {
        fontFamily: [
            '-apple-system',
            'BlinkMacSystemFont',
            '"Segoe UI"',
            'Roboto',
            '"Helvetica Neue"',
            'Arial',
            'sans-serif',
        ].join(','),
        h1: {
            fontWeight: 700,
            color: '#0f172a',
        },
        h2: {
            fontWeight: 700,
            color: '#0f172a',
        },
        h3: {
            fontWeight: 600,
            color: '#0f172a',
        },
        h4: {
            fontWeight: 600,
            color: '#0f172a',
        },
        h5: {
            fontWeight: 600,
            color: '#0f172a',
        },
        h6: {
            fontWeight: 600,
            color: '#0f172a',
        },
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    textTransform: 'none',
                    borderRadius: 8,
                    fontWeight: 600,
                    padding: '8px 20px',
                },
                contained: {
                    boxShadow: 'none',
                    '&:hover': {
                        boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
                    },
                },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    borderRadius: 12,
                    boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)',
                },
            },
        },
        MuiPaper: {
            styleOverrides: {
                root: {
                    borderRadius: 12,
                },
            },
        },
        MuiAppBar: {
            styleOverrides: {
                root: {
                    boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)',
                },
            },
        },
        MuiTableCell: {
            styleOverrides: {
                head: {
                    fontWeight: 600,
                    backgroundColor: '#f1f5f9',
                },
            },
        },
        MuiPaginationItem: {
            styleOverrides: {
                root: {
                    '&.Mui-selected': {
                        backgroundColor: '#1e3a8a',
                        color: '#ffffff',
                        '&:hover': {
                            backgroundColor: '#1e40af',
                        },
                    },
                },
            },
        },
    },
});

export default theme;
