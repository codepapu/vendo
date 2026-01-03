import { Link, useNavigate, useLocation } from 'react-router-dom';
import { ShoppingCart, User, Menu, X, Search, Building2, Store, Shield } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ThemeToggle } from '@/components/ui/theme-toggle';
import { NotificationProvider } from '@/components/notifications';
import { useAuthStore } from '@/stores/auth-store';
import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { useQuery } from '@tanstack/react-query';
import { cartApi } from '@/services/cart-api';
import { Input } from '@/components/ui/input';

export function Header() {
  const { isAuthenticated, user, logout } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Get cart item count
  const { data: cart } = useQuery({
    queryKey: ['cart'],
    queryFn: () => cartApi.getCart(),
    enabled: isAuthenticated,
  });

  const cartItemCount = cart?.totalItems || 0;

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
    }
  };

  // Determine user role
  const isB2B = user?.userType === 'BUSINESS';
  const isRetailer = user?.userType === 'RETAILER';
  const isAdmin = user?.superUser === true;

  // Get user initials for avatar
  const getUserInitials = () => {
    if (user?.firstName && user?.lastName) {
      return `${user.firstName[0]}${user.lastName[0]}`.toUpperCase();
    }
    if (user?.firstName) {
      return user.firstName[0].toUpperCase();
    }
    if (user?.email) {
      return user.email[0].toUpperCase();
    }
    return 'U';
  };

  // Check if current path is active
  const isActive = (path: string) => location.pathname === path || location.pathname.startsWith(path + '/');

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-16 items-center justify-between">
        <Link 
          to="/" 
          className="flex items-center space-x-2 transition-transform hover:scale-105"
        >
          <span className="text-2xl font-bold bg-gradient-to-r from-primary to-primary/60 bg-clip-text text-transparent">
            Vendo
          </span>
        </Link>

        {/* Desktop Navigation */}
        <nav className="hidden md:flex items-center space-x-6 flex-1 justify-center">
          <Link
            to="/products"
            className={`text-sm font-medium transition-colors hover:text-primary ${
              isActive('/products') ? 'text-primary' : ''
            }`}
          >
            Products
          </Link>
          <Link
            to="/categories"
            className={`text-sm font-medium transition-colors hover:text-primary ${
              isActive('/categories') ? 'text-primary' : ''
            }`}
          >
            Categories
          </Link>
          <Link
            to="/stores"
            className={`text-sm font-medium transition-colors hover:text-primary ${
              isActive('/stores') ? 'text-primary' : ''
            }`}
          >
            Stores
          </Link>
          <Link
            to="/blog"
            className={`text-sm font-medium transition-colors hover:text-primary ${
              isActive('/blog') ? 'text-primary' : ''
            }`}
          >
            Blog
          </Link>
          <Link
            to="/about"
            className={`text-sm font-medium transition-colors hover:text-primary ${
              isActive('/about') ? 'text-primary' : ''
            }`}
          >
            About
          </Link>
          <Link
            to="/contact"
            className={`text-sm font-medium transition-colors hover:text-primary ${
              isActive('/contact') ? 'text-primary' : ''
            }`}
          >
            Contact
          </Link>

          {/* Role-based navigation */}
          {isAuthenticated && (
            <>
              {isB2B && (
                <Link
                  to="/b2b/dashboard"
                  className="text-sm font-medium transition-colors hover:text-primary flex items-center gap-1"
                >
                  <Building2 className="h-4 w-4" />
                  B2B Portal
                </Link>
              )}
              {isRetailer && (
                <Link
                  to="/retailer/dashboard"
                  className="text-sm font-medium transition-colors hover:text-primary flex items-center gap-1"
                >
                  <Store className="h-4 w-4" />
                  Retailer
                </Link>
              )}
              {isAdmin && (
                <Link
                  to="/admin/dashboard"
                  className="text-sm font-medium transition-colors hover:text-primary flex items-center gap-1"
                >
                  <Shield className="h-4 w-4" />
                  Admin
                </Link>
              )}
            </>
          )}
        </nav>

        {/* Search Bar */}
        <form onSubmit={handleSearch} className="hidden lg:flex items-center flex-1 max-w-md mx-4">
          <div className="relative w-full">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              type="search"
              placeholder="Search products, brands, categories..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10 pr-4 w-full"
            />
          </div>
        </form>

        {/* Right Side Actions */}
        <div className="hidden md:flex items-center space-x-4">
          {isAuthenticated ? (
            <>
              <Link
                to="/cart"
                className="relative text-sm font-medium transition-colors hover:text-primary flex items-center gap-1"
              >
                <ShoppingCart className="h-5 w-5" />
                {cartItemCount > 0 && (
                  <Badge 
                    variant="destructive" 
                    className="absolute -top-2 -right-2 h-5 w-5 flex items-center justify-center p-0 text-xs rounded-full"
                  >
                    {cartItemCount > 99 ? '99+' : cartItemCount}
                  </Badge>
                )}
                <span className="sr-only">Cart</span>
              </Link>
              <NotificationProvider />
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="relative h-10 w-10 rounded-full">
                    <Avatar className="h-10 w-10">
                      <AvatarImage src={user?.avatarUrl} alt={user?.firstName || 'User'} />
                      <AvatarFallback>{getUserInitials()}</AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56">
                  <DropdownMenuLabel className="font-normal">
                    <div className="flex flex-col space-y-1">
                      <p className="text-sm font-medium leading-none">
                        {user?.firstName && user?.lastName
                          ? `${user.firstName} ${user.lastName}`
                          : user?.firstName || user?.email}
                      </p>
                      <p className="text-xs leading-none text-muted-foreground">{user?.email}</p>
                      <div className="flex gap-1 mt-1">
                        {isB2B && <Badge variant="secondary" className="text-xs">B2B</Badge>}
                        {isRetailer && <Badge variant="secondary" className="text-xs">Retailer</Badge>}
                        {isAdmin && <Badge variant="default" className="text-xs">Admin</Badge>}
                      </div>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => navigate('/profile')}>
                    <User className="mr-2 h-4 w-4" />
                    Profile
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => navigate('/profile/orders')}>
                    <ShoppingCart className="mr-2 h-4 w-4" />
                    Orders
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => navigate('/profile/settings')}>
                    Settings
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout}>
                    Logout
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : (
            <div className="flex items-center gap-2">
              <Button variant="ghost" size="sm" onClick={() => navigate('/login')}>
                Login
              </Button>
              <Button size="sm" onClick={() => navigate('/register')}>
                Sign Up
              </Button>
            </div>
          )}
          <ThemeToggle />
        </div>

        {/* Mobile Menu Button */}
        <div className="flex md:hidden items-center gap-2">
          {isAuthenticated && (
            <Link
              to="/cart"
              className="relative p-2"
              onClick={() => setMobileMenuOpen(false)}
            >
              <ShoppingCart className="h-5 w-5" />
              {cartItemCount > 0 && (
                <Badge 
                  variant="destructive" 
                  className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs rounded-full"
                >
                  {cartItemCount > 99 ? '99+' : cartItemCount}
                </Badge>
              )}
            </Link>
          )}
          <ThemeToggle />
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </Button>
        </div>
      </div>

      {/* Mobile Navigation */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/50 z-40 md:hidden"
              onClick={() => setMobileMenuOpen(false)}
            />
            <motion.nav
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              className="md:hidden border-t bg-background relative z-50"
            >
              <div className="container py-4 space-y-4">
                {/* Mobile Search */}
                <form onSubmit={handleSearch} className="lg:hidden">
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                      type="search"
                      placeholder="Search products..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="pl-10 pr-4 w-full"
                    />
                  </div>
                </form>

                {/* Navigation Links */}
                <div className="space-y-2">
                  <Link
                    to="/products"
                    className={`block text-sm font-medium transition-colors hover:text-primary py-2 ${
                      isActive('/products') ? 'text-primary' : ''
                    }`}
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Products
                  </Link>
                  <Link
                    to="/categories"
                    className={`block text-sm font-medium transition-colors hover:text-primary py-2 ${
                      isActive('/categories') ? 'text-primary' : ''
                    }`}
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Categories
                  </Link>
                  <Link
                    to="/stores"
                    className={`block text-sm font-medium transition-colors hover:text-primary py-2 ${
                      isActive('/stores') ? 'text-primary' : ''
                    }`}
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Stores
                  </Link>
                  <Link
                    to="/blog"
                    className={`block text-sm font-medium transition-colors hover:text-primary py-2 ${
                      isActive('/blog') ? 'text-primary' : ''
                    }`}
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Blog
                  </Link>
                  <Link
                    to="/about"
                    className={`block text-sm font-medium transition-colors hover:text-primary py-2 ${
                      isActive('/about') ? 'text-primary' : ''
                    }`}
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    About
                  </Link>
                  <Link
                    to="/contact"
                    className={`block text-sm font-medium transition-colors hover:text-primary py-2 ${
                      isActive('/contact') ? 'text-primary' : ''
                    }`}
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Contact
                  </Link>
                </div>

                {isAuthenticated ? (
                  <>
                    {/* Role-based links */}
                    {(isB2B || isRetailer || isAdmin) && (
                      <>
                        <div className="pt-4 border-t">
                          <p className="text-xs font-semibold text-muted-foreground mb-2">Quick Access</p>
                          {isB2B && (
                            <Link
                              to="/b2b/dashboard"
                              className="block text-sm font-medium transition-colors hover:text-primary py-2 flex items-center gap-2"
                              onClick={() => setMobileMenuOpen(false)}
                            >
                              <Building2 className="h-4 w-4" />
                              B2B Portal
                            </Link>
                          )}
                          {isRetailer && (
                            <Link
                              to="/retailer/dashboard"
                              className="block text-sm font-medium transition-colors hover:text-primary py-2 flex items-center gap-2"
                              onClick={() => setMobileMenuOpen(false)}
                            >
                              <Store className="h-4 w-4" />
                              Retailer Dashboard
                            </Link>
                          )}
                          {isAdmin && (
                            <Link
                              to="/admin/dashboard"
                              className="block text-sm font-medium transition-colors hover:text-primary py-2 flex items-center gap-2"
                              onClick={() => setMobileMenuOpen(false)}
                            >
                              <Shield className="h-4 w-4" />
                              Admin Panel
                            </Link>
                          )}
                        </div>
                        <div className="pt-4 border-t space-y-2">
                          <Link
                            to="/cart"
                            className="block text-sm font-medium transition-colors hover:text-primary flex items-center gap-2 py-2"
                            onClick={() => setMobileMenuOpen(false)}
                          >
                            <ShoppingCart className="h-4 w-4" />
                            Cart {cartItemCount > 0 && `(${cartItemCount})`}
                          </Link>
                          <div className="flex items-center gap-2 py-2" onClick={() => setMobileMenuOpen(false)}>
                            <NotificationProvider />
                            <span className="text-sm font-medium">Notifications</span>
                          </div>
                          <Link
                            to="/profile"
                            className="block text-sm font-medium transition-colors hover:text-primary flex items-center gap-2 py-2"
                            onClick={() => setMobileMenuOpen(false)}
                          >
                            <User className="h-4 w-4" />
                            Profile
                          </Link>
                          <Link
                            to="/profile/orders"
                            className="block text-sm font-medium transition-colors hover:text-primary py-2"
                            onClick={() => setMobileMenuOpen(false)}
                          >
                            Orders
                          </Link>
                          <Link
                            to="/profile/settings"
                            className="block text-sm font-medium transition-colors hover:text-primary py-2"
                            onClick={() => setMobileMenuOpen(false)}
                          >
                            Settings
                          </Link>
                        </div>
                        <div className="pt-4 border-t space-y-2">
                          <div className="px-2">
                            <p className="text-sm font-medium">{user?.firstName || user?.email}</p>
                            <p className="text-xs text-muted-foreground">{user?.email}</p>
                            <div className="flex gap-1 mt-1">
                              {isB2B && <Badge variant="secondary" className="text-xs">B2B</Badge>}
                              {isRetailer && <Badge variant="secondary" className="text-xs">Retailer</Badge>}
                              {isAdmin && <Badge variant="default" className="text-xs">Admin</Badge>}
                            </div>
                          </div>
                          <Button variant="outline" className="w-full" onClick={handleLogout}>
                            Logout
                          </Button>
                        </div>
                      </>
                    )}
                    {!isB2B && !isRetailer && !isAdmin && (
                      <>
                        <div className="pt-4 border-t space-y-2">
                          <Link
                            to="/cart"
                            className="block text-sm font-medium transition-colors hover:text-primary flex items-center gap-2 py-2"
                            onClick={() => setMobileMenuOpen(false)}
                          >
                            <ShoppingCart className="h-4 w-4" />
                            Cart {cartItemCount > 0 && `(${cartItemCount})`}
                          </Link>
                          <div className="flex items-center gap-2 py-2" onClick={() => setMobileMenuOpen(false)}>
                            <NotificationProvider />
                            <span className="text-sm font-medium">Notifications</span>
                          </div>
                          <Link
                            to="/profile"
                            className="block text-sm font-medium transition-colors hover:text-primary flex items-center gap-2 py-2"
                            onClick={() => setMobileMenuOpen(false)}
                          >
                            <User className="h-4 w-4" />
                            Profile
                          </Link>
                          <Link
                            to="/profile/orders"
                            className="block text-sm font-medium transition-colors hover:text-primary py-2"
                            onClick={() => setMobileMenuOpen(false)}
                          >
                            Orders
                          </Link>
                          <Link
                            to="/profile/settings"
                            className="block text-sm font-medium transition-colors hover:text-primary py-2"
                            onClick={() => setMobileMenuOpen(false)}
                          >
                            Settings
                          </Link>
                        </div>
                        <div className="pt-4 border-t space-y-2">
                          <p className="text-sm text-muted-foreground px-2">{user?.email}</p>
                          <Button variant="outline" className="w-full" onClick={handleLogout}>
                            Logout
                          </Button>
                        </div>
                      </>
                    )}
                  </>
                ) : (
                  <div className="pt-4 border-t space-y-2">
                    <Button
                      variant="outline"
                      className="w-full"
                      onClick={() => {
                        navigate('/login');
                        setMobileMenuOpen(false);
                      }}
                    >
                      Login
                    </Button>
                    <Button
                      className="w-full"
                      onClick={() => {
                        navigate('/register');
                        setMobileMenuOpen(false);
                      }}
                    >
                      Sign Up
                    </Button>
                  </div>
                )}
              </div>
            </motion.nav>
          </>
        )}
      </AnimatePresence>
    </header>
  );
}
