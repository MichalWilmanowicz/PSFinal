// package pl.wilmanowicz.demo.gui;

// import com.vaadin.flow.component.Unit;
// import com.vaadin.flow.component.button.Button;
// import com.vaadin.flow.component.grid.Grid;
// import com.vaadin.flow.component.html.Image;
// import com.vaadin.flow.component.html.Label;
// import com.vaadin.flow.component.orderedlayout.VerticalLayout;
// import com.vaadin.flow.component.textfield.TextField;
// import com.vaadin.flow.data.provider.ListDataProvider;
// import com.vaadin.flow.router.Route;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.core.ParameterizedTypeReference;
// import org.springframework.http.*;
// import org.springframework.web.client.RestTemplate;
// import pl.wilmanowicz.demo.service.AppService;

// import java.net.MalformedURLException;
// import java.net.URL;
// import java.util.LinkedHashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// @Route("test")
// public class AppGui extends VerticalLayout {

//     private final AppService appService;
//     private static  boolean page1 = false;
//     private static  boolean page2 = false;


//     @Autowired
//     public AppGui(AppService appService) {
//         this.appService = appService;
//         TextField textFieldCity = new TextField("miasto");
//         TextField textFieldCategory = new TextField("kategoria");
//         Button button = new Button("Zatwierdz");
//         Button button1 = new Button("Strona 2");
//         Button button2 = new Button("Strona 3");

//         button1.addClickListener(buttonClickEvent -> {
//             page1 = true;
//             page2 = false;
//         });
//         button2.addClickListener(buttonClickEvent -> {
//             page1 = false;
//             page2 = true;

//         });
//         button.addClickListener(buttonClickEvent -> {
//             Integer pageNumber = 0;
//             if (page1==true){
//                 pageNumber = 1;
//             }else if (page2==true){
//                 pageNumber = 2;
//             }
//             String city = textFieldCity.getValue();
//             String category = textFieldCategory.getValue();

// //            RestTemplate restTemplate = new RestTemplate();
// //            ParameterizedTypeReference<LinkedHashMap<String, List<String>>> responseType =
// //                    new ParameterizedTypeReference<>() {
// //                    };
// //            ResponseEntity<LinkedHashMap<String, List<String>>> exchange = restTemplate.exchange("http://localhost:8081/api", ///{city}/{category}
// //                    HttpMethod.GET,
// //                    HttpEntity.EMPTY,
// //                    responseType
// //            );
// //            LinkedHashMap<String, List<String>> body = exchange.getBody();

//             Map<String, List<String>> body = appService.searchOffers(city, category,pageNumber);

//             List<Data> collect = body.entrySet().stream()
//                     .map(stringListEntry -> {
//                         try {
//                             return new Data(new URL(stringListEntry.getKey()),
//                                     stringListEntry.getValue().get(0),
//                                     stringListEntry.getValue().get(1)
//                             );
//                         } catch (MalformedURLException e) {
//                             e.printStackTrace();
//                             return null;
//                         }
//                     })
//                     .collect(Collectors.toList());

//             Grid<Data> grid = new Grid<>(Data.class,false);
//             ListDataProvider<Data> dataProvider = new ListDataProvider<Data>(collect);
//             dataProvider.refreshAll();
//             grid.setDataProvider(dataProvider);

//             grid.addColumn(Data::getLink).setHeader("Link");
//             grid.addColumn(Data::getPrice).setHeader("Cena");
//             grid.addComponentColumn(data -> {
//                 Image image = new Image(data.getPicture(),"brak zdjęcia");
//                 image.setHeight(200,Unit.PIXELS);
//                 image.setWidth(250,Unit.PIXELS);
//                 return image;
//             });
//             grid.setWidth(100, Unit.PERCENTAGE);
//             grid.setHeight(2000,Unit.PIXELS);

//             add(button1,button2,grid);
//         });
//         add(textFieldCity,textFieldCategory,button);
//     }

// }
