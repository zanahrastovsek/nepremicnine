/**
 * Created by zana on 07/12/16.
 */
public enum RealEstateType {
    APARTMENT("Stanovanje"),
    HOUSE("Hiša"),
    VACATION_HOUSE("Vikend"),
    LAND("Posest"),
    OFFICE("Poslovni prostor"),
    GARAGE("Garaža"),
    VACATION_BUILDING("Počitniški objekt");

    private String name;

    RealEstateType(String name) {
        this.name = name;
    }
}