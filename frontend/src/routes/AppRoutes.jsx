import { Route, Routes } from 'react-router-dom'
import ProtectedRoute from '../auth/ProtectedRoute'
import { ROLES } from '../auth/roles'
import AppLayout from '../components/AppLayout'
import AdminPage from '../pages/AdminPage'
import Home from '../pages/Home'
import LoginPage from '../pages/LoginPage'
import ProductsPage from '../pages/ProductsPage'
import Register from '../pages/Register'
import SellerProductsPage from '../pages/SellerProductsPage'
import { PATHS } from './paths'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/register" element={<Register />} />
      <Route path={PATHS.LOGIN} element={<LoginPage />} />

      <Route element={<AppLayout />}>
        <Route path={PATHS.HOME} element={<Home />} />
        <Route path={PATHS.PRODUCTS} element={<ProductsPage />} />

        <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN]} />}>
          <Route path={PATHS.ADMIN} element={<AdminPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={[ROLES.VENDEDOR]} />}>
          <Route path={PATHS.SELLER_PRODUCTS} element={<SellerProductsPage />} />
        </Route>
      </Route>
    </Routes>
  )
}