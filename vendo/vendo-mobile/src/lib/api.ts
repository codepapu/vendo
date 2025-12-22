import { ApiClient, AuthApi, ProductsApi } from '@vendo/shared';
import Constants from 'expo-constants';

const API_BASE_URL =
  Constants.expoConfig?.extra?.apiUrl || 'http://localhost:8080/api';

const apiClient = new ApiClient(API_BASE_URL);

export const authApi = new AuthApi(apiClient);
export const productsApi = new ProductsApi(apiClient);

export { apiClient };
