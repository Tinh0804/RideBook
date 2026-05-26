package me.myproject.Utilities;

import java.net.URL;

public  class ImageAccess {
    private static final String pathImage="../IMAGE/";
    private static final String pathIcon="../IMAGE/ICON/";

    public static final URL image1 = ImageAccess.class.getResource(pathImage + "co.jpg");
    public static final String image2 = pathImage + "co.jpg";

    

}

