/* RublinMart Shopping Cart Client */

async function updateCartBadge() {
    const badge = document.getElementById('cart-badge-count');
    if (!badge) return;

    try {
        const res = await fetch(`${API_BASE}/cart`);
        const json = await res.json();
        if (json.success && json.data) {
            badge.textContent = json.data.itemCount || 0;
        } else {
            badge.textContent = 0;
        }
    } catch (e) {
        badge.textContent = 0;
    }
}

async function addToCart(productId, quantity = 1) {
    try {
        const res = await fetch(`${API_BASE}/cart/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId, quantity })
        });
        const json = await res.json();

        if (json.success) {
            updateCartBadge();
            alert('Item added to your shopping cart!');
        } else {
            if (json.error && json.error.code === 'UNAUTHORIZED') {
                alert('Please log in to add products to your cart.');
                window.location.href = '/rublinmart/pages/login.html';
            } else {
                alert(json.error ? json.error.message : 'Could not add item to cart.');
            }
        }
    } catch (e) {
        alert('Server error adding item to cart.');
    }
}

async function loadCartPage() {
    const tableBody = document.getElementById('cart-items-body');
    const summaryCard = document.getElementById('cart-summary-view');
    if (!tableBody) return;

    try {
        const res = await fetch(`${API_BASE}/cart`);
        const json = await res.json();

        if (json.success && json.data) {
            const cart = json.data;
            if (cart.items.length === 0) {
                document.getElementById('cart-view-container').innerHTML = `
                    <div class="empty-state">
                        <i class="fas fa-shopping-basket"></i>
                        <h2>Your Shopping Cart is Empty</h2>
                        <p style="color:var(--text-muted); margin-bottom:1.5rem;">Explore our marketplace and add some great products!</p>
                        <a href="/rublinmart/pages/products.html" class="btn btn-primary">Start Shopping</a>
                    </div>`;
                return;
            }

            tableBody.innerHTML = cart.items.map(item => `
                <tr>
                    <td>
                        <div style="display:flex; align-items:center; gap:1rem;">
                            <img src="${item.imageUrl}" style="width:60px; height:60px; object-fit:cover; border-radius:6px;" onerror="this.src='https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500'">
                            <div>
                                <a href="/rublinmart/pages/product-details.html?id=${item.productId}" style="font-weight:600; color:var(--secondary);">${item.productName}</a>
                                <div style="font-size:0.8rem; color:var(--text-muted);">${item.category}</div>
                            </div>
                        </div>
                    </td>
                    <td style="font-weight:600;">$${parseFloat(item.productPrice).toFixed(2)}</td>
                    <td>
                        <div class="qty-control">
                            <button class="qty-btn" onclick="updateCartItemQty(${item.productId}, ${item.quantity - 1})">-</button>
                            <input type="number" class="qty-input" value="${item.quantity}" readonly>
                            <button class="qty-btn" onclick="updateCartItemQty(${item.productId}, ${item.quantity + 1})">+</button>
                        </div>
                    </td>
                    <td style="font-weight:700; color:var(--primary);">$${(item.productPrice * item.quantity).toFixed(2)}</td>
                    <td>
                        <button onclick="removeCartItem(${item.productId})" class="btn btn-sm btn-danger"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `).join('');

            if (summaryCard) {
                summaryCard.innerHTML = `
                    <h3 style="margin-bottom:1.5rem; color:var(--secondary);">Order Summary</h3>
                    <div class="summary-row">
                        <span>Items (${cart.itemCount}):</span>
                        <span>$${parseFloat(cart.subtotal).toFixed(2)}</span>
                    </div>
                    <div class="summary-row">
                        <span>Standard Shipping:</span>
                        <span style="color:var(--success); font-weight:600;">FREE</span>
                    </div>
                    <div class="summary-row total">
                        <span>Total:</span>
                        <span>$${parseFloat(cart.totalAmount).toFixed(2)}</span>
                    </div>
                    <a href="/rublinmart/pages/checkout.html" class="btn btn-primary" style="width:100%; margin-top:1.5rem; font-size:1.1rem;">
                        Proceed to Checkout <i class="fas fa-arrow-right"></i>
                    </a>
                    <button onclick="clearCart()" class="btn btn-sm btn-secondary" style="width:100%; margin-top:0.75rem;">Clear Cart</button>
                `;
            }
        }
    } catch (e) {
        console.error("Error loading cart page", e);
    }
}

async function updateCartItemQty(productId, newQty) {
    try {
        const res = await fetch(`${API_BASE}/cart/update`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId, quantity: newQty })
        });
        const json = await res.json();

        if (json.success) {
            updateCartBadge();
            loadCartPage();
        } else {
            alert(json.error ? json.error.message : 'Cannot update quantity');
        }
    } catch (e) {
        alert('Server error updating item quantity.');
    }
}

async function removeCartItem(productId) {
    if (!confirm('Remove this product from your cart?')) return;
    try {
        const res = await fetch(`${API_BASE}/cart/remove`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId })
        });
        const json = await res.json();

        if (json.success) {
            updateCartBadge();
            loadCartPage();
        }
    } catch (e) {
        alert('Server error removing item.');
    }
}

async function clearCart() {
    if (!confirm('Clear all items from your shopping cart?')) return;
    try {
        const res = await fetch(`${API_BASE}/cart`, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) {
            updateCartBadge();
            loadCartPage();
        }
    } catch (e) {
        alert('Server error clearing cart.');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    updateCartBadge();
    loadCartPage();
});
