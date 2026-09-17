import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import './index.css'
import './i18n/i18n'
import MainPage from './pages/MainPage'
import SearchPage from './pages/SearchPage'
import UserPage from './pages/UserPage'
import SettingsPage from './pages/SettingsPage'
import { AppLayout } from './components/layout/AppLayout'

const queryClient = new QueryClient()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
        <BrowserRouter>
        <Routes>
          <Route element={<AppLayout />}>
            <Route path="/" element={<MainPage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/profile" element={<UserPage />} />
            <Route path="/settings" element={<SettingsPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)
