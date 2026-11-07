export const PATHS = {
  HOME: '/',
  LOGIN: '/login',
  FORGOT_PASSWORD: '/forgot-password',
  REGISTER: '/register',
  PRODUCTS: '/products',
  PRODUCT_DETAIL: '/products/:id',
  CHECKOUT: '/checkout',
  PROFILE: '/profile',
  PAYMENT_METHODS: '/payment-methods',
  ADDRESSES: '/addresses',
  ORDERS: '/orders',
  ORDER_DETAIL: '/orders/:id',
  WISHLIST: '/wishlist',
  INCIDENTS: '/incidents',
  INCIDENT_NEW: '/incidents/new',
  ADMIN: '/admin',
  ADMIN_USERS: '/admin/users',
  ADMIN_CLIENTS: '/admin/clients',
  ADMIN_SELLERS: '/admin/sellers',
  ADMIN_CATEGORIES: '/admin/categories',
  ADMIN_ADMINS: '/admin/admins',
  ADMIN_NEW_ADMIN: '/admin/admins/new',
  SELLER_PRODUCTS: '/seller/products',
  SELLER_PRODUCT_NEW: '/seller/products/new',
  SELLER_PRODUCT_EDIT: '/seller/products/:id/edit',
}

export const getProductDetailPath = (productId) => `/products/${encodeURIComponent(productId)}`

export const getOrderDetailPath = (orderId) => `/orders/${encodeURIComponent(orderId)}`

export const getSellerProductEditPath = (productId) =>
  `/seller/products/${encodeURIComponent(productId)}/edit`
