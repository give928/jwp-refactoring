package kitchenpos.order.dto;

import kitchenpos.order.domain.OrderMenu;

public class MenuResponse {
    private Long id;
    private String name;
    private Long price;

    public MenuResponse() {
    }

    public MenuResponse(Long id, String name, Long price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getPrice() {
        return price;
    }

    public OrderMenu toOrderMenu() {
        return OrderMenu.of(id, name, price);
    }

    @Override
    public String toString() {
        return "MenuResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
