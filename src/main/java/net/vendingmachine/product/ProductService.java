package net.vendingmachine.product;

import net.vendingmachine.domain.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAllProducts();

    void create(Product product);

    Product findByProductSeller(long id,String sellerId);
    Optional<Product> findByProduct(long id);

    Product update(Product product);

    void delete(Product entity);
}
