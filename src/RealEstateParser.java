import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by zana on 06/12/16.
 */
public class RealEstateParser {
    //settings
    private static String url;
    private static int offset;
    private static int numberOfPages;
    private static String pathToOutputFile;
    private static String pathToInputFileWithReferenceNumbers;

    //constants
    private static final String HREF_ATTRIBUTE = "href";
    private static final String ADD_HREF_PARENT_STYLE_CLASS = "slika";
    private static final String ENERGY_CLASS_STYLE_CLASS = "ei";
    private static final String DESCRIPTION_STYLE_CLASS = "web-opis";
    private static final String MORE_INFO_STYLE_CLASS = "more_info";
    private static final String SHORT_DESCRIPTION_STYLE_CLASS = "kratek";
    private static final String SHORT_DESCRIPTION_CHILD_STYLE_CLASS = "rdeca";
    private static final String BASE_URL = "https://www.nepremicnine.net/";

    private static final String LABEL_STYLE_CLASS = "lbl";
    private static final String LABEL_REFERENCE_NUMBER = "Referenčna št.:";
    private static final String LABEL_MACRO_REGION = "Regija: ";
    private static final String LABEL_MICRO_REGION = "Upravna enota: ";
    private static final String LABEL_MUNICIPALITY = "Obč\u008Dina: ";
    private static final String LABEL_REAL_ESTATE_TYPE = "Vrsta: ";
    private static final String LABEL_DEAL_TYPE = "Posredovanje: ";
    private static final String LABEL_AREA = " m2";
    private static final String LABEL_LAND_AREA = " m2 zemlj";
    private static final String LABEL_BUILD_YEAR = " l. ";
    private static final String LABEL_FLOOR = " nad.";
    private static final String LABEL_PRICE = "Cena: ";

    private Set<String> existingRealEstates = new HashSet<>();

    static {
        Properties properties = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream("../out/nepremicnine/nastavitve.properties");
            properties.load(input);
            url = properties.getProperty("url");
            offset = Integer.valueOf(properties.getProperty("offset"));
            numberOfPages = Integer.valueOf(properties.getProperty("number_of_pages"));
            pathToOutputFile = properties.getProperty("path_to_output_file");
            pathToInputFileWithReferenceNumbers = properties.getProperty("path_to_input_file_with_reference_numbers");
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to read properties. Error: " + e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Set<RealEstate> realEstates = new HashSet<>();
        RealEstateParser realEstateParser = new RealEstateParser();
        realEstateParser.readExistingRealEstates();
        for (int i = offset; i < numberOfPages; i++) {
            Logger.getAnonymousLogger().log(Level.INFO, "Starting to parse page number: " + i);
            String pageSource = RealEstateParserUtils.getPageSource(getPageUrl(url, i));
            Set<RealEstate> result = realEstateParser.parseRealEstateAdds(realEstateParser.parseRealEstateAddPage(Jsoup.parse(pageSource)));
            Logger.getAnonymousLogger().log(Level.INFO, "Page done. Number of adds parsed: " + result.size());
            realEstates.addAll(result);
        }
        realEstateParser.writeToFile(realEstates);
    }

    private static String getPageUrl(String baseUrl, int pageIndex) {
        if (pageIndex != 1) {
            baseUrl = baseUrl.contains(".html") ? baseUrl.replace(".html", "/" + pageIndex + "/") : baseUrl.concat("/" + pageIndex + "/");
        }
        return baseUrl;
    }

    private Set<Document> parseRealEstateAddPage(Document mainDocument) {
        Set<Document> addDocuments = new HashSet<>();
        Elements addPhotoElements = RealEstateParserUtils.getElementsForClass(mainDocument, ADD_HREF_PARENT_STYLE_CLASS);
        for (Element addPhoto : addPhotoElements) {
            if (!addPhoto.hasAttr(HREF_ATTRIBUTE)) {
                continue;
            }
            addDocuments.add(Jsoup.parse(RealEstateParserUtils.getPageSource(BASE_URL + addPhoto.attr(HREF_ATTRIBUTE))));
        }
        return addDocuments;
    }

    Set<RealEstate> parseRealEstateAdds(Set<Document> addDocuments) {
        return addDocuments.stream().map(this::parseRealEstateAdd).filter(result -> result != null).collect(Collectors.toSet());
    }

    private RealEstate parseRealEstateAdd(Document addDocument) {
        try {
            String realEstateReferenceNumber = parseReferenceNumber(addDocument);
            boolean newRealEstate = realEstateReferenceNumber != null && existingRealEstates.add(realEstateReferenceNumber);
            if (!newRealEstate) {
                return null;
            }

            RealEstate.Builder realEstateBuilder = new RealEstate.Builder();
            realEstateBuilder.referenceNumber(realEstateReferenceNumber);
            Element moreInfo = RealEstateParserUtils.getElementsForClass(addDocument, MORE_INFO_STYLE_CLASS).first();
            if (moreInfo != null) {
                String moreInfoText = moreInfo.text();
                parseMacroRegion(realEstateBuilder, moreInfoText);
                parseMicroRegion(realEstateBuilder, moreInfoText);
                parseMunicipality(realEstateBuilder, moreInfoText);
                parseRealEstateType(realEstateBuilder, moreInfoText);
                parseDealType(realEstateBuilder, moreInfoText);
            }

            Element shortDescription = RealEstateParserUtils.getElementsForClass(addDocument, SHORT_DESCRIPTION_STYLE_CLASS)
                    .stream()
                    .filter(el -> el.children().size() != 0 && el.child(0).hasClass(SHORT_DESCRIPTION_CHILD_STYLE_CLASS))
                    .findFirst()
                    .orElse(null);
            if (shortDescription != null) {
                String shortDescriptionText = shortDescription.text();
                parseArea(realEstateBuilder, shortDescriptionText);
                parseLandArea(realEstateBuilder, shortDescriptionText);
                parseBuildYear(realEstateBuilder, shortDescriptionText);
                parseFloor(realEstateBuilder, shortDescriptionText);
                parsePrice(realEstateBuilder, shortDescriptionText);
            }

            Elements energyClass = RealEstateParserUtils.getElementsForClass(addDocument, ENERGY_CLASS_STYLE_CLASS);
            realEstateBuilder.energyClass(energyClass != null ? energyClass.text() : null);

            Elements description = RealEstateParserUtils.getElementsForClass(addDocument, DESCRIPTION_STYLE_CLASS);
            realEstateBuilder.description(description != null ? description.text() : null);
            return realEstateBuilder.build();
        } catch (RuntimeException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Parsing an add resulted in fatal crash. Error: " + e.getMessage());
        }
        return null;
    }

    private void parseMacroRegion(RealEstate.Builder realEstateBuilder, String moreInfoText) {
        int macroRegionStart = moreInfoText.indexOf(LABEL_MACRO_REGION);
        if (macroRegionStart != -1) {
            macroRegionStart += LABEL_MACRO_REGION.length();
            realEstateBuilder.macroLocation(moreInfoText.substring(macroRegionStart, moreInfoText.indexOf(" |", macroRegionStart)));
        }
    }

    private void parseMicroRegion(RealEstate.Builder realEstateBuilder, String moreInfoText) {
        int microRegionStart = moreInfoText.indexOf(LABEL_MICRO_REGION);
        if (microRegionStart != -1) {
            microRegionStart += LABEL_MICRO_REGION.length();
            realEstateBuilder.microLocation(moreInfoText.substring(microRegionStart, moreInfoText.indexOf(" |", microRegionStart)));
        }
    }

    private void parseMunicipality(RealEstate.Builder realEstateBuilder, String moreInfoText) {
        int municipalityStart = moreInfoText.indexOf(LABEL_MUNICIPALITY);
        if (municipalityStart != -1) {
            municipalityStart += LABEL_MUNICIPALITY.length();
            realEstateBuilder.municipality(moreInfoText.substring(municipalityStart));
        }
    }

    private void parseRealEstateType(RealEstate.Builder realEstateBuilder, String moreInfoText) {
        int realEstateTypeStart = moreInfoText.indexOf(LABEL_REAL_ESTATE_TYPE);
        if (realEstateTypeStart != -1) {
            realEstateTypeStart += LABEL_REAL_ESTATE_TYPE.length();
            realEstateBuilder.realEstateType(moreInfoText.substring(realEstateTypeStart, moreInfoText.indexOf(" |", realEstateTypeStart)));
        }
    }

    private void parseDealType(RealEstate.Builder realEstateBuilder, String moreInfoText) {
        int dealTypeStart = moreInfoText.indexOf(LABEL_DEAL_TYPE);
        if (dealTypeStart != -1) {
            dealTypeStart += LABEL_DEAL_TYPE.length();
            realEstateBuilder.dealType(moreInfoText.substring(dealTypeStart, moreInfoText.indexOf(" |", dealTypeStart)));
        }
    }

    private void parseArea(RealEstate.Builder realEstateBuilder, String shortDescriptionText) {
        int areaStart = 0;
        int areaEnd = shortDescriptionText.indexOf(LABEL_AREA);
        if (areaEnd != -1) {
            for (int i = areaEnd - 1; i > 0; i--) {
                if (shortDescriptionText.charAt(i) == ' ') {
                    areaStart = i + 1;
                    break;
                }
            }
            realEstateBuilder.area(shortDescriptionText.substring(areaStart, areaEnd));
        }
    }

    private void parseLandArea(RealEstate.Builder realEstateBuilder, String shortDescriptionText) {
        int landAreaStart = 0;
        int landAreaEnd = shortDescriptionText.indexOf(LABEL_LAND_AREA);
        if (landAreaEnd != -1) {
            for (int i = landAreaEnd - 1; i > 0; i--) {
                if (shortDescriptionText.charAt(i) == ' ') {
                    landAreaStart = i + 1;
                    break;
                }
            }
            realEstateBuilder.landArea(shortDescriptionText.substring(landAreaStart, landAreaEnd));
        }
    }

    private void parseFloor(RealEstate.Builder realEstateBuilder, String shortDescriptionText) {
        int floorStart = 0;
        int floorEnd = shortDescriptionText.indexOf(LABEL_FLOOR);
        if (floorEnd != -1) {
            for (int i = floorEnd - 1; i > 0; i--) {
                if (shortDescriptionText.charAt(i) == '/') {
                    floorStart = i + 1;
                    realEstateBuilder.floorTotal(shortDescriptionText.substring(floorStart, floorEnd));
                    floorEnd = floorStart - 1;
                } else if (shortDescriptionText.charAt(i) == ' ') {
                    floorStart = i + 1;
                    break;
                }
            }
            realEstateBuilder.floor(shortDescriptionText.substring(floorStart, floorEnd).replace(".", ""));
        }
    }

    private void parseBuildYear(RealEstate.Builder realEstateBuilder, String shortDescriptionText) {
        int buildYearStart = shortDescriptionText.indexOf(LABEL_BUILD_YEAR);
        if (buildYearStart != -1) {
            buildYearStart += LABEL_BUILD_YEAR.length();
            realEstateBuilder.constructionYear(shortDescriptionText.substring(buildYearStart, shortDescriptionText.indexOf(",", buildYearStart)));
        }
    }

    private void parsePrice(RealEstate.Builder realEstateBuilder, String shortDescriptionText) {
        int priceStart = shortDescriptionText.indexOf(LABEL_PRICE);
        if (priceStart != -1) {
            priceStart += LABEL_PRICE.length();
            realEstateBuilder.price(shortDescriptionText.substring(priceStart));
        }
    }

    private String parseReferenceNumber(Document addDocument) {
        Elements labels = RealEstateParserUtils.getElementsForClass(addDocument, LABEL_STYLE_CLASS);
        Element referenceNumberLabel = labels.stream().filter(label -> label.text().equals(LABEL_REFERENCE_NUMBER)).findFirst().orElse(null);
        return referenceNumberLabel != null ? referenceNumberLabel.nextElementSibling().text() : null;
    }

    private void writeToFile(Set<RealEstate> realEstates) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(pathToOutputFile, "UTF-8");
            writer.println(RealEstate.getCsvHeader());
            for (RealEstate realEstate : realEstates) {
                if (realEstate.allElementsAreEmpty()) {
                    continue;
                }
                writer.println(realEstate.toCsv());
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void readExistingRealEstates() {
        try {
            try (Stream<String> stream = Files.lines(Paths.get(pathToInputFileWithReferenceNumbers))) {
                stream.forEach(referenceNumber -> existingRealEstates.add(referenceNumber));
            }
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to read existing real estates. Error: " + e.getMessage());
        }
    }
}
