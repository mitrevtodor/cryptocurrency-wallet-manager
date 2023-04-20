package bg.sofia.uni.fmi.mjt.walletmanager;


import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Cryptocurrency {
    @SerializedName("asset_id")
    private final String id;
    @SerializedName("price_usd")
    private final double priceBought;
    @SerializedName("type_is_crypto")
    private final int isCrypto;

    public Cryptocurrency(String id, double priceBought, int isCrypto) {
        this.id = new String(id);
        this.priceBought = priceBought;
        this.isCrypto = isCrypto;
    }

    public String getId() {
        return id;
    }

    public boolean isCrypto() {
        return isCrypto == 1;
    }

    public double getPriceBought() {
        return priceBought;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cryptocurrency that = (Cryptocurrency) o;
        return Double.compare(that.priceBought, priceBought) == 0 && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, priceBought);
    }

    @Override
    public String toString() {
        return "Cryptocurrency{" +
            "id='" + id + '\'' +
            ", priceBought=" + priceBought +
            '}';
    }
}