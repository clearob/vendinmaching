package net.vendingmachine.domain;

import javax.persistence.*;

@Entity
@Table(name = "product")

//@NamedQuery(name="product.findAll", query="SELECT e FROM product e")
public class Product {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;


    @Column(name = "productname", nullable = false)
    private String productName;

    @Column(name = "amountavailable", nullable = false)
    private long amountAvailable;

    @Column(name = "cost", nullable = false)
    private long cost;

    @Column(name = "sellerid", nullable = false)
    private String sellerId;

    public Long getId() {
        return id;
    }



    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getAmountAvailable() {
        return amountAvailable;
    }

    public void setAmountAvailable(long amountAvailable) {
        this.amountAvailable = amountAvailable;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }


}
