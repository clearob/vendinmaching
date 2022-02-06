package net.vendingmachine.product;

import net.vendingmachine.domain.Product;
import net.vendingmachine.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Service
public class ProducteServiceImpl implements ProductService {
    //protected  JpaRepository<T, Long> getRepository();

    private final ProductRepository productRepository;

    @Autowired
    public ProducteServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PersistenceContext
    private EntityManager entitymanager;

    @Override
    public List<Product> getAllProducts() {
/*
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("ProductRepository");
        EntityManager em = factory.createEntityManager();
        Query q = em.createQuery("from Product e", Product.class);
        return em.createNamedQuery("Product.findAll").getResultList();
 */
        //  return  productRepository.findAll();
        return entitymanager.createQuery("from Product", Product.class).getResultList();
    }

    @Override
    public void create(Product product) {
        productRepository.save(product);
    }

    @Override
    public Product findByProductSeller(long id, String sellerId) {
        return (Product) entitymanager.createQuery("select u from Product u " +
                        "where u.id=:id " +
                        "and u.sellerId=:sellerid")
                .setParameter("id", id)
                .setParameter("sellerid", sellerId).getSingleResult();


    }

    @Override
    public Optional<Product> findByProduct(long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product update(Product product) {
        //Product prod = findByProductSeller(product.getId(), product.getSellerId());
        //LOGGER.info();
        return productRepository.save(product);
    }

    @Override
    public void delete(Product product) {
        productRepository.delete(product);
    }
}
