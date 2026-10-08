import { Route, Routes } from 'react-router-dom'
import ProtectedRoute from '../auth/ProtectedRoute'
import { ROLES } from '../auth/roles'
import AppLayout from '../components/AppLayout'
import AdminPage from '../pages/AdminPage'
import AdminSellersPage from '../pages/AdminSellersPage'
import AdminUsersPage from '../pages/AdminUsersPage'
import Home from '../pages/Home'
import LoginPage from '../pages/LoginPage'
import ProductsPage from '../pages/ProductsPage'
import Register from '../pages/Register'
import SellerProductFormPage from '../pages/SellerProductFormPage'
import SellerProductsPage from '../pages/SellerProductsPage'
import { PATHS } from './paths'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path={PATHS.LOGIN} element={<LoginPage />} />

      <Route element={<AppLayout />}>
        <Route path={PATHS.HOME} element={<Home />} />
        <Route path={PATHS.REGISTER} element={<Register />} />
        <Route path={PATHS.PRODUCTS} element={<ProductsPage />} />

        <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN]} />}>
          <Route path={PATHS.ADMIN} element={<AdminPage />} />
          <Route path={PATHS.ADMIN_SELLERS} element={<AdminSellersPage />} />
          <Route path={PATHS.ADMIN_USERS} element={<AdminUsersPage />} />
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