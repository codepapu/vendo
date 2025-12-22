import { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '../types';
import { ApiClient } from './client';

export class AuthApi {
  constructor(private client: ApiClient) {}

  async login(credentials: LoginRequest): Promise<ApiResponse<AuthResponse>> {
    const response = await this.client.instance.post<ApiResponse<AuthResponse>>(
      '/v1/auth/login',
      credentials
    );
    return response.data;
  }

  async register(userData: RegisterRequest): Promise<ApiResponse<AuthResponse>> {
    const response = await this.client.instance.post<ApiResponse<AuthResponse>>(
      '/v1/auth/register',
      userData
    );
    return response.data;
  }

  async refreshToken(refreshToken: string): Promise<ApiResponse<AuthResponse>> {
    const response = await this.client.instance.post<ApiResponse<AuthResponse>>(
      '/v1/auth/refresh',
      { refreshToken }
    );
    return response.data;
  }

  async logout(): Promise<void> {
    await this.client.instance.post('/v1/auth/logout');
    this.client.clearAuth();
  }
}
