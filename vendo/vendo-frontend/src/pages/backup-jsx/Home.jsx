import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getProducts } from '../services/api'
import './Home.css'

function Home() {
  const { data: products, isLoading } = useQuery({
    queryKey: ['products', 'featured'],
    queryFn: () => getProducts({ limit: 8 })
  })

  return (
    <div className="home">
      <section className="hero">
        <h1>Welcome to Vendo</h1>
        <p>Your one-stop shop for everything you need</p>
        <Link to="/products" className="btn btn-primary">
          Shop Now
        </Link>
      </section>

      <section className="featured-products">
        <h2>Featured Products</h2>
        {isLoading ? (
          <div className="loading">Loading products...</div>
        ) : (
          <div className="grid grid-cols-4">
            {products?.data?.map((product) => (
              <div key={product.id} className="card product-card">
                <Link to={`/products/${product.id}`}>
                  <img 
                    src={product.image || '/placeholder.jpg'} 
                    alt={product.name}
                    className="product-image"
                  />
                  <h3>{product.name}</h3>
                  <p className="price">${product.price}</p>
                </Link>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}

export default Home
