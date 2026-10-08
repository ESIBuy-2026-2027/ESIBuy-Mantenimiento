export const PATHS = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  PRODUCTS: '/products',
  ADMIN: '/admin',
  ADMIN_USERS: '/admin/users',
  ADMIN_CLIENTS: '/admin/clients',
  ADMIN_SELLERS: '/admin/sellers',
  ADMIN_ADMINS: '/admin/admins',
  ADMIN_NEW_ADMIN: '/admin/admins/new',
  SELLER_PRODUCTS: '/seller/products',
  SELLER_PRODUCT_NEW: '/seller/products/new',
  SELLER_PRODUCT_EDIT: '/seller/products/:id/edit',
}

export const getSellerProductEditPath = (productId) =>
  `/seller/products/${encodeURIComponent(productId)}/edit`
