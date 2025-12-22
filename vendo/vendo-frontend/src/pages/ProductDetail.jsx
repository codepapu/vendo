import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getProduct } from '../services/api'
import './ProductDetail.css'

function ProductDetail() {
  const { id } = useParams()
  const { data: product, isLoading } = useQuery({
    queryKey: ['product', id],
    queryFn: () => getProduct(id)
  })

  if (isLoading) return <div className="loading">Loading product...</div>
  if (!product) return <div>Product not found</div>

  return (
    <div className="product-detail">
      <div className="product-detail-grid">
        <div className="product-image-section">
          <img 
            src={product.image || '/placeholder.jpg'} 
            alt={product.name}
            className="product-detail-image"
          />
        </div>
        <div className="product-info">
          <h1>{product.name}</h1>
          <p className="product-price">${product.price}</p>
          <p className="product-description">{product.description}</p>
          <button className="btn btn-primary">Add to Cart</button>
        </div>
      </div>
    </div>
  )
}

export default ProductDetail
