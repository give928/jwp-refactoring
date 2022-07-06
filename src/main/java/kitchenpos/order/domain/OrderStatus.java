package kitchenpos.order.domain;

public enum OrderStatus {
    COOKING, MEAL, COMPLETION;

    public boolean isCookingOrMeal() {
        return isCooking() || isMeal();
    }

    private boolean isMeal() {
        return this == MEAL;
    }

    private boolean isCooking() {
        return this == COOKING;
    }
}
