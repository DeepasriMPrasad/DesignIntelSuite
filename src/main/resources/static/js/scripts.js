/**
 * Quiz Master JavaScript Utilities
 */

// Global error handler for Axios
axios.interceptors.response.use(
    response => response,
    error => {
        // Log the error
        console.error('API Error:', error);
        
        // Check for specific error types
        if (!error.response) {
            console.error('Network error: No response from server');
        } else {
            switch (error.response.status) {
                case 400:
                    console.error('Bad request:', error.response.data);
                    break;
                case 401:
                    console.error('Unauthorized access:', error.response.data);
                    break;
                case 404:
                    console.error('Resource not found:', error.response.data);
                    break;
                case 500:
                    console.error('Server error:', error.response.data);
                    break;
                default:
                    console.error(`HTTP Error ${error.response.status}:`, error.response.data);
            }
        }
        
        // Re-throw to allow component-specific error handling
        return Promise.reject(error);
    }
);

// Utility to show a toast notification
function showToast(message, type = 'info') {
    // Simple fallback if bootstrap isn't available
    if (typeof bootstrap === 'undefined') {
        console.log('Toast message:', message, '(Type:', type, ')');
        alert(message);
        return;
    }
    
    // Check if the toast container exists, create if not
    let toastContainer = document.getElementById('toast-container');
    
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }
    
    // Create toast element
    const toastId = `toast-${Date.now()}`;
    const toast = document.createElement('div');
    toast.id = toastId;
    toast.className = `toast fade show bg-${type}`;
    toast.role = 'alert';
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');
    
    // Toast content
    toast.innerHTML = `
        <div class="toast-header">
            <strong class="me-auto">Quiz Master</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body text-white">${message}</div>
    `;
    
    // Add to container
    toastContainer.appendChild(toast);
    
    // Log the message as well
    console.log('Toast message:', message, '(Type:', type, ')');
    
    try {
        // Initialize Bootstrap toast and show it
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
        
        // Remove after 5 seconds
        setTimeout(() => {
            try {
                bsToast.hide();
            } catch (err) {
                toast.remove();
            }
            setTimeout(() => {
                try {
                    toast.remove();
                } catch (err) {
                    console.error('Failed to remove toast:', err);
                }
            }, 500);
        }, 5000);
    } catch (error) {
        console.error('Failed to show toast:', error);
        // Fallback
        alert(message);
        try {
            toast.remove();
        } catch (err) {
            // Ignore
        }
    }
}