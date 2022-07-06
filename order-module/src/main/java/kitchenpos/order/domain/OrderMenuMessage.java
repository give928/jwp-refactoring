package kitchenpos.order.domain;

public class OrderMenuMessage {
    private Long id;
    private String name;
    private Long price;

    public OrderMenuMessage() {
    }

    public OrderMenuMessage(Long id, String name, Long price) {
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
}
