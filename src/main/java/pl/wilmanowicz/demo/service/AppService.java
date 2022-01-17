package pl.wilmanowicz.demo.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AppService {

    private HashMap<String, String> getPagePrefix(){
        HashMap<String, String> pagesPrefix = new HashMap<>();
        pagesPrefix.put("olx","https://www.olx.pl/");
        pagesPrefix.put("otoDom","https://www.otodom.pl/pl/oferty/");
        pagesPrefix.put("sprzedajemy","https://sprzedajemy.pl/szukaj?");
        pagesPrefix.put("otoMoto", "https://www.otomoto.pl/");
        return pagesPrefix;
    }

    private HashMap<String, String> getPageNumber(int pageNumber){
        HashMap<String, String> pagesPrefix = new HashMap<>();
        pagesPrefix.put("olx","?page=" + pageNumber);
        pagesPrefix.put("otoDom","&page=" + pageNumber);
        pagesPrefix.put("otoMoto", "?page=" + pageNumber);
        if (pageNumber==1)
            pagesPrefix.put("sprzedajemy","?offset=0");
        else if (pageNumber==2){
            pagesPrefix.put("sprzedajemy","?offset=30");
        }
        else if (pageNumber==2){
            pagesPrefix.put("sprzedajemy","?offset=60");
        }
        return pagesPrefix;
    }

    private HashMap<String, String> getLocations(String city){
        HashMap<String, String> locations = new HashMap<>();
        locations.put("olx","");
        locations.put("otoDom","");
        locations.put("sprzedajemy","inp_location_id=0&");
        locations.put("otoMoto","");
        if (!city.isEmpty()){
            locations.replace("olx",city + "/");
            locations.replace("otoDom",city);
            locations.replace("otoMoto", city + "?search[dist]=25");
        }
        if (city.equals("gdansk")){
            locations.replace("sprzedajemy", "inp_location_id=58819&");
        }else if (city.equals("warszawa")){
            locations.replace("sprzedajemy", "inp_location_id=28024&");
        }else if (city.equals("krakow")){
            locations.replace("sprzedajemy", "inp_location_id=36906&");
        }
        return locations;
    }
    private HashMap<String, String> getCategories(String category){
        HashMap<String, String> categories = new HashMap<>();
        categories.put("olx","");
        categories.put("otoDom","");
        categories.put("sprzedajemy", "inp_category_id=0&");
        categories.put("otoMoto","");
        if (category.equals("mieszkania")){
            categories.replace("olx","nieruchomosci/mieszkania/sprzedaz/");
            categories.replace("otoDom", "sprzedaz/mieszkanie/");
            categories.replace("sprzedajemy", "inp_category_id=18502&");
        }else if (category.equals("motoryzacja")){
            categories.replace("olx","motoryzacja/samochody/");
            categories.replace("otoMoto", "osobowe/");
            categories.replace("sprzedajemy", "inp_category_id=6&");

        }
        return categories;
    }

    public Map<String, List<String>> searchOffers(String city, String category, int pageNumber){
        HashMap<String, String> pageNumber1 = getPageNumber(pageNumber);
        Map<String, List<String>> allData = new HashMap<>();
        HttpClient httpClient = getHttpClient();
        HashMap<String, String> pagePrefix = getPagePrefix();
        HashMap<String, String> categories = getCategories(category);
        HashMap<String, String> locations = getLocations(city);
        HashSet<String> olxLinks = pageScrape(locations.get("olx"), categories.get("olx"), httpClient,pagePrefix.get("olx"),
                "https://www.olx.pl/d/oferta/",".html", pageNumber1.get("olx"));
        Map<String, List<String>> olxData = scrapeInside(olxLinks, httpClient,
                "class=\"css-okktvh-Text eu5v0x0\"",
                "<!-- --> <!-- -->zł",
                "https://ireland.",
                ";"
        );
        allData.putAll(olxData);

        HashSet<String> sprzedajemy = pageScrape(locations.get("sprzedajemy"), categories.get("sprzedajemy"), httpClient, pagePrefix.get("sprzedajemy"),
                "<h2 class=\"title\">\n" +
                        "\n" +
                        "\t\t\t\t\t\t\t<a href=\"", "class=\"offerLink\"",
                pageNumber1.get("sprzedajemy"));

        Set<String> sprzedajemyLinks = sprzedajemy.stream()
                .map(link -> link.substring(link.indexOf("/"), link.indexOf("\" ")))
                .map(link -> "https://sprzedajemy.pl" + link)
                .collect(Collectors.toSet());

        Map<String, List<String>> sprzedajemyData = scrapeInside(sprzedajemyLinks, httpClient,
                "\"offerPrice\":\"",
                "\",",
                "https://thumbs.",
                "\""
        );
        allData.putAll(sprzedajemyData);

        if(category.equals("mieszkania")){
            HashSet<String> otoDom = pageScrape(locations.get("otoDom"), categories.get("otoDom"), httpClient,
                    pagePrefix.get("otoDom"), "/pl/oferta/", "\"",
                    pageNumber1.get("otoDom"));
            Set<String> otoDomLinks = otoDom.stream()
                    .map(link -> "https://www.otodom.pl" + link)
                    .map(link -> {
                        int i = link.indexOf("\"");
                        return link.substring(0,i);
                    })
                    .collect(Collectors.toSet());

            Map<String, List<String>> otoDomData = scrapeInside(otoDomLinks, httpClient,
                    "\"css-b114we eu6swcv14\">",
                    "</strong>",
                    "https://ireland.",
                    ";"
            );
            allData.putAll(otoDomData);

        }else if (category.equals("motoryzacja")){
            HashSet<String> otoMotoLinks = pageScrape(locations.get("otoMoto"),
                    categories.get("otoMoto"), httpClient, pagePrefix.get("otoMoto"),
                    "https://www.otomoto.pl/oferta/", ".html",
                    pageNumber1.get("otoMoto"));
            Map<String, List<String>> otoMotoData = scrapeInside(otoMotoLinks, httpClient,
                    "<div class=\"offer-price\" data-price=\"",
                    "\">",
                    "https://ireland.",
                    ";");
            allData.putAll(otoMotoData);
        }
        return allData;
    }

    private HashSet<String> pageScrape(String city, String category,
                                       HttpClient httpClient,String pagePrefix,
                                       String prefix, String suffix,
                                       String pageNumber){
        String page = parsePage(pagePrefix,category, city,pageNumber);
        HttpRequest httpRequest = getHttpRequest(page);
        String content = getHttpResponse(httpClient, httpRequest);
        HashSet<String> links = scrape(content, prefix, suffix);
        return links;
    }

    private String parsePage(String prefix,String category, String city, String page){
        String link = prefix +  category  + city + page;
        return link;
    }

    private HashSet<String> scrape(String content,String prefix, String suffix){
        HashSet<String> linkSet = new HashSet<>();
        for (int i = 0; i < content.length(); i++) {
            i = content.indexOf(prefix, i);
            if (i < 0) {
                break;
            }
            linkSet.add(content.substring(i).split(suffix)[0] + suffix);
        }
        return linkSet;
    }

    private Map<String, List<String>> scrapeInside(Set<String> links, HttpClient httpClient, String pricePrefix, String priceSuffix, String picturePrefix, String pictureSuffix){
        return links.stream()
                .collect(Collectors.toMap(Function.identity(),
                        link -> getInsideData(getHttpResponse(httpClient,getHttpRequest(link)),pricePrefix,priceSuffix,picturePrefix,pictureSuffix)
                ));
    }

    private List<String> getInsideData(String content, String pricePrefix, String priceSuffix, String picturePrefix, String pictureSuffix){
        int i = content.indexOf(pricePrefix);
        String zls = content.substring(i).split(priceSuffix)[0];
        String price = zls.split(pricePrefix)[1];

        i = content.indexOf(picturePrefix);
        String picture = content.substring(i).split(pictureSuffix)[0];
        List<String> list = Arrays.asList(price, picture);
        return list;
    }


    private HttpClient getHttpClient(){
        return HttpClient.newHttpClient();
    }

    private HttpRequest getHttpRequest(String page) {
        try{
            return HttpRequest.newBuilder()
                    .uri(new URI(page))
                    .GET()
                    .build();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private String getHttpResponse(HttpClient httpClient,HttpRequest httpRequest){
        try {
            HttpResponse<String> send = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return send.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
