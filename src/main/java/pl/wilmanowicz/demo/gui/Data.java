package pl.wilmanowicz.demo.gui;

import java.net.URL;

public class Data {
    private final URL link;
    private final String picture;
    private final String price;

    public Data(URL link, String price, String picture) {
        this.link = link;
        this.picture = picture;
        this.price = price;
    }

    public URL getLink() {
        return link;
    }

    public String getPicture() {
        return picture;
    }

    public String getPrice() {
        return price;
    }
}
