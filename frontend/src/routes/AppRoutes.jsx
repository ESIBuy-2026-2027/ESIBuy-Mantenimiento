import { Route, Routes } from 'react-router-dom'
import GuestOnlyRoute from '../auth/GuestOnlyRoute'
import ProtectedRoute from '../auth/ProtectedRoute'
import { ROLES } from '../auth/roles'
import AppLayout from '../components/AppLayout'
import AddressesPage from '../pages/AddressesPage'
import AdminAdminsPage from '../pages/AdminAdminsPage'
import AdminClientsPage from '../pages/AdminClientsPage'
import AdminNewAdminPage from '../pages/AdminNewAdminPage'
import AdminPage from '../pages/AdminPage'
import AdminSellersPage from '../pages/AdminSellersPage'
import AdminUsersPage from '../pages/AdminUsersPage'
import CheckoutPage from '../pages/CheckoutPage'
import ForgotPasswordPage from '../pages/ForgotPasswordPage'
import Home from '../pages/Home'
import LoginPage from '../pages/LoginPage'
import PaymentMethodsPage from '../pages/PaymentMethodsPage'
import ProductDetailPage from '../pages/ProductDetailPage'
import ProductsPage from '../pages/ProductsPage'
import ProfilePage from '../pages/ProfilePage'
import Register from '../pages/Register'
import SellerProductFormPage from '../pages/SellerProductFormPage'
import SellerProductsPage from '../pages/SellerProductsPage'
import { PATHS } from './paths'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path={PATHS.LOGIN} element={<LoginPage />} />
      <Route path={PATHS.FORGOT_PASSWORD} element={<ForgotPasswordPage />} />

      <Route element={<AppLayout />}>
        <Route path={PATHS.HOME} element={<Home />} />
        <Route element={<GuestOnlyRoute />}>
          <Route path={PATHS.REGISTER} element={<Register />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={[ROLES.CLIENTE]} />}>
          <Route path={PATHS.PRODUCTS} element={<ProductsPage />} />
          <Route path={PATHS.PRODUCT_DETAIL} element={<ProductDetailPage />} />
          <Route path={PATHS.CHECKOUT} element={<CheckoutPage />} />
          <Route path={PATHS.PAYMENT_METHODS} element={<PaymentMethodsPage />} />
          <Route path={PATHS.ADDRESSES} element={<AddressesPage />} />
        </Route>

        {/* El perfil propio (HU4) es del cliente y del vendedor: el backend responde 403 al administrador. */}
        <Route element={<ProtectedRoute allowedRoles={[ROLES.CLIENTE, ROLES.VENDEDOR]} />}>
          <Route path={PATHS.PROFILE} element={<ProfilePage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN]} />}>
          <Route path={PATHS.ADMIN} element={<AdminPage />} />
          <Route path={PATHS.ADMIN_SELLERS} element={<AdminSellersPage />} />
          <Route path={PATHS.ADMIN_USERS} element={<AdminUsersPage />} />
          <Route path={PATHS.ADMIN_CLIENTS} element={<AdminClientsPage />} />
          <Route path={PATHS.ADMIN_ADMINS} element={<AdminAdminsPage />} />
          <Route path={PATHS.ADMIN_NEW_ADMIN} element={<AdminNewAdminPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={[ROLES.VENDEDOR]} />}>
          <Route path={PATHS.SELLER_PRODUCTS} element={<SellerProductsPage />} />
          <Route path={PATHS.SELLER_PRODUCT_NEW} element={<SellerProductFormPage />} />
          <Route path={PATHS.SELLER_PRODUCT_EDIT} element={<SellerProductFormPage />} />
        </Route>
      </Route>
    </Routes>
  )
}