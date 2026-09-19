/* RublinMart Admin Control Panel Client */

async function loadAdminDashboard() {
    const usersTable = document.getElementById('admin-users-body');
    const productsTable = document.getElementById('admin-products-body');
    const ordersTable = document.getElementById('admin-orders-body');
    if (!usersTable) return;

    try {
        const [usersRes, prodsRes, ordersRes] = await Promise.all([
            fetch(`${API_BASE}/admin/users`),
            fetch(`${API_BASE}/admin/products`),
            fetch(`${API_BASE}/admin/orders`)
        ]);

        const usersJson = await usersRes.json();
        const prodsJson = await prodsRes.json();
        const ordersJson = await ordersRes.json();

        if (usersJson.success && usersJson.data) {
            const users = usersJson.data;
            document.getElementById('stat-total-users').textContent = users.length;

            usersTable.innerHTML = users.map(u => `
                <tr>
                    <td><strong>#${u.id}</strong></td>
                    <td>${u.name}</td>
                    <td>${u.email}</td>
                    <td><span class="badge badge-role-${u.role.toLowerCase()}">${u.role}</span></td>
                    <td>${u.createdAt}</td>
                </tr>
            `).join('');
        }

        if (prodsJson.success && prodsJson.data) {
            const products = prodsJson.data;
            document.getElementById('stat-total-products').textContent = products.length;

            productsTable.innerHTML = products.map(p => `
                <tr>
                    <td>
                        <div style="display:flex; align-items:center; gap:0.75rem;">
                            <img src="${p.imageUrl}" style="width:35px; height:35px; object-fit:cover; border-radius:4px;" onerror="this.src='https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500'">
                            <strong>${p.name}</strong>
                        </div>
                    </td>
                    <td>${p.sellerName}</td>
                    <td><span class="badge badge-confirmed">${p.category}</span></td>
                    <td>$${parseFloat(p.price).toFixed(2)}</td>
                    <td>${p.stockQty}</td>
                    <td>
                        <button onclick="adminDeleteProduct(${p.id})" class="btn btn-sm btn-danger"><i class="fas fa-gavel"></i> Remove</button>
                    </td>
                </tr>
            `).join('');
        }

        if (ordersJson.success && ordersJson.data) {
            const orders = ordersJson.data;
            document.getElementById('stat-total-orders').textContent = orders.length;

            ordersTable.innerHTML = orders.map(o => `
                <tr>
                    <td><strong>RUB-${o.id}</strong></td>
                    <td>${o.buyerName}</td>
                    <td>${o.items.length} items</td>
                    <td style="font-weight:700; color:var(--primary);">$${parseFloat(o.totalAmount).toFixed(2)}</td>
                    <td><span class="badge badge-${o.status.toLowerCase()}">${o.status}</span></td>
                    <td>${o.createdAt}</td>
                </tr>
            `).join('');
        }
    } catch (e) {
        console.error("Failed to load admin dashboard data", e);
    }
}

async function adminDeleteProduct(productId) {
    if (!confirm('ADMIN ACTION: Are you sure you want to remove this inappropriate product from RublinMart?')) return;
    try {
        const res = await fetch(`${API_BASE}/products/${productId}`, { method: 'DELETE' });
        const json = await res.json();
        if (json.success) {
            alert('Product removed by Admin.');
            loadAdminDashboard();
        } else {
            alert(json.error ? json.error.message : 'Cannot remove product.');
        }
    } catch (e) {
        alert('Server connection error.');
    }
}

document.addEventListener('DOMContentLoaded', loadAdminDashboard);
