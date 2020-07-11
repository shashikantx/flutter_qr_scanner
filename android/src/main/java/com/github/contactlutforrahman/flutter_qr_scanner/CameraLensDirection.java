package com.github.contactlutforrahman.flutter_qr_scanner;

import java.util.HashMap;
import java.util.Map;

public enum CameraLensDirection {
    Front("front"),
    Back("back"),
    External("external");

    private String value;

    CameraLensDirection(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    //****** Reverse Lookup Implementation************//

    //Lookup table
    private static final Map<String, CameraLensDirection> lookup = new HashMap<>();

    //Populate the lookup table on loading time
    static
    {
        for(CameraLensDirection env : CameraLensDirection.values())
        {
            lookup.put(env.getValue(), env);
        }
    }

    //This method can be used for reverse lookup purpose
    public static CameraLensDirection get(String value)
    {
        return lookup.get(value);
    }

}
