package com.project.wwg.plan.service;

import com.project.wwg.plan.dao.SpotDao;
import com.project.wwg.plan.dto.Spot;
import com.project.wwg.plan.exceptions.NotAvailableDataException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpotServiceImpl implements SpotService {

    private final SpotDao spotDao;
    private final JSONParser parser = new JSONParser();
    private List<Spot> spots;

    @Autowired
    public SpotServiceImpl(SpotDao spotDao) {
        this.spotDao = spotDao;
    }

    // ----------------------------- Spot CRUD -----------------------------

    /**
     * 검색어로 Spot 검색하여 list로 반환
     *
     * @param keyword
     * @return
     */
    @Override
    public List<Spot> searchSpots(String keyword) {
        return spotDao.searchSpots(keyword);
    }

    /**
     * Spot 1개 등록
     *
     * @param spot
     * @return
     */
    @Override
    public void insertSpot(Spot spot) {
        spotDao.insertSpot(spot);
    }

    /**
     * Spot 여러 개 등록
     *
     * @param spots
     * @return
     */
    @Override
    public int insertSpots(List<Spot> spots) {
        return spotDao.insertSpots(spots);
    }

    /**
     * id로 Spot 1개 삭제
     *
     * @param id
     */
    @Override
    public void deleteSpot(String id) {
        spotDao.deleteSpot(id);
    }

    /**
     * 모든 Spot 삭제
     *
     * @return
     */
    @Override
    public int deleteAllSpots() {
        return spotDao.deleteAllSpots();
    }

    // ------------------------------ API 관련 ------------------------------

    /**
     * API의 모든 Spots DB에 저장
     *
     * @return 저장된 Item의 개수 리턴
     */
    @Override
    public int resetAllSpots() {
        spots = new ArrayList<Spot>();
        int result = 0;
        try {
            int pageCount = getPageCountFromApi();
            if (pageCount == 0) {
                throw new NotAvailableDataException("데이터가 존재하지 않습니다.");
            }
            for (int i = 1; i <= pageCount; i++) {
                jsonToList(searchSpotsFromApi(i));
            }
            deleteAllSpots();
            result = spotDao.insertSpots(spots);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * API에서 1페이지 Spots 조회
     *
     * @return
     */
    @Override
    public String searchSpotsFromApi() {
        return searchSpotsFromApi(1);
    }

    /**
     * API에서 n페이지 Spots 조회
     *
     * @param page
     * @return json형식의 String 리턴
     */
    @Override
    public String searchSpotsFromApi(int page) {
        StringBuilder sb = new StringBuilder();

        String host = "https://api.visitjeju.net";
        String path = "/vsjApi/contents/searchList";
        String apiKey = "sh2krg8tnt28ayuk";
        String locale = "kr";
        String params = "?apiKey=" + apiKey + "&locale=" + locale + "&page=";

        try {
            URL url = new URL(host + path + params + page);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * API에서 1페이지 조회하여 pageCount값 찾기
     *
     * @return json형식의 String 리턴
     */
    @Override
    public int getPageCountFromApi() {
        String response = searchSpotsFromApi();
        int pageCount = 0;
        try {
            JSONObject responseObj = (JSONObject) parser.parse(response);
            pageCount = ((Long) responseObj.get("pageCount")).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pageCount;
    }

    /**
     * 여러 Spot들이 담긴 String response를 파싱하여 List에 담기
     *
     * @param response
     * @return Item들이 담긴 List 객체
     */
    @Override
    public void jsonToList(String response) {
        try {
            JSONObject responseObj = (JSONObject) parser.parse(response);
            JSONArray jsonArray = (JSONArray) responseObj.get("items");

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject currentObj = (JSONObject) jsonArray.get(i);

                String title = (String) currentObj.get("title");
                String info = (String) currentObj.getOrDefault("introduction", "");
                double lat = (double) currentObj.getOrDefault("latitude", 0);
                double lng = (double) currentObj.getOrDefault("longitude", 0);
                String address = (String) currentObj.getOrDefault("roadaddress", "");

//                if (currentObj.get("latitude") != null) {
//                    latD = (Double) currentObj.get("latitude");
//                    lat = new BigDecimal(latD);
//                }
//                if (currentObj.get("longitude") != null) {
//                    lngD = (Double) currentObj.get("longitude");
//                    lng = new BigDecimal(lngD);
//                }

                String photo = "";
                JSONObject photo1 = (JSONObject) currentObj.get("repPhoto");
                if (photo1 != null) {
                    JSONObject photo2 = (JSONObject) photo1.get("photoid");
                    if (photo2 != null) {
                        photo = (String) photo2.get("imgpath");
                    }
                }

                String phone = (String) currentObj.getOrDefault("phoneno", "");
                String id = (String) currentObj.getOrDefault("phoneno", "");

                Spot spot = new Spot();
                spots.add(spot);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
