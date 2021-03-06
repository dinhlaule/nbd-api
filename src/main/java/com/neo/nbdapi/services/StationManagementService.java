package com.neo.nbdapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.nbdapi.dto.DefaultPaginationDTO;
import com.neo.nbdapi.dto.DefaultResponseDTO;
import com.neo.nbdapi.entity.Station;
import com.neo.nbdapi.exception.BusinessException;
import com.neo.nbdapi.rest.vm.CreateMailConfigVM;
import com.neo.nbdapi.rest.vm.DefaultRequestPagingVM;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.validation.Valid;

@Component
@Slf4j
public class StationManagementService {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static String[] HEADERs = {"Id", "Title", "Description", "Published"};
    private static String SHEET = "Tutorials";

    @Autowired
    private HikariDataSource ds;

    @Autowired
//    @Qualifier("objectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    CommonService commonService;

    DefaultPaginationDTO getListMailConfigPagination(DefaultRequestPagingVM defaultRequestPagingVM) throws SQLException, BusinessException {
        DefaultPaginationDTO result = null;
        return result;
    }

    DefaultResponseDTO createMailConfig(CreateMailConfigVM createMailConfigVM) throws SQLException {
        DefaultResponseDTO result = null;
        return result;
    }

    public DefaultResponseDTO saveOrUpdateStationTimeSeriesPLSQL(Map<String, String> params, boolean isNew) throws SQLException, JsonProcessingException {
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql = "";
        if (isNew) {
            sql = "begin ? := STATION.create_station_series_times(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?); end;";
        } else {
            sql = "begin ? := STATION.update_station_series_times(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?); end;";
        }
        try (Connection con = ds.getConnection(); CallableStatement st = con.prepareCall(sql);) {
            log.info(objectMapper.writeValueAsString(params));
            int i = 2;
            if (!isNew) {
                st.setString(i++, params.get("stationId"));
            }
            st.setString(i++, params.get("tsId"));
            st.setString(i++, params.get("tsName"));
            st.setString(i++, params.get("tsTypeId"));
            st.setString(i++, params.get("stationTypeId"));
            st.setString(i++, params.get("parameterTypeId"));
            st.setString(i++, params.get("paramterTypeName"));
            st.setString(i++, params.get("parameterTypeDes"));
            st.setString(i++, params.get("stationCode"));
            st.setString(i++, params.get("stationName"));
            st.setString(i++, params.get("stationLongName"));
            st.setString(i++, params.get("longtitude"));
            st.setString(i++, params.get("latitude"));
            st.setString(i++, params.get("catchementId"));
            st.setString(i++, params.get("catchementName"));
            st.setString(i++, params.get("areaId"));
            st.setString(i++, params.get("areaName"));
            st.setString(i++, params.get("siteId"));
            st.setString(i++, params.get("siteName"));
            st.setString(i++, params.get("riverId"));
            st.setString(i++, params.get("riverName"));
            st.setString(i++, params.get("provinceId"));
            st.setString(i++, params.get("provinceName"));
            st.setString(i++, params.get("districtId"));
            st.setString(i++, params.get("districtName"));
            st.setString(i++, params.get("wardId"));
            st.setString(i++, params.get("wardName"));
            st.setString(i++, params.get("countryId"));
            st.setString(i++, params.get("countryName"));
            st.setString(i++, params.get("projectId"));
            st.setString(i++, params.get("projectName"));
            st.setString(i++, params.get("pstorage"));
            st.setString(i++, params.get("address"));
            st.setString(i++, params.get("uuid"));
            st.setString(i++, params.get("modeStationType"));
            st.setString(i++, params.get("username"));
            st.setString(i++, params.get("status"));
            st.setString(i++, params.get("staffStation"));
            st.registerOutParameter(1, Types.VARCHAR);
            st.execute();
            String result = st.getString(1);
            if (Objects.equals(result, "OK")) {
                defaultResponseDTO.setStatus(1);
                if (isNew) {
                    defaultResponseDTO.setMessage("Th??m m???i th??nh c??ng");
                } else {
                    defaultResponseDTO.setMessage("C???p nh???t th??nh c??ng");
                }
            } else {
                defaultResponseDTO.setStatus(0);
                defaultResponseDTO.setMessage(result);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            defaultResponseDTO.setStatus(0);
            if (isNew) {
                defaultResponseDTO.setMessage("L???i khi th??m m???i: " + e.getMessage());
            } else {
                defaultResponseDTO.setMessage("L???i khi c???p nh???t: " + e.getMessage());
            }
            return defaultResponseDTO;
        }
        return defaultResponseDTO;
    }

    public DefaultResponseDTO saveOrUpdateTimeSeriesConfigParameterPLSQL(Map<String, String> params, boolean isNew) throws SQLException, JsonProcessingException {
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql = "";
        if (isNew) {
            sql = "begin ? := STATION.create_time_series_config_parameter(?,?,?,?,?); end;";
        } else {
            sql = "begin ? := STATION.update_time_series_config_parameter(?,?,?,?,?,?); end;";
        }
        try (Connection con = ds.getConnection(); CallableStatement st = con.prepareCall(sql);) {
            log.info(objectMapper.writeValueAsString(params));
            int i = 2;
            if (!isNew) {
                st.setString(i++, params.get("parameterId"));
            }
            st.setString(i++, params.get("parameter"));
            st.setString(i++, params.get("parameterDesc"));
            st.setString(i++, params.get("unitId"));
            st.setString(i++, params.get("uuid"));
            st.setString(i++, params.get("username"));
            st.registerOutParameter(1, Types.VARCHAR);
            st.execute();
            String result = st.getString(1);
            if (Objects.equals(result, "OK")) {
                defaultResponseDTO.setStatus(1);
                if (isNew) {
                    defaultResponseDTO.setMessage("Th??m m???i th??nh c??ng");
                } else {
                    defaultResponseDTO.setMessage("C???p nh???t th??nh c??ng");
                }
            } else {
                defaultResponseDTO.setStatus(0);
                defaultResponseDTO.setMessage(result);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            defaultResponseDTO.setStatus(0);
            if (isNew) {
                defaultResponseDTO.setMessage("L???i khi th??m m???i: " + e.getMessage());
            } else {
                defaultResponseDTO.setMessage("L???i khi c???p nh???t: " + e.getMessage());
            }
            return defaultResponseDTO;
        }
        return defaultResponseDTO;
    }

    public DefaultResponseDTO deleteStationTimeSeriesPLSQL(String stationId) throws SQLException, JsonProcessingException {
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql = "begin ? := STATION.delete_station_series_times(?); end;";
        try (Connection con = ds.getConnection(); CallableStatement st = con.prepareCall(sql);) {
            st.setString(2, stationId);
            st.registerOutParameter(1, Types.VARCHAR);
            st.execute();

            defaultResponseDTO.setStatus(1);
            defaultResponseDTO.setMessage("X??a th??nh c??ng");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            defaultResponseDTO.setStatus(0);
            defaultResponseDTO.setMessage("L???i khi x??a: " + e.getMessage());
            return defaultResponseDTO;
        }
        return defaultResponseDTO;
    }

    public DefaultResponseDTO deleteTimeSeriesConfigParameterPLSQL(String parameterId) throws SQLException, JsonProcessingException {
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql = "begin ? := STATION.delete_time_series_config_parameter(?); end;";
        try (Connection con = ds.getConnection(); CallableStatement st = con.prepareCall(sql);) {
            st.setString(2, parameterId);
            st.registerOutParameter(1, Types.VARCHAR);
            st.execute();

            defaultResponseDTO.setStatus(1);
            defaultResponseDTO.setMessage("X??a th??nh c??ng");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            defaultResponseDTO.setStatus(0);
            defaultResponseDTO.setMessage("L???i khi x??a: " + e.getMessage());
            return defaultResponseDTO;
        }
        return defaultResponseDTO;
    }

    public DefaultResponseDTO createStationTimeSeries(Map<String, String> params) throws SQLException {
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();

        Connection con = null;
        try {
            con = ds.getConnection();
            con.setAutoCommit(false);
            //save station
            defaultResponseDTO = saveStation(con, params);
            if (!Objects.equals(defaultResponseDTO.getMessage(), "OK")) {
                return defaultResponseDTO;
            }

            //save station type
            params.put("stationId", Integer.toString(defaultResponseDTO.getStatus()));
            defaultResponseDTO = saveStationTypeObject(con, params);
            if (!Objects.equals(defaultResponseDTO.getMessage(), "OK")) {
                return defaultResponseDTO;
            }

            defaultResponseDTO = saveStationSeriesTime(con, params);
            if (!Objects.equals(defaultResponseDTO.getMessage(), "OK")) {
                return defaultResponseDTO;
            }
            con.commit();
        } catch (Exception e) {
            con.rollback();
            if (con != null) {
                con.close();
            }
            defaultResponseDTO.setStatus(0);
            defaultResponseDTO.setMessage("L???i khi th??m m???i: " + e.getMessage());
            return defaultResponseDTO;
        }
        defaultResponseDTO.setStatus(1);
        defaultResponseDTO.setMessage("Th??m m???i th??nh c??ng");
        return defaultResponseDTO;
    }

    private DefaultResponseDTO saveStation(Connection connection, Map<String, String> params) throws SQLException, BusinessException {
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql = "INSERT INTO STATIONS(STATION_ID, STATION_CODE, STATION_NAME, LONGITUDE, LATITUDE, ADDRESS, STATUS,TRANS_MISS, "
                + " COUNTRY_ID,AREA_ID,DISTRICT_ID, PROVINCE_ID, RIVER_ID, STATION_TYPE_ID, WARD_ID,IS_ACTIVE,CREATED_AT,UPDATED_AT) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,sysdate,sysdate)";
        Long seq = commonService.getSequence("STATIONS_SEQ");
        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            int idx = 1;
            statement.setLong(idx++, seq);
            statement.setString(idx++, params.get("stationCode"));
            statement.setString(idx++, params.get("stationName"));
            statement.setString(idx++, params.get("longtitude"));
            statement.setString(idx++, params.get("latitude"));
            statement.setString(idx++, params.get("address"));
            statement.setString(idx++, params.get("status"));
            statement.setInt(idx++, 1);
            statement.setString(idx++, params.get("countryId"));
            statement.setString(idx++, params.get("areaId"));
            statement.setString(idx++, params.get("districtId"));
            statement.setString(idx++, params.get("provinceId"));
            statement.setString(idx++, params.get("riverId"));
            statement.setString(idx++, params.get("stationTypeId"));
            statement.setString(idx++, params.get("wardId"));
            statement.setInt(idx++, 1);

            statement.execute();

            defaultResponseDTO.setStatus(seq.intValue());
            defaultResponseDTO.setMessage("OK");

        } catch (Exception e) {
            defaultResponseDTO.setStatus(0);
            defaultResponseDTO.setMessage("L???i th???c hi???n :" + e.getMessage());
        }
        return defaultResponseDTO;
    }

    private DefaultResponseDTO saveStationTypeObject(Connection connection, Map<String, String> params) throws SQLException {
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql = "INSERT INTO STATIONS_OBJECT_TYPE(STATIONS_OBJECT_TYPE_ID, STATION_ID, OBJECT_TYPE_ID) "
                + "values (STATIONS_OBJECT_TYPE_SEQ.nextval, ?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            int idx = 1;
            statement.setString(idx++, params.get("stationId"));
            statement.setString(idx++, params.get("stationTypeId"));

            statement.execute();
            defaultResponseDTO.setStatus(1);
            defaultResponseDTO.setMessage("OK");
        } catch (Exception e) {
            defaultResponseDTO.setStatus(0);
            defaultResponseDTO.setMessage("L???i th???c hi???n :" + e.getMessage());
        }
        return defaultResponseDTO;
    }

    private DefaultResponseDTO saveParamter(Connection connection, Map<String, String> params) throws SQLException {
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql = "INSERT INTO PARAMETER(STATION_PARAMETER_ID, PARAMETER_TYPE_ID, STATION_ID, TIME_FREQUENCY) "
                + "values (PARAMETER_KTTV_SEQ.nextval, ?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            int idx = 1;
            statement.setString(idx++, params.get("parameter"));
            statement.setString(idx++, params.get("stationId"));
            statement.setString(idx++, params.get("frequency"));

            statement.execute();
            defaultResponseDTO.setStatus(1);
            defaultResponseDTO.setMessage("OK");
        } catch (Exception e) {
            defaultResponseDTO.setStatus(0);
            defaultResponseDTO.setMessage("L???i th???c hi???n :" + e.getMessage());
        }
        return defaultResponseDTO;
    }

    private DefaultResponseDTO saveStationSeriesTime(Connection connection, Map<String, String> params) throws SQLException {
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql = "INSERT INTO STATION_TIME_SERIES(ID,TS_ID, TS_NAME, STATION_ID, TS_TYPE_ID, PARAMETERTYPE_ID, PARAMETERTYPE_NAME, "
                + "PARAMETERTYPE_DESCRIPTION, STATION_NO, STATION_NAME, STATION_LONGNAME, STATION_LATITUDE, STATION_LONGTITUDE, CATCHMENT_ID, "
                + "CATCHMENT_NAME, SITE_ID, SITE_NAME, RIVER_ID, RIVER_NAME, PROVINCE_ID, PROVINCE_NAME, DISTRICT_ID, DISTRICT_NAME, WARD_ID, "
                + "WARD_NAME, COUNTRY_ID, COUNTRY_NAME, PROJECT_ID, PROJECT_NAME, STORAGE, CREATE_DATE) "
                + "values (STATION_TIME_SERIES_SEQ.nextval, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,sysdate)";
        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            int idx = 1;
            statement.setString(idx++, params.get("tsId"));
            statement.setString(idx++, params.get("tsName"));
            statement.setString(idx++, params.get("stationId"));
            statement.setString(idx++, params.get("tsTypeId"));
            statement.setString(idx++, params.get("parameterTypeId"));
            statement.setString(idx++, params.get("paramterTypeName"));
            statement.setString(idx++, params.get("parameterTypeDes"));
            statement.setString(idx++, params.get("stationCode"));
            statement.setString(idx++, params.get("stationName"));
            statement.setString(idx++, params.get("stationLongName"));
            statement.setString(idx++, params.get("longtitude"));
            statement.setString(idx++, params.get("latitude"));
            statement.setString(idx++, params.get("catchementId"));
            statement.setString(idx++, params.get("catchementName"));
            statement.setString(idx++, params.get("siteId"));
            statement.setString(idx++, params.get("siteName"));
            statement.setString(idx++, params.get("riverId"));
            statement.setString(idx++, params.get("riverName"));
            statement.setString(idx++, params.get("provinceId"));
            statement.setString(idx++, params.get("provinceName"));
            statement.setString(idx++, params.get("districtId"));
            statement.setString(idx++, params.get("districtName"));
            statement.setString(idx++, params.get("wardId"));
            statement.setString(idx++, params.get("wardName"));
            statement.setString(idx++, params.get("countryId"));
            statement.setString(idx++, params.get("countryName"));
            statement.setString(idx++, params.get("projectId"));
            statement.setString(idx++, params.get("projectName"));
            statement.setString(idx++, params.get("storage"));
            statement.execute();
            defaultResponseDTO.setStatus(1);
            defaultResponseDTO.setMessage("OK");
        } catch (Exception e) {
            defaultResponseDTO.setStatus(0);
            defaultResponseDTO.setMessage("L???i th???c hi???n :" + e.getMessage());
            return defaultResponseDTO;
        }
        return defaultResponseDTO;
    }

    public ByteArrayInputStream export() {
        List<String> tutorials = new ArrayList<>();
        tutorials.add("a");
        tutorials.add("b");

        ByteArrayInputStream in = write2Excel(tutorials);
        return in;
    }

    public static ByteArrayInputStream write2Excel(List<String> tutorials) {

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet(SHEET);

            // Header
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERs.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERs[col]);
            }
            int rowIdx = 1;
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(tutorials.get(0));
            row.createCell(1).setCellValue(tutorials.get(1));
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public DefaultResponseDTO saveOrUpdateManualParameterPLSQL(Map<String, String> params, boolean isNew) throws SQLException, JsonProcessingException {
        DefaultResponseDTO defaultResponseDTO = DefaultResponseDTO.builder().build();
        String sql = "";
        if (isNew) {
            sql = "begin ? := STATION.create_manual_parameter(?,?,?,?,?,?); end;";
        } else {
            sql = "begin ? := STATION.update_manual_parameter(?,?,?,?,?,?,?); end;";
        }
        try (Connection con = ds.getConnection(); CallableStatement st = con.prepareCall(sql);) {
            log.info(objectMapper.writeValueAsString(params));
            int i = 2;
            if (!isNew) {
                st.setString(i++, params.get("productId"));
            }
            st.setString(i++, params.get("stationCodeN"));
//			st.setString(i++,params.get("pSalinity"));
//			st.setString(i++,params.get("pWT"));
//			st.setString(i++,params.get("pConductivity"));
            st.setString(i++, params.get("timestampN"));
            st.setString(i++, params.get("value"));
//			st.setString(i++,params.get("waterTemperature"));
            st.setString(i++, params.get("type_parameter"));
            st.setString(i++, params.get("statusN"));
            st.setString(i++, params.get("username"));
            st.registerOutParameter(1, Types.VARCHAR);
            st.execute();
            String result = st.getString(1);
            if (result.startsWith("OK")) {
                defaultResponseDTO.setStatus(1);
                if (isNew) {
                    defaultResponseDTO.setMessage("Th??m m???i th??nh c??ng");
                } else {
                    defaultResponseDTO.setMessage("C???p nh???t th??nh c??ng");
                }
            } else {
                defaultResponseDTO.setStatus(0);
                defaultResponseDTO.setMessage(result);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            defaultResponseDTO.setStatus(0);
            if (isNew) {
                defaultResponseDTO.setMessage("L???i khi th??m m???i: " + e.getMessage());
            } else {
                defaultResponseDTO.setMessage("L???i khi c???p nh???t: " + e.getMessage());
            }
            return defaultResponseDTO;
        }
        return defaultResponseDTO;
    }

    public String getStringStationPagination(String search, List<Object> paramSearch) {
        StringBuilder sql = new StringBuilder("select a.*,b1.AREA_NAME, b.PROVINCE_NAME, c.DISTRICT_NAME, e.RIVER_NAME, d.WARD_NAME, g.* from stations a ,AREAS b1, PROVINCES b, DISTRICTS c, WARDS d, rivers e , stations_object_type f , OBJECT_TYPE g\r\n" +
                "where a.AREA_ID = b1.AREA_ID(+) and a.PROVINCE_ID = b.PROVINCE_ID(+) and a.DISTRICT_ID = c.DISTRICT_ID(+) and a.WARD_ID = d.WARD_ID(+) and a.RIVER_ID = e.RIVER_ID(+) and a.STATION_ID = f.STATION_ID and f.OBJECT_TYPE_ID = g.OBJECT_TYPE_ID");
        if (Strings.isNotEmpty(search)) {
            try {
                Map<String, String> params = objectMapper.readValue(search, Map.class);
                if (Strings.isNotEmpty(params.get("s_objectType"))) {
                    sql.append(" and g.OBJECT_TYPE = ? ");
                    paramSearch.add(params.get("s_objectType"));
                }
                if (Strings.isNotEmpty(params.get("s_objectTypeName"))) {
                    sql.append(" and lower(g.OBJECT_TYPE_SHORTNAME) like lower(?) ");
                    paramSearch.add("%" + params.get("s_objectTypeName") + "%");
                }
                if (Strings.isNotEmpty(params.get("s_stationId"))) {
                    sql.append(" and a.station_id = ? ");
                    paramSearch.add(params.get("s_stationId"));
                }
                if (Strings.isNotEmpty(params.get("s_stationCode"))) {
                    sql.append(" and a.station_code = ? ");
                    paramSearch.add(params.get("s_stationCode"));
                }
                if (Strings.isNotEmpty(params.get("s_stationName"))) {
                    sql.append(" and lower(a.STATION_NAME) like lower(?) ");
                    paramSearch.add("%" + params.get("s_stationName") + "%");
                }
                if (Strings.isNotEmpty(params.get("s_longtitude"))) {
                    sql.append(" and a.LONGTITUDE = ? ");
                    paramSearch.add(params.get("s_longtitude"));
                }
                if (Strings.isNotEmpty(params.get("s_latitude"))) {
                    sql.append(" and a.LATITUDE = ? ");
                    paramSearch.add(params.get("s_latitude"));
                }
                if (Strings.isNotEmpty(params.get("s_provinceName"))) {
                    sql.append(" and lower(b.PROVINCE_NAME) like lower(?) ");
                    paramSearch.add("%" + params.get("s_provinceName") + "%");
                }
                if (Strings.isNotEmpty(params.get("s_districtName"))) {
                    sql.append(" and lower(c.DISTRICT_NAME) like lower(?) ");
                    paramSearch.add("%" + params.get("s_districtName") + "%");
                }
                if (Strings.isNotEmpty(params.get("s_wardName"))) {
                    sql.append(" and lower(d.WARD_NAME) like lower(?) ");
                    paramSearch.add("%" + params.get("s_wardName") + "%");
                }

                if (Strings.isNotEmpty(params.get("s_address"))) {
                    sql.append(" and lower(a.address) like lower(?) ");
                    paramSearch.add("%" + params.get("s_address") + "%");
                }

                if (Strings.isNotEmpty(params.get("s_riverName"))) {
                    sql.append(" and lower(e.RIVER_NAME) like lower(?) ");
                    paramSearch.add("%" + params.get("s_riverName") + "%");
                }

                if (Strings.isNotEmpty(params.get("s_status"))) {
                    sql.append(" and a.is_active = ? ");
                    paramSearch.add(params.get("s_status"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sql.append(" order by a.CREATED_AT desc ");
        return sql.toString();
    }

    public List<Station> getRsListStationPagination(ResultSet rs) {
        List<Station> list = new ArrayList<>();
        try {
            while (rs.next()) {
                Station bo = Station.builder()
                        .stationId(rs.getString("STATION_ID"))
                        .stationCode(rs.getString("STATION_CODE"))
                        .elevation(rs.getFloat("ELEVATION"))
                        .stationName(rs.getString("STATION_NAME"))
                        .latitude(rs.getFloat("LATITUDE"))
                        .longtitude(rs.getFloat("LONGTITUDE"))
                        .trans_miss(rs.getInt("TRANS_MISS"))
                        .address(rs.getString("ADDRESS"))
                        .is_active(rs.getInt("IS_ACTIVE"))
                        .riverId(rs.getLong("RIVER_ID"))
                        .riverName(rs.getString("RIVER_NAME"))
                        .provinceId(rs.getLong("PROVINCE_ID"))
                        .provinceName(rs.getString("PROVINCE_NAME"))
                        .districtId(rs.getLong("DISTRICT_ID"))
                        .districtName(rs.getString("DISTRICT_NAME"))
                        .countryId(rs.getInt("COUNTRY_ID"))
//                        .countryName(rs.getString("COUNTRY_NAME"))
                        .wardId(rs.getLong("WARD_ID"))
                        .wardName(rs.getString("WARD_NAME"))
                        .projectId(rs.getInt("PROJECT_ID"))
                        .modeStationType(rs.getInt("MODE_CONTROL"))
                        .areaId(rs.getLong("AREA_ID"))
                        .areaName(rs.getString("AREA_NAME"))
                        .stationTypeId(rs.getLong("STATION_TYPE_ID"))
                        .objectTypeId(rs.getInt("OBJECT_TYPE_ID"))
                        .objectType(rs.getString("OBJECT_TYPE"))
                        .objectTypeName(rs.getString("OBJECT_TYPE_SHORTNAME"))
                        .isAuto(rs.getInt("IS_AUTO"))
                        .build();
                list.add(bo);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return list;
    }

    public List<Map> getRsListMapStationPagination(ResultSet rs) {
        List<Map> list = new ArrayList<>();
        try {
            while (rs.next()) {
                Map bo = new HashMap();
                bo.put("stationId", rs.getString("STATION_ID"));
                bo.put("stationCode", rs.getString("STATION_CODE"));
                bo.put("stationName", rs.getString("STATION_NAME"));
                bo.put("latitude", rs.getFloat("LATITUDE"));
                bo.put("longtitude", rs.getFloat("LONGTITUDE"));
                bo.put("trans_miss", rs.getInt("TRANS_MISS"));
                bo.put("address", rs.getString("ADDRESS"));
                bo.put("is_active", rs.getInt("IS_ACTIVE"));
                bo.put("riverId",rs.getLong("RIVER_ID"));
                bo.put("riverName",rs.getString("RIVER_NAME"));
                bo.put("provinceId",rs.getLong("PROVINCE_ID"));
                bo.put("provinceName",rs.getString("PROVINCE_NAME"));
                bo.put("districtId",rs.getLong("DISTRICT_ID"));
                bo.put("districtName",rs.getString("DISTRICT_NAME"));
                bo.put("countryId",rs.getInt("COUNTRY_ID"));
//                bo.put("countryName",rs.getString("COUNTRY_NAME"));
                bo.put("wardId",rs.getLong("WARD_ID"));
                bo.put("wardName",rs.getString("WARD_NAME"));
                bo.put("projectId",rs.getInt("PROJECT_ID"));
                bo.put("modeStationType",rs.getInt("MODE_CONTROL"));
                bo.put("areaId",rs.getLong("AREA_ID"));
                bo.put("areaName",rs.getString("AREA_NAME"));
                bo.put("stationTypeId",rs.getLong("STATION_TYPE_ID"));
                bo.put("objectTypeId",rs.getInt("OBJECT_TYPE_ID"));
                bo.put("objectType",rs.getString("OBJECT_TYPE"));
                bo.put("objectTypeName",rs.getString("OBJECT_TYPE_SHORTNAME"));
//						.build();
                list.add(bo);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return list;
    }
}
