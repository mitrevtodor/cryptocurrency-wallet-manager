package bg.sofia.uni.fmi.mjt.walletmanager;

public class CurrencyInfo {
    private final String id;
    private final double quantity;
    private final double profit;

    private StringBuilder partialBuild() {
        StringBuilder builder = new StringBuilder();
        builder.append("id:").append(id).append(" ").append("quantity:").append(quantity);
        return builder;
    }

    public CurrencyInfo(Cryptocurrency cryptocurrency, double quantity, double currentPrice) {
        this.id = new String(cryptocurrency.getId());
        this.quantity = quantity;
        this.profit = quantity * (currentPrice - cryptocurrency.getPriceBought());
    }

    public String toStringWithoutProfitInfo() {
        return partialBuild().append(System.lineSeparator()).toString();
    }

    public String toStringWithProfitInfo() {
        return partialBuild().append(" ").append("profit:").append(profit)
            .append(System.lineSeparator()).toString();
    }

    //getters for testing purposes
    public String getId() {
        return id;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getProfit() {
        return profit;
    }
}
