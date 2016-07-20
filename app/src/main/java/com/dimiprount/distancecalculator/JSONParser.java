package com.dimiprount.distancecalculator;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JSONParser {

    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {        // Draw route and find distance and duration

        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes, jLegs, jSteps;
        JSONObject jDistance, jDuration;

        try {

            jRoutes = jObject.getJSONArray("routes");

            // Select and draw in the path everything
            // Traverse all routes
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");

                List<HashMap<String, String>> path = new ArrayList<>();

                // Traverse all legs
                for (int j = 0; j < jLegs.length(); j++) {

                    // Get distance from the json data
                    jDistance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");
                    HashMap<String, String> hmDistance = new HashMap<>();
                    hmDistance.put("distance", jDistance.getString("text"));

                    // Getting duration from the json data
                    jDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");
                    HashMap<String, String> hmDuration = new HashMap<>();
                    hmDuration.put("duration", jDuration.getString("text"));

                    path.add(hmDistance);       // Add distance object to the path

                    path.add(hmDuration);       // Add duration object to the path

                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    // Traverse all steps
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List list = decodePoly(polyline);

                        // Traverse all points
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                }
                routes.add(path);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

        return routes;
    }


    // Method to decode polyline points
    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;// result |= B: assigns to result variable the result of perform a logical OR operation between result itself and the result of B operation. This means: perform a logical OR between result and the last 5 bits of b shifted to the left an amount of shift bits, and then assign the result to result variable.
                // b & 0x1f: Performs a logical AND operation between b and 0xf1. This means: return the last 5 bits of b
                // A << shift: shifts to the left an amount of shift bits the result of A operation. This means: shift the last 5 bits of b an amount of shift bits to the left.
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
