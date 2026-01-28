import { toast as reactToast, ToastOptions } from 'react-toastify';

// Success toasts auto-close after 3 seconds
export const toastSuccess = (message: string, options?: ToastOptions) => {
    reactToast.success(message, {
        autoClose: 3000,
        ...options,
    });
};

// Error toasts do NOT auto-close - user must manually dismiss
export const toastError = (message: string, options?: ToastOptions) => {
    reactToast.error(message, {
        autoClose: false,
        closeButton: true,
        ...options,
    });
};

// Warning toasts auto-close after 5 seconds
export const toastWarning = (message: string, options?: ToastOptions) => {
    reactToast.warning(message, {
        autoClose: 5000,
        ...options,
    });
};

// Info toasts auto-close after 4 seconds
export const toastInfo = (message: string, options?: ToastOptions) => {
    reactToast.info(message, {
        autoClose: 4000,
        ...options,
    });
};

// Re-export the original toast for any custom usage
export { reactToast as toast };
