/* RublinMart Seller Studio Client */

let currentEditProductId = null;

async function loadSellerDashboard() {
    const productsTable = document.getElementById('seller-products-body');
    const ordersTable = document.getElementById('seller-orders-body');
    if (!productsTable) return;

    try {
        const [prodRes, orderRes] = await Promise.all([
            fetch(`${API_BASE}/seller/products`),
            fetch(`${API_BASE}/seller/orders`)
        ]);

        const prodJson = await prodRes.json();
        const orderJson = await orderRes.json();

        if (prodJson.success && prodJson.data) {
            const products = prodJson.data;
            document.getElementById('stat-total-products').textContent = products.length;

            if (products.length === 0) {
                productsTable.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:2rem; color:var(--text-muted);">No products listed yet. Click "Add New Product" to create your first listing!</td></tr>`;
            } else {
                productsTable.innerHTML = products.map(p => `
                    <tr>
                        <td>
                            <div style="display:flex; align-items:center; gap:0.75rem;">
                                <img src="${p.imageUrl}" style="width:40px; height:40px; object-fit:cover; border-radius:4px;" onerror="this.src='https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500'">
                                <strong>${p.name}</strong>
                            </div>
                        </td>
                        <td><span class="badge badge-confirmed">${p.category}</span></td>
                        <td style="font-weight:600;">$${parseFloat(p.price).toFixed(2)}</td>
                        <td>${p.stockQty} units</td>
                        <td><i class="fas fa-star" style="color:#f59e0b;"></i> ${p.averageRating ? p.averageRating.toFixed(1) : '0.0'} (${p.reviewCount})</td>
                        <td>
                            <button onclick="openEditProductModal(${JSON.stringify(p).replace(/"/g, '&quot;')})" class="btn btn-sm btn-secondary"><i class="fas fa-edit"></i> Edit</button>
                            <button onclick="deleteSellerProduct(${p.id})" class="btn btn-sm btn-danger"><i class="fas fa-trash"></i></button>
                        </td>
                    </tr>
                `).join('');
            }
        }

        if (orderJson.success && orderJson.data) {
            const orders = orderJson.data;
            document.getElementById('stat-total-orders').textContent = orders.length;

            if (orders.length === 0) {
                ordersTable.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:2rem; color:var(--text-muted);">No incoming orders for your products yet.</td></tr>`;
            } else {
                ordersTable.innerHTML = orders.map(o => {
                    const itemsHtml = o.items.map(i => `${i.productName} (x${i.quantity})`).join(', ');
                    return `
                    <tr>
                        <td><strong>RUB-${o.id}</strong></td>
                        <td>${o.buyerName} (${o.buyerEmail})</td>
                        <td>${itemsHtml}</td>
                        <td style="font-weight:700; color:var(--primary);">$${parseFloat(o.totalAmount).toFixed(2)}</td>
                        <td><span class="badge badge-${o.status.toLowerCase()}">${o.status}</span></td>
                        <td>
                            <select onchange="updateOrderStatus(${o.id}, this.value)" class="form-control" style="padding:0.3rem; font-size:0.85rem; width:auto;">
                                <option value="PENDING" ${o.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                                <option value="CONFIRMED" ${o.status === 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                                <option value="SHIPPED" ${o.status === 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
                                <option value="DELIVERED" ${o.status === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                                <option value="CANCELLED" ${o.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                            </select>
                        </td>
                    </tr>`;
                }).join('');
            }
        }
    } catch (e) {
        console.error("Error loading seller dashboard", e);
    }
}

function openAddProductModal() {
    currentEditProductId = null;
    document.getElementById('product-modal-title').textContent = 'Add New Product';
    document.getElementById('product-form').reset();
    document.getElementById('product-modal').classList.add('active');
}

function openEditProductModal(p) {
    currentEditProductId = p.id;
    document.getElementById('product-modal-title').textContent = 'Edit Product';
    document.getElementById('prod-name').value = p.name;
    document.getElementById('prod-category').value = p.category;
    document.getElementById('prod-price').value = p.price;
    document.getElementById('prod-stock').value = p.stockQty;
    document.getElementById('prod-image').value = p.imageUrl;
    document.getElementById('prod-desc').value = p.description;
    document.getElementById('product-modal').classList.add('active');
}

function closeProductModal() {
    document.getElementById('product-modal').classList.remove('active');
}

async function handleSaveProduct(event) {
    event.preventDefault();
    const name = document.getElementById('prod-name').value;
    const category = document.getElementById('prod-category').value;
    const price = parseFloat(document.getElementById('prod-price').value);
    const stockQty = parseInt(document.getElementById('prod-stock').value);
    const imageUrl = document.getElementById('prod-image').value;
    const description = document.getElementById('prod-desc').value;

    const method = currentEditProductId ? 'PUT' : 'POST';
    const url = currentEditProductId ? `${API_BASE}/products/${currentEditProductId}` : `${API_BASE}/products`;

    try {
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, category, price, stockQty, imageUrl, description })
        });
        const json = await res.json();

        if (json.success) {
            closeProductModal();
            loadSellerDashboard();
            alert(currentEditProductId ? 'Product updated successfully!' : 'Product added successfully!');
        } else {
            alert(json.error ? json.error.message : 'Operation failed');
        }
    } catch (e) {
        alert('Server error saving product.');
    }
}

async function deleteSellerProduct(productId) {
    if (!confirm('Are you sure you want to delete this product listing?')) return;
    try {
        const res = await fetch(`${API_BASE}/products/${productId}`, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) {
            loadSellerDashboard();
        } else {
            alert(json.error ? json.error.message : 'Cannot delete product.');
        }
    } catch (e) {
        alert('Server error deleting product.');
    }
}

async function updateOrderStatus(orderId, status) {
    try {
        const res = await fetch(`${API_BASE}/seller/orders/${orderId}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status })
        });
        const json = await res.json();

        if (json.success) {
            loadSellerDashboard();
        } else {
            alert(json.error ? json.error.message : 'Status update failed.');
        }
    } catch (e) {
        alert('Server error updating status.');
    }
}

document.addEventListener('DOMContentLoaded', loadSellerDashboard);
