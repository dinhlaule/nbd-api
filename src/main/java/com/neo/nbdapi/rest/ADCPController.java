package com.neo.nbdapi.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.nbdapi.dao.PaginationDAO;
import com.neo.nbdapi.dto.DefaultPaginationDTO;
import com.neo.nbdapi.dto.DefaultResponseDTO;
import com.neo.nbdapi.entity.ADCP;
import com.neo.nbdapi.exception.BusinessException;
import com.neo.nbdapi.rest.vm.DefaultRequestPagingVM;
import com.neo.nbdapi.services.StationManagementService;
import com.neo.nbdapi.utils.Constants;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(Constants.APPLICATION_API.API_PREFIX + "/manual-input")
public class ADCPController {
    private Logger logger = LogManager.getLogger(StationTypeController.class);

    private Logger loggerAction = LogManager.getLogger("ActionCrud");

    @Autowired
    private HikariDataSource ds;

    @Autowired
    private PaginationDAO paginationDAO;

    @Autowired
    private StationManagementService stationManagementService;

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper objectMapper;

    @PostMapping("/get-list-adcp-pagination")
    public DefaultPaginationDTO getListADCPPagination(@RequestBody @Valid DefaultRequestPagingVM defaultRequestPagingVM) throws SQLException, BusinessException {

        logger.debug("defaultRequestPagingVM: {}", defaultRequestPagingVM);
        try (Connection connection = ds.getConnection()) {
            int pageNumber = Integer.parseInt(defaultRequestPagingVM.getStart());
            int recordPerPage = Integer.parseInt(defaultRequestPagingVM.getLength());
            String search = defaultRequestPagingVM.getSearch();

            StringBuilder sql = new StringBuilder("select a.*,liss.TOTAL_TURB,liss.SUSPENDED_MATERIAL, b.station_code, b.STATION_NAME, r.RIVER_ID, r.RIVER_NAME, d.OBJECT_TYPE, d.OBJECT_TYPE_SHORTNAME from adcp a, liss , stations b, stations_object_type c, OBJECT_TYPE d , RIVERS r \n" +
                    "where a.STATION_ID = liss.STATION_ID(+) and a.WATER_FLOW = liss.WATER_FLOW(+) and a.STATION_ID = b.STATION_ID and b.river_id = r.river_id and a.STATION_ID = c.STATION_ID and c.OBJECT_TYPE_ID = d.OBJECT_TYPE_ID ");
            List<Object> paramSearch = new ArrayList<>();
            if (Strings.isNotEmpty(search)) {
                try {
                    Map<String, String> params = objectMapper.readValue(search, Map.class);
                    if (Strings.isNotEmpty(params.get("s_objectType"))) {
                        sql.append(" and d.OBJECT_TYPE = ? ");
                        paramSearch.add(params.get("s_objectType"));
                    }
                    if (Strings.isNotEmpty(params.get("s_objectTypeName"))) {
                        sql.append(" and lower(d.OBJECT_TYPE_SHORTNAME) like lower(?) ");
                        paramSearch.add("%" + params.get("s_objectTypeName") + "%");
                    }
                    if (Strings.isNotEmpty(params.get("s_stationCode"))) {
                        sql.append(" and b.station_code = ? ");
                        paramSearch.add(params.get("s_stationCode"));
                    }

                    if (Strings.isNotEmpty(params.get("s_stationName"))) {
                        sql.append(" and lower(b.STATION_NAME) like lower(?) ");
                        paramSearch.add("%" + params.get("s_stationName") + "%");
                    }
                    if (Strings.isNotEmpty(params.get("s_riverName"))) {
                        sql.append(" and r.river_name like ? ");
                        paramSearch.add("%" + params.get("s_riverName") + "%");
                    }
                    if (Strings.isNotEmpty(params.get("s_timeStart"))) {
                        sql.append(" and a.TIME_START >= to_date(?,'DD/MM/YYYY HH24:MI') ");
                        paramSearch.add(params.get("s_timeStart"));
                    }
                    if (Strings.isNotEmpty(params.get("s_timeEnd"))) {
                        sql.append(" and a.time_end <= to_date(?,'DD/MM/YYYY HH24:MI') ");
                        paramSearch.add(params.get("s_timeEnd"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sql.append(" order by a.CREATED_AT desc ");
            log.info(sql.toString());
            logger.debug("NUMBER OF SEARCH : {}", paramSearch.size());
            ResultSet rs = paginationDAO.getResultPagination(connection, sql.toString(), pageNumber + 1, recordPerPage, paramSearch);
            List<ADCP> list = new ArrayList<>();

            while (rs.next()) {
                ADCP bo = ADCP.builder()
                        .id(rs.getLong("ID"))
                        .stationId(rs.getString("STATION_ID"))
                        .objectType(rs.getString("OBJECT_TYPE"))
                        .objectName(rs.getString("OBJECT_TYPE_SHORTNAME"))
                        .stationCode(rs.getString("station_code"))
                        .stationName(rs.getString("STATION_NAME"))
                        .riverName(rs.getString("RIVER_NAME"))
                        .timeStart(rs.getDate("TIME_START"))
                        .timeEnd(rs.getDate("TIME_END"))
                        .timeAvg(rs.getDate("TIME_AVG"))
                        .waterLevelStart(rs.getFloat("WATER_LEVEL_START"))
                        .waterLevelEnd(rs.getFloat("WATER_LEVEL_END"))
                        .waterLevelAvg(rs.getFloat("WATER_LEVEL_AVG"))
                        .speedAvg(rs.getFloat("SPEED_AVG"))
                        .speedMax(rs.getFloat("SPEED_MAX"))
                        .deepAvg(rs.getFloat("DEEP_AVG"))
                        .deepMax(rs.getFloat("DEEP_MAX"))
                        .squareRiver(rs.getFloat("SQUARE_RIVER"))
                        .widthRiver(rs.getFloat("WIDTH_RIVER"))
                        .waterFlow(rs.getFloat("WATER_FLOW"))
                        .note(rs.getString("NOTE"))
                        .linkFile(rs.getString("LINK_FILE"))
                        .createdAt(rs.getDate("CREATED_AT"))
                        .totalTurb(rs.getFloat("TOTAL_TURB"))
                        .suspendedMaterial(rs.getFloat("SUSPENDED_MATERIAL"))
                        .build();
                list.add(bo);
            }

            rs.close();
            // count result
            long total = paginationDAO.countResultQuery(sql.toString(), paramSearch);
            return DefaultPaginationDTO
                    .builder()
                    .draw(Integer.parseInt(defaultRequestPagingVM.getDraw()))
                    .recordsFiltered(list.size())
                    .recordsTotal(total)
                    .content(list)
                    .build();
        }
    }

    @PostMapping("/create-adcp")
    public DefaultResponseDTO createADCP(HttpServletRequest request, @RequestParam Map<String, String> params, @RequestParam("linkFile") MultipartFile[] file) throws SQLException, JsonProcessingException {
        log.info(objectMapper.writeValueAsString(params) +  file.length);

        loggerAction.info("{};{}", "create-adcp", objectMapper.writeValueAsString(params));
        //tao thu muc luu tru file
        File f = new File(request.getRealPath("") + "/upload");
        if(!f.exists()){
            f.mkdir();
        }
        String linkFile = null;
        if(file[0].getSize() > 0) {
            linkFile = "upload/" + file[0].getOriginalFilename();
            String path = request.getRealPath("") + linkFile;
            f = new File(path);
            try (OutputStream os = new FileOutputStream(f)) {
                os.write(file[0].getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //luu cac thong tin con lai vao bang
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql1 = "select decode(max(MEASURE_NTH),null,0,max(MEASURE_NTH)) from adcp where station_id = ?";
        String sql = "insert into adcp(ID,STATION_ID,TIME_START,TIME_END,TIME_AVG,WATER_LEVEL_START,WATER_LEVEL_END," +
                "WATER_LEVEL_AVG,SPEED_AVG,SPEED_MAX,DEEP_AVG,DEEP_MAX,SQUARE_RIVER,WIDTH_RIVER,WATER_FLOW,NOTE,CREATED_AT,CREATED_BY,LINK_FILE,MEASURE_NTH)\n" +
                "values(adcp_seq.nextval,?,to_date(?,'dd/MM/yyyy HH24:MI'),to_date(?,'dd/MM/yyyy HH24:MI'),to_date(?,'dd/MM/yyyy HH24:MI'),?,?,?,?,?,?,?,?,?,?,?,sysdate,?,?,?)";
        try (Connection connection = ds.getConnection();
             PreparedStatement st = connection.prepareStatement(sql1);
             PreparedStatement statement = connection.prepareStatement(sql);) {
            st.setString(1, params.get("stationId"));
            ResultSet rs = st.executeQuery();
            rs.next();
            Integer num = rs.getInt(1);
            int i = 1;
            statement.setString(i++, params.get("stationId"));
            statement.setString(i++, params.get("timeStart"));
            statement.setString(i++, params.get("timeEnd"));
            statement.setString(i++, params.get("timeAvg"));

            statement.setString(i++, params.get("waterLevelStart"));
            statement.setString(i++, params.get("waterLevelEnd"));
            statement.setString(i++, params.get("waterLevelAvg"));
            statement.setString(i++, params.get("speedAvg"));
            statement.setString(i++, params.get("speedMax"));

            statement.setString(i++, params.get("deepAvg"));
            statement.setString(i++, params.get("deepMax"));
            statement.setString(i++, params.get("squareRiver"));
            statement.setString(i++, params.get("widthRiver"));

            statement.setString(i++, params.get("waterFlow"));
            statement.setString(i++, params.get("note"));
            statement.setString(i++, params.get("username"));
            if(linkFile != null) {
                statement.setString(i++, linkFile);
            }else{
                statement.setString(i++, null);
            }
            statement.setString(i++, String.valueOf(num + 1));
            statement.execute();

            defaultResponseDTO.setStatus(1);
            defaultResponseDTO.setMessage("Th??m m???i th??nh c??ng");
            return defaultResponseDTO;
        } catch (Exception e) {
            log.error(e.getMessage());
            defaultResponseDTO.setStatus(0);
            defaultResponseDTO.setMessage("Th??m m???i th???t b???i: " + e.getMessage());
            return defaultResponseDTO;
        }
    }

    @PostMapping("/update-adcp")
    public DefaultResponseDTO updateADCP(HttpServletRequest request, @RequestParam Map<String, String> params, @RequestParam("linkFile") MultipartFile[] file) throws SQLException, JsonProcessingException {
        log.info(objectMapper.writeValueAsString(params) +  file.length);

        loggerAction.info("{};{}", "update-adcp", objectMapper.writeValueAsString(params));
        //tao thu muc luu tru file
        File f = new File(request.getRealPath("") + "/upload");
        if(!f.exists()){
            f.mkdir();
        }
        String linkFile = null;
        if(file[0].getSize() > 0) {
            linkFile = "upload/" + file[0].getOriginalFilename();
            String path = request.getRealPath("") + linkFile;
            f = new File(path);
            try (OutputStream os = new FileOutputStream(f)) {
                os.write(file[0].getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //luu cac thong tin con lai vao bang
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql = "update adcp set TIME_START = to_date(?,'DD/MM/YYYY HH24:MI'),TIME_END = to_date(?,'DD/MM/YYYY HH24:MI'),TIME_AVG = to_date(?,'DD/MM/YYYY HH24:MI'),WATER_LEVEL_START = ?,WATER_LEVEL_END = ?," +
                "WATER_LEVEL_AVG = ?,SPEED_AVG = ?,SPEED_MAX = ?,DEEP_AVG = ?,DEEP_MAX = ?,SQUARE_RIVER = ?,WIDTH_RIVER = ?,WATER_FLOW = ?,NOTE = ?,UPDATED_BY = ?, UPDATED_AT = sysdate,LINK_FILE = ?\n" +
                "where id =?";
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            int i = 1;
//            statement.setString(i++, params.get("stationId"));
            statement.setString(i++, params.get("timeStart"));
            statement.setString(i++, params.get("timeEnd"));
            statement.setString(i++, params.get("timeAvg"));

            statement.setString(i++, params.get("waterLevelStart"));
            statement.setString(i++, params.get("waterLevelEnd"));
            statement.setString(i++, params.get("waterLevelAvg"));
            statement.setString(i++, params.get("speedAvg"));
            statement.setString(i++, params.get("speedMax"));

            statement.setString(i++, params.get("deepAvg"));
            statement.setString(i++, params.get("deepMax"));
            statement.setString(i++, params.get("squareRiver"));
            statement.setString(i++, params.get("widthRiver"));

            statement.setString(i++, params.get("waterFlow"));
            statement.setString(i++, params.get("note"));
            statement.setString(i++, params.get("username"));
            if(linkFile != null) {
                statement.setString(i++, linkFile);
            }else{
                if(params.get("linkFileName") != null){
                    statement.setString(i++, params.get("linkFileName"));
                }else {
                    statement.setString(i++, null);
                }
            }
            statement.setString(i++, params.get("id"));
            statement.execute();

            defaultResponseDTO.setStatus(1);
            defaultResponseDTO.setMessage("C???p nh???t th??nh c??ng");
            return defaultResponseDTO;
        } catch (Exception e) {
            log.error(e.getMessage());
            defaultResponseDTO.setStatus(0);
            defaultResponseDTO.setMessage("C???p nh???t th???t b???i: " + e.getMessage());
            return defaultResponseDTO;
        }
    }

    @PostMapping("/delete-adcp")
    public DefaultResponseDTO deleteADCP(@RequestParam("id") String id){

        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql = "delete adcp where id =?";
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            int i = 1;
            statement.setString(i++, id);
            statement.execute();

            defaultResponseDTO.setStatus(1);
            defaultResponseDTO.setMessage("X??a th??nh c??ng");
            return defaultResponseDTO;
        } catch (Exception e) {
            log.error(e.getMessage());
            defaultResponseDTO.setStatus(0);
            defaultResponseDTO.setMessage("X??a th???t b???i: " + e.getMessage());
            return defaultResponseDTO;
        }
    }
}
