import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add token to requests if available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Product APIs
export const getProducts = async (params = {}) => {
  const response = await api.get('/products', { params })
  return response.data
}

export const getProduct = async (id) => {
  const response = await api.get(`/products/${id}`)
  return response.data
}

// Auth APIs
export const login = async (credentials) => {
  const response = await api.post('/auth/login', credentials)
  return response.data
}

export const register = async (userData) => {
  const response = await api.post('/auth/register', userData)
  return response.data
}

// Cart APIs
export const getCart = async () => {
  const response = await api.get('/cart')
  return response.data
}

export const addToCart = async (productId, quantity = 1) => {
  const response = await api.post('/cart/items', { productId, quantity })
  return response.data
}

export default api
