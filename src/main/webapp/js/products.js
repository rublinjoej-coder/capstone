/* RublinMart Products Client */

async function loadCategories() {
    const categorySelect = document.getElementById('category-filter');
    if (!categorySelect) return;

    try {
        const res = await fetch(`${API_BASE}/products/categories`);
        const json = await res.json();
        if (json.success && json.data) {
            let html = '<option value="all">All Categories</option>';
            json.data.forEach(cat => {
                html += `<option value="${cat}">${cat}</option>`;
            });
            categorySelect.innerHTML = html;
        }
    } catch (e) {
        console.error("Failed to load categories", e);
    }
}

async function loadProducts() {
    const grid = document.getElementById('product-grid');
    if (!grid) return;

    const searchInput = document.getElementById('search-input');
    const categorySelect = document.getElementById('category-filter');

    const search = searchInput ? searchInput.value : '';
    const category = categorySelect ? categorySelect.value : 'all';

    grid.innerHTML = '<div style="grid-column: 1/-1; text-align:center; padding:3rem;"><i class="fas fa-spinner fa-spin fa-2x"></i><p>Loading catalog...</p></div>';

    try {
        const url = `${API_BASE}/products?search=${encodeURIComponent(search)}&category=${encodeURIComponent(category)}`;
        const res = await fetch(url);
        const json = await res.json();

        if (json.success && json.data) {
            renderProductGrid(json.data, grid);
        } else {
            grid.innerHTML = '<div style="grid-column: 1/-1; text-align:center; padding:3rem;"><p>No products found.</p></div>';
        }
    } catch (e) {
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align:center; padding:3rem;" class="alert alert-error">Failed to connect to RublinMart server.</div>';
    }
}

function renderProductGrid(products, container) {
    if (products.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="grid-column: 1/-1;">
                <i class="fas fa-box-open"></i>
                <h3>No Products Found</h3>
                <p style="color:var(--text-muted)">Try adjusting your search query or category filter.</p>
            </div>`;
        return;
    }

    container.innerHTML = products.map(p => {
        const stars = '★'.repeat(Math.round(p.averageRating || 0)) + '☆'.repeat(5 - Math.round(p.averageRating || 0));
        return `
        <div class="product-card">
            <div class="product-img-wrapper">
                <img src="${p.imageUrl}" alt="${p.name}" onerror="this.src='https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500'">
                <span class="product-category-tag">${p.category}</span>
            </div>
            <div class="product-info">
                <div class="product-seller"><i class="fas fa-store"></i> ${p.sellerName || 'Verified Seller'}</div>
                <h3 class="product-title">
                    <a href="/rublinmart/pages/product-details.html?id=${p.id}">${p.name}</a>
                </h3>
                <p style="font-size:0.875rem; color:var(--text-muted); margin-bottom:0.75rem; line-clamp:2;">${p.description}</p>
                <div class="product-rating">
                    <span style="color:#f59e0b;">${stars}</span>
                    <span style="font-size:0.8rem; color:var(--text-muted);">(${p.reviewCount || 0})</span>
                </div>
                <div class="product-price-row">
                    <div>
                        <div class="product-price">$${parseFloat(p.price).toFixed(2)}</div>
                        <div class="product-stock">${p.stockQty > 0 ? p.stockQty + ' in stock' : '<span style="color:var(--danger)">Out of stock</span>'}</div>
                    </div>
                    <button onclick="addToCart(${p.id})" class="btn btn-sm btn-primary" ${p.stockQty <= 0 ? 'disabled' : ''}>
                        <i class="fas fa-cart-plus"></i> Add
                    </button>
                </div>
            </div>
        </div>`;
    }).join('');
}

async function loadProductDetails() {
    const detailContainer = document.getElementById('product-detail-view');
    if (!detailContainer) return;

    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');

    if (!productId) {
        detailContainer.innerHTML = '<div class="alert alert-error">Invalid product ID.</div>';
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/products/${productId}`);
        const json = await res.json();

        if (json.success && json.data) {
            const p = json.data;
            const stars = '★'.repeat(Math.round(p.averageRating || 0)) + '☆'.repeat(5 - Math.round(p.averageRating || 0));

            detailContainer.innerHTML = `
                <div class="product-detail-container">
                    <div>
                        <img src="${p.imageUrl}" alt="${p.name}" class="product-detail-img" onerror="this.src='https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500'">
                    </div>
                    <div>
                        <span class="badge badge-confirmed">${p.category}</span>
                        <h1 class="product-detail-title">${p.name}</h1>
                        <p style="color:var(--text-muted); margin-bottom:1rem;"><i class="fas fa-store"></i> Sold by: <strong>${p.sellerName || 'Verified Seller'}</strong></p>
                        
                        <div class="product-rating" style="font-size:1.2rem; margin-bottom:1.5rem;">
                            <span>${stars}</span>
                            <span>(${p.averageRating ? p.averageRating.toFixed(1) : '0.0'} / 5 based on ${p.reviewCount || 0} reviews)</span>
                        </div>

                        <div style="font-size:2.2rem; font-weight:700; color:var(--primary); margin-bottom:1.5rem;">
                            $${parseFloat(p.price).toFixed(2)}
                        </div>

                        <p style="font-size:1.05rem; line-height:1.7; color:var(--text); margin-bottom:2rem;">
                            ${p.description}
                        </p>

                        <div style="margin-bottom:2rem;">
                            <strong>Availability:</strong> 
                            ${p.stockQty > 0 ? `<span style="color:var(--success); font-weight:600;">In Stock (${p.stockQty} items left)</span>` : '<span style="color:var(--danger); font-weight:600;">Out of Stock</span>'}
                        </div>

                        <div style="display:flex; gap:1rem; align-items:center;">
                            <div class="qty-control" style="height:45px;">
                                <button class="qty-btn" onclick="adjustDetailQty(-1)">-</button>
                                <input type="number" id="detail-qty" class="qty-input" value="1" min="1" max="${p.stockQty}">
                                <button class="qty-btn" onclick="adjustDetailQty(1)">+</button>
                            </div>
                            <button onclick="addDetailToCart(${p.id})" class="btn btn-primary" style="height:45px; font-size:1.1rem;" ${p.stockQty <= 0 ? 'disabled' : ''}>
                                <i class="fas fa-cart-plus"></i> Add to Cart
                            </button>
                        </div>
                    </div>
                </div>
            `;

            loadReviews(productId);
        } else {
            detailContainer.innerHTML = '<div class="alert alert-error">Product not found.</div>';
        }
    } catch (e) {
        detailContainer.innerHTML = '<div class="alert alert-error">Error loading product details.</div>';
    }
}

function adjustDetailQty(delta) {
    const input = document.getElementById('detail-qty');
    if (!input) return;
    let val = parseInt(input.value) || 1;
    val = Math.max(1, Math.min(parseInt(input.max) || 99, val + delta));
    input.value = val;
}

async function addDetailToCart(productId) {
    const input = document.getElementById('detail-qty');
    const qty = input ? parseInt(input.value) : 1;
    await addToCart(productId, qty);
}

async function loadReviews(productId) {
    const container = document.getElementById('reviews-container');
    if (!container) return;

    try {
        const res = await fetch(`${API_BASE}/reviews?productId=${productId}`);
        const json = await res.json();

        if (json.success && json.data) {
            if (json.data.length === 0) {
                container.innerHTML = '<p style="color:var(--text-muted)">No customer reviews yet. Be the first buyer to review this product!</p>';
            } else {
                container.innerHTML = json.data.map(r => {
                    const stars = '★'.repeat(r.rating) + '☆'.repeat(5 - r.rating);
                    return `
                    <div class="review-item">
                        <div class="review-header">
                            <span class="review-author"><i class="fas fa-user-circle"></i> ${r.userName}</span>
                            <span style="color:#f59e0b; font-weight:bold;">${stars} (${r.rating}/5)</span>
                        </div>
                        <p style="color:var(--text);">${r.comment}</p>
                        <span style="font-size:0.75rem; color:var(--text-muted);">${r.createdAt}</span>
                    </div>`;
                }).join('');
            }
        }
    } catch (e) {
        console.error("Failed to load reviews", e);
    }
}

async function submitReview(event) {
    event.preventDefault();
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');
    const rating = document.getElementById('review-rating').value;
    const comment = document.getElementById('review-comment').value;
    const alertBox = document.getElementById('review-alert');

    if (alertBox) alertBox.style.display = 'none';

    try {
        const res = await fetch(`${API_BASE}/reviews`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId, rating: parseInt(rating), comment })
        });
        const json = await res.json();

        if (json.success) {
            if (alertBox) {
                alertBox.className = 'alert alert-success';
                alertBox.textContent = 'Thank you! Your review has been published.';
                alertBox.style.display = 'block';
            }
            loadReviews(productId);
            document.getElementById('review-form').reset();
        } else {
            if (alertBox) {
                alertBox.className = 'alert alert-error';
                alertBox.textContent = json.error ? json.error.message : 'Failed to submit review.';
                alertBox.style.display = 'block';
            }
        }
    } catch (e) {
        if (alertBox) {
            alertBox.className = 'alert alert-error';
            alertBox.textContent = 'Server connection error.';
            alertBox.style.display = 'block';
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadCategories();
    loadProducts();
    loadProductDetails();
});
