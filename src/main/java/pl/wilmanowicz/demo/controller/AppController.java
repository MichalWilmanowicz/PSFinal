package pl.wilmanowicz.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.wilmanowicz.demo.service.AppService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AppController {

    private final AppService appService;

    @Autowired
    public AppController(AppService appService) {
        this.appService = appService;
    }
//     @GetMapping
//     @ResponseBody
//     public Map<String, List<String>> getOffers(@PathVariable String city, @PathVariable String category, @PathVariable Integer pageNumber){
// //        String city = "gdansk";
// //        String category = "mieszkania";
//         Map<String, List<String>> allData = appService.searchOffers(city, category, pageNumber);
//         return allData;

//     }
    @GetMapping
    @ResponseBody
    public Map<String, List<String>> getOffers(){
        String city = "gdansk";
        String category = "mieszkania";
        Integer i = 1;
        Map<String, List<String>> allData = appService.searchOffers(city, category, pageNumber);
        return allData;

    }
}
