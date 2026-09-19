/* RublinMart Checkout & Mock Payment Client */

async function loadCheckoutPage() {
    const summaryContainer = document.getElementById('checkout-items-summary');
    const totalDisplay = document.getElementById('checkout-total-amount');
    if (!summaryContainer) return;

    try {
        const res = await fetch(`${API_BASE}/cart`);
        const json = await res.json();

        if (json.success && json.data) {
            const cart = json.data;
            if (cart.items.length === 0) {
                window.location.href = '/rublinmart/pages/cart.html';
                return;
            }

            summaryContainer.innerHTML = cart.items.map(item => `
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem; padding-bottom:0.75rem; border-bottom:1px solid var(--border);">
                    <div style="display:flex; gap:1rem; align-items:center;">
                        <img src="${item.imageUrl}" style="width:50px; height:50px; object-fit:cover; border-radius:6px;" onerror="this.src='https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500'">
                        <div>
                            <div style="font-weight:600; color:var(--secondary);">${item.productName}</div>
                            <div style="font-size:0.85rem; color:var(--text-muted);">Qty: ${item.quantity} × $${parseFloat(item.productPrice).toFixed(2)}</div>
                        </div>
                    </div>
                    <div style="font-weight:700;">$${(item.productPrice * item.quantity).toFixed(2)}</div>
                </div>
            `).join('');

            if (totalDisplay) {
                totalDisplay.textContent = `$${parseFloat(cart.totalAmount).toFixed(2)}`;
            }
        }
    } catch (e) {
        console.error("Error loading checkout page", e);
    }
}

async function handlePlaceOrder(event) {
    event.preventDefault();
    const btn = document.getElementById('place-order-btn');
    const alertBox = document.getElementById('checkout-error');

    if (alertBox) alertBox.style.display = 'none';
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing Mock Payment...';
    }

    try {
        // Simulate quick mock payment validation delay
        await new Promise(r => setTimeout(r, 1200));

        const res = await fetch(`${API_BASE}/orders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        const json = await res.json();

        if (json.success) {
            const order = json.data;
            sessionStorage.setItem('lastOrder', JSON.stringify(order));
            window.location.href = `/rublinmart/pages/order-success.html?id=${order.id}`;
        } else {
            if (alertBox) {
                alertBox.textContent = json.error ? json.error.message : 'Checkout failed.';
                alertBox.style.display = 'block';
            }
            if (btn) {
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-lock"></i> Confirm & Pay Now';
            }
        }
    } catch (e) {
        if (alertBox) {
            alertBox.textContent = 'Server connection error during payment processing.';
            alertBox.style.display = 'block';
        }
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-lock"></i> Confirm & Pay Now';
        }
    }
}

function loadOrderSuccessPage() {
    const container = document.getElementById('order-success-details');
    if (!container) return;

    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('id');

    const lastOrderStr = sessionStorage.getItem('lastOrder');
    let orderData = null;

    if (lastOrderStr) {
        try { orderData = JSON.parse(lastOrderStr); } catch(e){}
    }

    if (orderId || orderData) {
        const idToDisplay = orderId || (orderData ? orderData.id : 'N/A');
        const amountDisplay = orderData ? `$${parseFloat(orderData.totalAmount).toFixed(2)}` : 'Confirmed';

        container.innerHTML = `
            <div style="text-align:center; padding:3rem 1.5rem;" class="cart-table-card">
                <i class="fas fa-check-circle" style="font-size:4.5rem; color:var(--success); margin-bottom:1.5rem;"></i>
                <h1 style="color:var(--secondary); margin-bottom:0.5rem;">Order Successfully Placed!</h1>
                <p style="color:var(--text-muted); font-size:1.1rem; margin-bottom:2rem;">Thank you for shopping on RublinMart. Your payment was verified.</p>
                
                <div style="background:#f8fafc; border:1px solid var(--border); border-radius:var(--radius); padding:1.5rem; max-width:500px; margin:0 auto 2rem; text-align:left;">
                    <div style="margin-bottom:0.75rem;"><strong>Order Reference #:</strong> RUB-${idToDisplay}</div>
                    <div style="margin-bottom:0.75rem;"><strong>Status:</strong> <span class="badge badge-confirmed">CONFIRMED</span></div>
                    <div><strong>Total Paid:</strong> <span style="font-weight:700; color:var(--primary);">${amountDisplay}</span></div>
                </div>

                <div style="display:flex; justify-content:center; gap:1rem;">
                    <a href="/rublinmart/pages/orders.html" class="btn btn-primary"><i class="fas fa-box"></i> View Order History</a>
                    <a href="/rublinmart/pages/products.html" class="btn btn-outline">Continue Shopping</a>
                </div>
            </div>
        `;
    }
}

async function loadBuyerOrdersPage() {
    const tableBody = document.getElementById('buyer-orders-body');
    if (!tableBody) return;

    try {
        const res = await fetch(`${API_BASE}/orders`);
        const json = await res.json();

        if (json.success && json.data) {
            if (json.data.length === 0) {
                document.getElementById('orders-view-container').innerHTML = `
                    <div class="empty-state">
                        <i class="fas fa-receipt"></i>
                        <h2>No Previous Orders</h2>
                        <p style="color:var(--text-muted); margin-bottom:1.5rem;">You haven't placed any orders on RublinMart yet.</p>
                        <a href="/rublinmart/pages/products.html" class="btn btn-primary">Browse Products</a>
                    </div>`;
                return;
            }

            tableBody.innerHTML = json.data.map(order => {
                const badgeClass = `badge-${order.status.toLowerCase()}`;
                const itemsListHtml = order.items.map(i => `
                    <div style="display:flex; align-items:center; gap:0.5rem; margin-bottom:0.3rem;">
                        <img src="${i.productImage}" style="width:30px; height:30px; object-fit:cover; border-radius:4px;" onerror="this.src='https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500'">
                        <span>${i.productName} (x${i.quantity})</span>
                        <a href="/rublinmart/pages/product-details.html?id=${i.productId}#review-section" style="font-size:0.75rem; margin-left:auto; color:var(--primary); font-weight:600;">Rate & Review</a>
                    </div>
                `).join('');

                return `
                <tr>
                    <td><strong>RUB-${order.id}</strong></td>
                    <td>${order.createdAt}</td>
                    <td>${itemsListHtml}</td>
                    <td style="font-weight:700; color:var(--primary);">$${parseFloat(order.totalAmount).toFixed(2)}</td>
                    <td><span class="badge ${badgeClass}">${order.status}</span></td>
                </tr>`;
            }).join('');
        }
    } catch (e) {
        console.error("Failed to load buyer orders", e);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadCheckoutPage();
    loadOrderSuccessPage();
    loadBuyerOrdersPage();
});
