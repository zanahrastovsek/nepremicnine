import java.util.StringJoiner;

/**
 * Created by zana on 08/12/16.
 */
public class RealEstate {
    private String referenceNumber;
    private String macroLocation;
    private String microLocation;
    private String municipality;
    private String realEstate;
    private String dealType;
    private String area;
    private String constructionYear;
    private String adaptationYear;
    private String landArea;
    private String floor;
    private String floorTotal;
    private String energyClass;
    private String description;
    private String price;

    private RealEstate(Builder builder) {
        this.referenceNumber = builder.referenceNumber;
        this.macroLocation = builder.macroLocation;
        this.microLocation = builder.microLocation;
        this.municipality = builder.municipality;
        this.realEstate = builder.realEstate;
        this.dealType = builder.dealType;
        this.area = builder.area;
        this.constructionYear = builder.constructionYear;
        this.adaptationYear = builder.adaptationYear;
        this.landArea = builder.landArea;
        this.floor = builder.floor;
        this.floorTotal = builder.floorTotal;
        this.energyClass = builder.energyClass;
        this.description = builder.description;
        this.price = builder.price;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getMacroLocation() {
        return macroLocation;
    }

    public String getMicroLocation() {
        return microLocation;
    }

    public String getMunicipality() {
        return municipality;
    }

    public String getRealEstate() {
        return realEstate;
    }

    public String getDealType() {
        return dealType;
    }

    public String getArea() {
        return area;
    }

    public String getConstructionYear() {
        return constructionYear;
    }

    public String getAdaptationYear() {
        return adaptationYear;
    }

    public String getLandArea() {
        return landArea;
    }

    public String getFloor() {
        return floor;
    }

    public String getFloorTotal() {
        return floorTotal;
    }

    public String getEnergyClass() {
        return energyClass;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public static class Builder {
        private String referenceNumber = "";
        private String macroLocation = "";
        private String microLocation = "";
        private String municipality = "";
        private String realEstate = "";
        private String dealType = "";
        private String area = "";
        private String constructionYear = "";
        private String adaptationYear = "";
        private String landArea = "";
        private String floor = "";
        private String floorTotal = "";
        private String energyClass = "";
        private String description = "";
        private String price = "";

        public Builder() {
        }

        public Builder referenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
            return this;
        }

        public Builder macroLocation(String macroLocation) {
            this.macroLocation = macroLocation;
            return this;
        }

        public Builder microLocation(String microLocation) {
            this.microLocation = microLocation;
            return this;
        }

        public Builder municipality(String municipality) {
            this.municipality = municipality;
            return this;
        }

        public Builder realEstate(String realEstate) {
            this.realEstate = realEstate;
            return this;
        }

        public Builder dealType(String dealType) {
            this.dealType = dealType;
            return this;
        }

        public Builder area(String surface) {
            this.area = surface;
            return this;
        }

        public Builder constructionYear(String constructionYear) {
            this.constructionYear = constructionYear;
            return this;
        }

        public Builder adaptationYear(String adaptationYear) {
            this.adaptationYear = adaptationYear;
            return this;
        }

        public Builder landArea(String landArea) {
            this.landArea = landArea;
            return this;
        }

        public Builder floor(String floor) {
            this.floor = floor;
            return this;
        }

        public Builder floorTotal(String floorTotal) {
            this.floorTotal = floorTotal;
            return this;
        }

        public Builder energyClass(String energyClass) {
            this.energyClass = energyClass;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder price(String price) {
            this.price = price;
            return this;
        }

        public RealEstate build() {
            return new RealEstate(this);
        }
    }

    public static String getCsvHeader() {
        return "Referenčna št.;Regija;Upravna enota;Občina;Vrsta nepremičnine;Vrsta posredovanja;Leto izgradnje;Leto adaptacije;Površina;Površina zemljišča;Nadstropje;Št. nadstropij;Energijski razred;Cena;Opis";
    }

    public boolean allElementsAreEmpty() {
        return getReferenceNumber().isEmpty() && getMacroLocation().isEmpty() && getMicroLocation().isEmpty() && getMunicipality().isEmpty() &&
               getRealEstate().isEmpty() && getDealType().isEmpty() && getArea().isEmpty() && getConstructionYear().isEmpty() &&
               getAdaptationYear().isEmpty() &&
               getLandArea().isEmpty() && getFloor().isEmpty() && getFloorTotal().isEmpty() &&
               getEnergyClass().isEmpty() && getDescription().isEmpty() && getPrice().isEmpty();
    }

    public String toCsv() {
        return new StringJoiner(";").add(getReferenceNumber())
                .add(getMacroLocation())
                .add(getMicroLocation())
                .add(getMunicipality())
                .add(getRealEstate())
                .add(getDealType())
                .add(getConstructionYear())
                .add(getAdaptationYear())
                .add(getArea())
                .add(getLandArea())
                .add(getFloor())
                .add(getFloorTotal())
                .add(getEnergyClass())
                .add(getPrice())
                .add(getDescription())
                .toString();
    }
}