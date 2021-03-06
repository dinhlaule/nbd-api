package com.neo.nbdapi.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.nbdapi.dao.PaginationDAO;
import com.neo.nbdapi.dao.TidalHarmonicConstantsDAO;
import com.neo.nbdapi.dao.WaterLevelDAO;
import com.neo.nbdapi.dto.DefaultPaginationDTO;
import com.neo.nbdapi.dto.DefaultResponseDTO;
import com.neo.nbdapi.dto.FileWaterLevelInfo;
import com.neo.nbdapi.dto.GuessDataDTO;
import com.neo.nbdapi.entity.*;
import com.neo.nbdapi.exception.BusinessException;
import com.neo.nbdapi.rest.vm.DefaultRequestPagingVM;
import com.neo.nbdapi.rest.vm.WaterLevelExecutedVM;
import com.neo.nbdapi.rest.vm.WaterLevelVM;
import com.neo.nbdapi.services.WaterLevelService;
import com.neo.nbdapi.services.objsearch.WaterLevelSearch;
import com.neo.nbdapi.utils.Constants;
import com.neo.nbdapi.utils.FileFilter;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

@Service
public class WaterLevelServiceImpl implements WaterLevelService {

    private Logger logger = LogManager.getLogger(ConfigValueTypeServiceImpl.class);

    @Autowired
    private HikariDataSource ds;

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private PaginationDAO paginationDAO;

    @Autowired
    private WaterLevelDAO waterLevelDAO;

    @Autowired
    private TidalHarmonicConstantsDAO tidalHarmonicConstantsDAO;

    @Value("${water.level.file.out}")
    private String pathDirectory;

    private static Long timeTmp;

    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

    @Override
    public DefaultPaginationDTO getListWaterLevel(DefaultRequestPagingVM defaultRequestPagingVM) throws SQLException, BusinessException {
        logger.debug("defaultRequestPagingVM: {}", defaultRequestPagingVM);
        List<WaterLevel> waterLevels = new ArrayList<>();
        try (Connection connection = ds.getConnection()) {
            logger.debug("mailConfigVM: {}", defaultRequestPagingVM);
            // start = pageNumber, lenght = recordPerPage
            int pageNumber = Integer.parseInt(defaultRequestPagingVM.getStart());
            int recordPerPage = Integer.parseInt(defaultRequestPagingVM.getLength());
            String search = defaultRequestPagingVM.getSearch();

            StringBuilder sql = new StringBuilder("select w.id, w.ts_id, w.value, w.timestamp, w.status, w.manual, w.warning, w.create_user  from water_Level w inner join station_time_series s on s.ts_id = w.ts_id where 1 = 1 ");
            List<Object> paramSearch = new ArrayList<>();
            logger.debug("Object search: {}", search);
            // set value query to sql
            if (Strings.isNotEmpty(search)) {
                WaterLevelSearch objectSearch = objectMapper.readValue(search, WaterLevelSearch.class);
                if(objectSearch.getHours() != null){
                    if(objectSearch.getHours() ==1){
                        sql.append(" and TO_CHAR(TIMESTAMP,'MI')='00' ");
                    } else if(objectSearch.getHours() ==3){
                        sql.append(" and TO_CHAR(TIMESTAMP,'MI')='00' ");
                        sql.append(" and MOD(to_number(TO_CHAR(TIMESTAMP,'HH')),3) = 0 ");
                    } else if(objectSearch.getHours() ==24){
                        sql.append(" and TO_CHAR(TIMESTAMP,'MI')='00' ");
                        sql.append(" and TO_CHAR(TIMESTAMP,'HH24')='00' ");
                    }
                }
                if (Strings.isNotEmpty(objectSearch.getStationId())) {
                    sql.append(" AND s.station_id = ? ");
                    paramSearch.add(objectSearch.getStationId());
                }
                if (objectSearch.getId() != null) {
                    sql.append(" AND w.id = ? ");
                    paramSearch.add(objectSearch.getId());
                }
                if (objectSearch.getTsId() != null) {
                    sql.append(" AND w.ts_id = ? ");
                    paramSearch.add(objectSearch.getTsId());
                }
                if (objectSearch.getValue()!=null) {
                    sql.append(" AND w.value = ? ");
                    paramSearch.add(objectSearch.getValue());
                }
                if (Strings.isNotEmpty(objectSearch.getStartDate())) {
                    sql.append(" AND w.timestamp  >=  to_timestamp(?, 'DD/MM/YYYY') ");
                    paramSearch.add(objectSearch.getStartDate());
                }
                if (Strings.isNotEmpty(objectSearch.getEndDate())) {
                    sql.append(" AND w.timestamp -1 <  to_timestamp(?, 'DD/MM/YYYY') ");
                    paramSearch.add(objectSearch.getEndDate());
                }
                if (objectSearch.getStatus() != null) {
                    sql.append(" AND w.status = ? ");
                    paramSearch.add(objectSearch.getStatus());
                }
                if (objectSearch.getManual() != null) {
                    sql.append(" AND w.manual= ? ");
                    paramSearch.add(objectSearch.getManual());
                }
                if (objectSearch.getWarning()!=null) {
                    sql.append(" AND w.warning = ? ");
                    paramSearch.add(objectSearch.getWarning());
                }
                if (Strings.isNotEmpty(objectSearch.getCreateUser())) {
                    sql.append(" and w.create_user like ? ");
                    paramSearch.add(objectSearch.getCreateUser());
                }
            }
            sql.append(" ORDER BY w.id DESC ");
            logger.debug("NUMBER OF SEARCH : {}", paramSearch.size());
            ResultSet resultSetListData = paginationDAO.getResultPagination(connection, sql.toString(), pageNumber + 1, recordPerPage, paramSearch);

            while (resultSetListData.next()) {
                WaterLevel waterLevel = WaterLevel.builder().
                        id(resultSetListData.getLong("id"))
                        .tsId(resultSetListData.getLong("ts_id"))
                        .value(resultSetListData.getFloat("value"))
                        .timestamp(resultSetListData.getString("timestamp"))
                        .status(resultSetListData.getInt("status"))
                        .manual(resultSetListData.getInt("manual"))
                        .warning(resultSetListData.getInt("warning"))
                        .createUser(resultSetListData.getString("create_user"))
                        .build();

                waterLevels.add(waterLevel);
            }

            long total = paginationDAO.countResultQuery(sql.toString(), paramSearch);
            return DefaultPaginationDTO
                    .builder()
                    .draw(Integer.parseInt(defaultRequestPagingVM.getDraw()))
                    .recordsFiltered(waterLevels.size())
                    .recordsTotal(total)
                    .content(waterLevels)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return DefaultPaginationDTO
                    .builder()
                    .draw(Integer.parseInt(defaultRequestPagingVM.getDraw()))
                    .recordsFiltered(0)
                    .recordsTotal(0)
                    .content(waterLevels)
                    .build();
        }
    }

    @Override
    public DefaultResponseDTO updateWaterLevel(WaterLevelVM waterLevelVM) throws SQLException {
        List<Object> datas = waterLevelDAO.queryInformation(waterLevelVM);
        if(datas == null){
            return  DefaultResponseDTO.builder().status(0).message("L???i l???y ra c??c th??ng s??? ???????c c??i ?????t").build();
        }
        VariableTime variableTime = null;
        List<VariablesSpatial> variablesSpatials = null;
        Float nearest = null;

        variableTime = (VariableTime) datas.get(0);

        variablesSpatials = (List<VariablesSpatial>) datas.get(1);

        nearest = (Float) datas.get(2);
        Boolean continude = true;

        if(continude && variableTime != null && waterLevelVM.getValue() < variableTime.getMin()){
            waterLevelVM.setWarning(2);
            continude = false;
        }

        if(continude && variableTime != null && waterLevelVM.getValue() > variableTime.getMax()){
            waterLevelVM.setWarning(3);
            continude = false;
        }
        if(nearest!=null && variableTime != null && continude){
            Float percentTmp = waterLevelVM.getValue()/ nearest;
            Float percent = 100 - percentTmp*100;
            if(Math.abs(percent) > variableTime.getVariableTime() && continude){
                waterLevelVM.setWarning(4);
                continude = false;
            }
        }

        if(continude && variableTime!= null){
            float spatial = variableTime.getVariableSpatial();
            float tmp = waterLevelVM.getValue();
            for(VariablesSpatial variablesSpatial : variablesSpatials){
                if(variablesSpatial.getValue() == 0){
                    continue;
                }
                float percent = Math.abs(100 - (tmp/variablesSpatial.getValue())*100);
                if(percent>spatial){
                    continude = false;
                    waterLevelVM.setWarning(5);
                    break;
                }
            }
        }
        if(continude){
            waterLevelVM.setWarning(1);
        }
        return waterLevelDAO.updateWaterLevel(waterLevelVM);
    }

    @Override
    public DefaultResponseDTO executeWaterLevel(WaterLevelExecutedVM waterLevelExecutedVM) throws SQLException {
            String fileName = "/";
            String fileNameExecute = "";
        if(waterLevelExecutedVM.getStationId().equals(Constants.WATER_LEVEL.ID_PHU_QUOC)){
            fileName+=Constants.WATER_LEVEL.FILE_PHU_QUOC;
            fileNameExecute=Constants.WATER_LEVEL.FILE_PHU_QUOC;
        } else if(waterLevelExecutedVM.getStationId().equals(Constants.WATER_LEVEL.ID_GANH_HAO)){
            fileName+=Constants.WATER_LEVEL.FILE_GANH_HAO;
            fileNameExecute=Constants.WATER_LEVEL.FILE_GANH_HAO;
        } else if(waterLevelExecutedVM.getStationId().equals(Constants.WATER_LEVEL.ID_HA_TIEN)){
            fileName+=Constants.WATER_LEVEL.FILE_HA_TIEN;
            fileNameExecute=Constants.WATER_LEVEL.FILE_HA_TIEN;;
        } else{
            return DefaultResponseDTO.builder().status(0).message("Tr???m ch??a h???p l???").build();
        }


        if(waterLevelExecutedVM.getHours() == 1){
            fileName+="1h";
            fileNameExecute+="1h";
        } else if(waterLevelExecutedVM.getHours() == 3){
            fileName+="3h";
            fileNameExecute+="3h";
        }
        else if(waterLevelExecutedVM.getHours() == 24){
            fileName+="24h";
            fileNameExecute+="24h";
        } else{
            return DefaultResponseDTO.builder().status(0).message("Kho???ng th???i gian ch??a h???p l???").build();
        }
        this.timeTmp = 0L;

        List<WaterLevelExecute> waterLevels = waterLevelDAO.executeWaterLevel(waterLevelExecutedVM);
        if(waterLevels.size() == 0){
            return DefaultResponseDTO.builder().status(1).message("Th??nh c??ng").build();
        }

        try{
            String folderExport = Constants.WATER_LEVEL.FOLDER_EXPORT;



            PrintWriter print = new PrintWriter(new File(folderExport + fileName + ".ip"));

            WaterLevelExecute firstTmp = waterLevels.get(0);

            Calendar calendarFirst = convertStringToCalender(firstTmp);

            StringBuilder title = new StringBuilder("     1 ");
            title.append(calendarFirst.get(Calendar.YEAR));
            print.println(title.toString());

            for (WaterLevelExecute waterLevelExecute: waterLevels) {
                int position = waterLevels.indexOf(waterLevelExecute);
                if(position==0){
                    print.println(lineWithDate(waterLevelExecute, null));
                } else {
                    WaterLevelExecute waterLevelExecuteBefore = waterLevels.get(position-1);
                    if(convertStringToCalender(waterLevelExecute).get(Calendar.DAY_OF_MONTH) !=  convertStringToCalender(waterLevelExecuteBefore).get(Calendar.DAY_OF_MONTH)){
                        print.println(lineWithDate(waterLevelExecute, waterLevelExecuteBefore));
                    } else{
                        print.println(line(waterLevelExecute, waterLevelExecuteBefore));
                    }
                }

            }
            print.flush();
            print.close();

            // s??? d???ng restemplate ????? th???c hi???n t??nh h???ng s??? ??i???u h??a

            String command = "echo "+fileNameExecute+".par | ./tt_phantich_v1_2";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            Map<String, Object> map = new HashMap<>();
            map.put("commandExecute", command);
            map.put("stationId", waterLevelExecutedVM.getStationId());
            map.put("fileName", fileName+".ip");

            // build the request
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(Constants.WATER_LEVEL.URL_EXECUTE, entity, String.class);
            String dataResponse = response.getBody();
            DataResponse object = objectMapper.readValue(dataResponse, DataResponse.class);
            tidalHarmonicConstantsDAO.insertTidalHarmonicConstantsDAOs(object.getTidalHarmonicConstantes());
            return DefaultResponseDTO.builder().status(1).message(object.getResponse()).build();

        }
         catch (IOException | ParseException e) {
            logger.error("WaterLevelServiceImpl exception : {} ", e.getMessage());
            return DefaultResponseDTO.builder().status(0).message(e.getMessage()).build();
        }
    }

    private String lineWithDate(WaterLevelExecute waterLevelExecute, WaterLevelExecute waterLevelExecuteBefore) throws ParseException {
        Calendar calendar = convertStringToCalender(waterLevelExecute);
        if(waterLevelExecuteBefore != null){
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            Calendar calendarBefore = convertStringToCalender(waterLevelExecuteBefore);
            long tmp = calendar.getTimeInMillis() - calendarBefore.getTimeInMillis();
            this.timeTmp = this.timeTmp + tmp/1000;

        }
        StringBuilder line = new StringBuilder("");
        line.append(timeTmp+". ");
        line.append(waterLevelExecute.getValue());
        line.append(" \t");
        line.append(calendar.get(Calendar.YEAR));
        line.append(" ");
        line.append(calendar.get(Calendar.MONTH)+1);
        line.append(" ");
        line.append(calendar.get(Calendar.DAY_OF_MONTH));
        line.append(" ");
        line.append(calendar.get(Calendar.HOUR));
        line.append(" ");
        line.append(calendar.get(Calendar.MINUTE));
        line.append(" ");
        line.append(calendar.get(Calendar.SECOND));
        return line.toString();
    }
    private String line(WaterLevelExecute waterLevelExecute, WaterLevelExecute waterLevelExecuteBefore) throws ParseException {
        Calendar calendar = convertStringToCalender(waterLevelExecute);
        if(waterLevelExecuteBefore != null){
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            Calendar calendarBefore = convertStringToCalender(waterLevelExecuteBefore);
            long tmp = calendar.getTimeInMillis() - calendarBefore.getTimeInMillis();
            this.timeTmp = this.timeTmp + tmp/1000;

        }
        StringBuilder line = new StringBuilder("");
        line.append(timeTmp+". ");
        line.append(waterLevelExecute.getValue());
        return line.toString();
    }
    private Calendar convertStringToCalender(WaterLevelExecute tmp) throws  ParseException{
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        Date dateFirst = formatter.parse(tmp.getTimestamp());
        Calendar calendarFirst = Calendar.getInstance();
        calendarFirst.setTime(dateFirst);
        return calendarFirst;

    }

    @Override
    public DefaultResponseDTO executeGuess(String stationId, Integer end, Integer start, MultipartFile file, String type) throws IOException {
        File fileOut = new File(pathDirectory + Constants.WATER_LEVEL.FILE_EXECUTE_GUESS);
        BufferedOutputStream bos = null;
        // th???c hi???n check file;
        Pattern pattern = Pattern.compile(Constants.WATER_LEVEL.REGEX_FILE_UPLOAD);
        String filName = file.getOriginalFilename();
        Matcher matcher = pattern.matcher(filName);
        if(!matcher.matches()){
            return DefaultResponseDTO.builder().status(0).message("File kh??ng h???p l???").build();
        }
        if(file.getSize() > 102400){
            return DefaultResponseDTO.builder().status(0).message("Dung l?????ng file qu?? l???n").build();
        }

        try{
            // th???c hi???n ghi file
            byte datas[] = file.getBytes();
            bos = new BufferedOutputStream(new FileOutputStream(fileOut));
            bos.write(datas);
            bos.flush();

        } catch (IOException e){
            e.printStackTrace();
            return DefaultResponseDTO.builder().status(0).message(e.getMessage()).build();
        }
        finally {
            if(bos!= null){
                bos.close();
           }
        }
        // th???c hi???n thay ?????i c???u h??nh
        // ?????c file config
        BufferedReader readConfig = null;
        List<String> dataConfigs = new ArrayList<>();
        try{
            File fileConfig  = new File(pathDirectory+ Constants.WATER_LEVEL.FILE_CONFIG);
            readConfig = new BufferedReader(new FileReader(fileConfig));
            String lineConF = "";
            while ((lineConF = readConfig.readLine()) != null) {
                dataConfigs.add(lineConF);
            }
            fileConfig.delete();

        } catch (Exception e){
            e.printStackTrace();
            return DefaultResponseDTO.builder().status(0).message(e.getMessage()).build();
        } finally {
            if(readConfig!= null){
                readConfig.close();
            }
        }
        // ghi l???i file config
        PrintWriter writeConfig = null;
        try{
             writeConfig = new PrintWriter(new FileWriter(new File(pathDirectory+ Constants.WATER_LEVEL.FILE_CONFIG)));
            String fileNameConf = "";
            if(stationId.equals(Constants.WATER_LEVEL.ID_HA_TIEN)){
                fileNameConf = Constants.WATER_LEVEL.FILE_HA_TIEN;
            } else if(stationId.equals(Constants.WATER_LEVEL.ID_GANH_HAO)){
                fileNameConf = Constants.WATER_LEVEL.FILE_GANH_HAO;
            } else if(stationId.equals(Constants.WATER_LEVEL.ID_PHU_QUOC)){
                fileNameConf = Constants.WATER_LEVEL.FILE_PHU_QUOC;
            } else {
                return  DefaultResponseDTO.builder().status(0).message("Tr???m kh??ng h???p l???").build();
            }

            writeConfig.println(fileNameConf.toUpperCase()+ ".log");
            writeConfig.println(fileNameConf.toUpperCase()+ ".tab");
            writeConfig.println(fileNameConf.toUpperCase());
            writeConfig.println(start);
            writeConfig.println(end);
            writeConfig.println(type);
            writeConfig.println(fileNameConf.toUpperCase()+".tsr");
            int i =0;
            for( String tmp : dataConfigs){
                if( i > 6){
                    writeConfig.println(tmp);
                }
                i++;
            }
            writeConfig.flush();

            // th???c hi???n call xu???ng service

            String command = "./tt_dubao_v2";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
            map.add("execute", command);
            map.add("fileName", ("/"+fileNameConf.toUpperCase()+ ".tab"));

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

            ResponseEntity<String> response = restTemplate.postForEntity( Constants.WATER_LEVEL.URL_GUESS, request , String.class );
            String dataResponse = response.getBody();
            List<GuessDataDTO> guessDataDTOs = objectMapper.readValue(dataResponse, new TypeReference<List<GuessDataDTO>>(){});

            //Ki???m tra file log xem ch???y ????ng hay sai
            FileInputStream fileInputStream = null;
            BufferedReader bufferedReader = null;
            int k = 0;
            try{
                fileInputStream = new FileInputStream((pathDirectory.toUpperCase()+"/"+fileNameConf.toUpperCase()+ ".log"));
                bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                String line = bufferedReader.readLine();
                while (line != null) {
                    k++;
                    System.out.println(line);
                    line = bufferedReader.readLine();
                }

            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    bufferedReader.close();
                    fileInputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if(k<=3){
                return  DefaultResponseDTO.builder().message("Th???c thi h???ng s??? ??i???u h??a kh??ng th??nh c??ng").status(0).build();
            }

            return  waterLevelDAO.insertTidalPrediction(guessDataDTOs, stationId);
        }catch (Exception e){
            e.printStackTrace();
            return  DefaultResponseDTO.builder().status(0).message(e.getMessage()).build();
        } finally {
            writeConfig.close();
        }

    }

    @Override
    public ResponseEntity<InputStreamResource> downloadTemplate(HttpServletRequest request) throws IOException, BusinessException {
        HttpHeaders responseHeader = new HttpHeaders();
        String filename = request.getParameter("filename");
        try {
            File file =  new File(pathDirectory+"template.dat");
            byte[] data = FileUtils.readFileToByteArray(file);
            // Set mimeType tr??? v???
            responseHeader.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // Thi???t l???p th??ng tin tr??? v???
            responseHeader.set("Content-disposition", "attachment; filename=" + file.getName());
            responseHeader.setContentLength(data.length);
            InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(data));
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
            return new ResponseEntity<InputStreamResource>(inputStreamResource, responseHeader, HttpStatus.OK);
        } catch (Exception ex) {
            throw new BusinessException("File d??? li???u kh??ng t???n t???i");
        }
    }
    public List<FileWaterLevelInfo> getInfoFileWaterLevelInfo() throws IOException {
        File directory = new File(pathDirectory);
        File[] fileList = directory.listFiles(new FileFilter("*.hg"));
        List<FileWaterLevelInfo> fileWaterLevelInfos = new ArrayList<>();
        for (File f : fileList) {
            Path file = Paths.get(f.getPath());
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
            FileWaterLevelInfo fileWaterLevelInfo = FileWaterLevelInfo.builder().fileName(f.getName().trim()).modifyDate(new Date(attr.creationTime().toMillis())).build();
            fileWaterLevelInfos.add(fileWaterLevelInfo);
        }
        return fileWaterLevelInfos;
    }

    public List<FileWaterLevelInfo> getInfoFileGuess() throws IOException {
        File directory = new File(pathDirectory);
        File[] fileList = directory.listFiles(new FileFilter("*.tsr"));
        File[] fileListTab = directory.listFiles(new FileFilter("*.tab"));
        List<FileWaterLevelInfo> fileWaterLevelInfos = new ArrayList<>();
        for (File f : fileList) {
            Path file = Paths.get(f.getPath());
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
            FileWaterLevelInfo fileWaterLevelInfo = FileWaterLevelInfo.builder().fileName(f.getName().trim()).modifyDate(new Date(attr.creationTime().toMillis())).build();
            fileWaterLevelInfos.add(fileWaterLevelInfo);
        }
        for (File f : fileListTab) {
            Path file = Paths.get(f.getPath());
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
            FileWaterLevelInfo fileWaterLevelInfo = FileWaterLevelInfo.builder().fileName(f.getName().trim()).modifyDate(new Date(attr.creationTime().toMillis())).build();
            fileWaterLevelInfos.add(fileWaterLevelInfo);
        }
        return fileWaterLevelInfos;

    }
}