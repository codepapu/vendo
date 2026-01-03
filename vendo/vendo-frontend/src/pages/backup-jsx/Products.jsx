import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getProducts } from '../services/api'
import './Products.css'

function Products() {
  const [page, setPage] = useState(1)
  const { data, isLoading } = useQuery({
    queryKey: ['products', page],
    queryFn: () => getProducts({ page, limit: 12 })
  })

  return (
    <div className="products-page">
      <h1>All Products</h1>
      
      {isLoading ? (
        <div className="loading">Loading products...</div>
      ) : (
        <>
          <div className="grid grid-cols-4">
            {data?.data?.map((product) => (
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
          
          <div className="pagination">
            <button 
              onClick={() => setPage(p => Math.max(1, p - 1))}
              disabled={page === 1}
              className="btn btn-secondary"
            >
              Previous
            </button>
            <span>Page {page}</span>
            <button 
              onClick={() => setPage(p => p + 1)}
              disabled={!data?.hasMore}
              className="btn btn-secondary"
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  )
}

export default Products
