import { ApiClient, AuthApi, ProductsApi } from '@vendo/shared';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const apiClient = new ApiClient(API_BASE_URL);

export const authApi = new AuthApi(apiClient);
export const productsApi = new ProductsApi(apiClient);

export { apiClient };
