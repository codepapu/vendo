import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@/test/utils/test-utils';
import userEvent from '@testing-library/user-event';
import { useNavigate } from 'react-router-dom';
import { useToast } from '@/hooks/use-toast';
import { b2bApi } from '@/services/b2b-api';
import { useB2BStore } from '@/stores/b2b-store';
import B2BRegister from '../register';

// Mock dependencies
vi.mock('react-router-dom', () => ({
  useNavigate: vi.fn(),
}));

vi.mock('@/hooks/use-toast', () => ({
  useToast: vi.fn(),
}));

vi.mock('@/services/b2b-api', () => ({
  b2bApi: {
    createAccount: vi.fn(),
  },
}));

vi.mock('@/stores/b2b-store', () => ({
  useB2BStore: vi.fn(),
}));

describe('B2BRegistrationForm', () => {
  const mockNavigate = vi.fn();
  const mockToast = vi.fn();
  const mockSetBusinessAccount = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    (useNavigate as any).mockReturnValue(mockNavigate);
    (useToast as any).mockReturnValue({ toast: mockToast });
    (useB2BStore as any).mockReturnValue({
      setBusinessAccount: mockSetBusinessAccount,
    });
  });

  it('should render registration form with all fields', () => {
    render(<B2BRegister />);

    expect(screen.getByText('Register Business Account')).toBeInTheDocument();
    expect(screen.getByLabelText(/company name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/business registration number/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/tax id/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/business type/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/credit limit/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/billing address id/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create business account/i })).toBeInTheDocument();
  });

  it('should validate required fields', async () => {
    const user = userEvent.setup();
    render(<B2BRegister />);

    const submitButton = screen.getByRole('button', { name: /create business account/i });
    await user.click(submitButton);

    // Form validation should prevent submission
    await waitFor(() => {
      expect(b2bApi.createAccount).not.toHaveBeenCalled();
    });
  });

  it('should submit form with valid data', async () => {
    const user = userEvent.setup();
    const mockAccount = {
      id: 1,
      companyName: 'Test Company',
      businessType: 'CORPORATE',
      status: 'PENDING',
    };

    (b2bApi.createAccount as any).mockResolvedValue(mockAccount);

    render(<B2BRegister />);

    // Fill in required fields
    await user.type(screen.getByLabelText(/company name/i), 'Test Company');
    
    // Select business type
    const businessTypeSelect = screen.getByLabelText(/business type/i);
    await user.click(businessTypeSelect);
    await user.click(screen.getByText('Corporate'));

    // Submit form
    const submitButton = screen.getByRole('button', { name: /create business account/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(b2bApi.createAccount).toHaveBeenCalledWith(
        expect.objectContaining({
          companyName: 'Test Company',
          businessType: 'CORPORATE',
        })
      );
    });

    await waitFor(() => {
      expect(mockSetBusinessAccount).toHaveBeenCalledWith(mockAccount);
      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({
          title: 'Success',
          description: 'Business account created successfully',
        })
      );
      expect(mockNavigate).toHaveBeenCalledWith('/b2b/dashboard');
    });
  });

  it('should handle form submission errors', async () => {
    const user = userEvent.setup();
    const errorMessage = 'Company name already exists';

    (b2bApi.createAccount as any).mockRejectedValue({
      response: {
        data: {
          message: errorMessage,
        },
      },
    });

    render(<B2BRegister />);

    // Fill in required fields
    await user.type(screen.getByLabelText(/company name/i), 'Test Company');
    
    // Select business type
    const businessTypeSelect = screen.getByLabelText(/business type/i);
    await user.click(businessTypeSelect);
    await user.click(screen.getByText('Corporate'));

    // Submit form
    const submitButton = screen.getByRole('button', { name: /create business account/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({
          title: 'Error',
          description: errorMessage,
          variant: 'destructive',
        })
      );
    });

    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('should show loading state during submission', async () => {
    const user = userEvent.setup();
    let resolvePromise: (value: any) => void;

    (b2bApi.createAccount as any).mockImplementation(
      () =>
        new Promise((resolve) => {
          resolvePromise = resolve;
        })
    );

    render(<B2BRegister />);

    // Fill in required fields
    await user.type(screen.getByLabelText(/company name/i), 'Test Company');
    
    // Select business type
    const businessTypeSelect = screen.getByLabelText(/business type/i);
    await user.click(businessTypeSelect);
    await user.click(screen.getByText('Corporate'));

    // Submit form
    const submitButton = screen.getByRole('button', { name: /create business account/i });
    await user.click(submitButton);

    // Check loading state
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /creating/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /creating/i })).toBeDisabled();
    });

    // Resolve the promise
    resolvePromise!({ id: 1, companyName: 'Test Company' });

    await waitFor(() => {
      expect(screen.queryByRole('button', { name: /creating/i })).not.toBeInTheDocument();
    });
  });

  it('should allow optional fields to be left empty', async () => {
    const user = userEvent.setup();
    const mockAccount = {
      id: 1,
      companyName: 'Test Company',
      businessType: 'SME',
      status: 'PENDING',
    };

    (b2bApi.createAccount as any).mockResolvedValue(mockAccount);

    render(<B2BRegister />);

    // Fill only required fields
    await user.type(screen.getByLabelText(/company name/i), 'Test Company');
    
    // Select business type
    const businessTypeSelect = screen.getByLabelText(/business type/i);
    await user.click(businessTypeSelect);
    await user.click(screen.getByText('SME'));

    // Submit form
    const submitButton = screen.getByRole('button', { name: /create business account/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(b2bApi.createAccount).toHaveBeenCalledWith(
        expect.objectContaining({
          companyName: 'Test Company',
          businessType: 'SME',
        })
      );
    });
  });

  it('should display validation errors for invalid input', async () => {
    const user = userEvent.setup();
    render(<B2BRegister />);

    // Try to submit without required fields
    const submitButton = screen.getByRole('button', { name: /create business account/i });
    await user.click(submitButton);

    // Check for validation errors (if form library shows them)
    await waitFor(() => {
      const companyNameInput = screen.getByLabelText(/company name/i);
      expect(companyNameInput).toBeInvalid();
    });
  });
});

