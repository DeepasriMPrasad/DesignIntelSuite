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
    let containerId = type === 'error' ? 'error-toast-container' : 'toast-container';
    let toastContainer = document.getElementById(containerId);
    
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = containerId;
        
        // Position error messages at top-center for maximum visibility
        if (type === 'error') {
            toastContainer.className = 'toast-container position-fixed top-0 start-50 translate-middle-x p-3 mt-4';
        } else {
            toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        }
        
        document.body.appendChild(toastContainer);
    }
    
    // Create toast element
    const toastId = `toast-${Date.now()}`;
    const toast = document.createElement('div');
    toast.id = toastId;
    toast.className = `toast fade show border-0 shadow-lg`;
    toast.role = 'alert';
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');
    
    // Set color scheme based on type
    let headerClass = 'bg-dark text-white';
    let bodyClass = 'bg-light';
    let icon = 'fa-info-circle';
    
    if (type === 'success') {
        headerClass = 'bg-success text-white';
        icon = 'fa-check-circle';
    } else if (type === 'error') {
        headerClass = 'bg-danger text-white';
        icon = 'fa-exclamation-circle';
        bodyClass = 'bg-danger bg-opacity-10 text-danger fw-bold'; // Higher contrast for error messages
        // Add styling for additional visibility
        toast.classList.add('border', 'border-danger');
        toast.style.minWidth = '350px'; // Ensure error messages have enough width
        toast.style.maxWidth = '80vw';  // But not too wide on mobile
    } else if (type === 'warning') {
        headerClass = 'bg-warning text-dark';
        icon = 'fa-exclamation-triangle';
    }
    
    // Toast content
    toast.innerHTML = `
        <div class="toast-header ${headerClass}">
            <i class="fas ${icon} me-2"></i>
            <strong class="me-auto">CXS Quiz System</strong>
            <button type="button" class="btn-close ${type === 'warning' ? '' : 'btn-close-white'}" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body ${bodyClass}">${message}</div>
    `;
    
    // Add to container
    toastContainer.appendChild(toast);
    
    // Log the message as well
    console.log('Toast message:', message, '(Type:', type, ')');
    
    try {
        // Initialize Bootstrap toast and show it
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
        
        // Show longer for error messages (8 seconds) than other types (5 seconds)
        const displayTime = type === 'error' ? 8000 : 5000;
        
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
        }, displayTime);
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