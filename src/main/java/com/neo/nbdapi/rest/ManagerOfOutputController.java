package com.neo.nbdapi.rest;

import com.neo.nbdapi.dto.DefaultPaginationDTO;
import com.neo.nbdapi.dto.HistoryOutPutsDTO;
import com.neo.nbdapi.dto.UserGroupDTO;
import com.neo.nbdapi.entity.ComboBox;
import com.neo.nbdapi.entity.ComboBoxStr;
import com.neo.nbdapi.exception.BusinessException;
import com.neo.nbdapi.rest.vm.DefaultRequestPagingVM;
import com.neo.nbdapi.rest.vm.ManageOutPutVM;
import com.neo.nbdapi.rest.vm.UsersManagerVM;
import com.neo.nbdapi.services.ManageCDHService;
import com.neo.nbdapi.services.ManageOutputService;
import com.neo.nbdapi.services.UsersManagerService;
import com.neo.nbdapi.utils.Constants;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(Constants.APPLICATION_API.API_PREFIX + Constants.APPLICATION_API.MODULE.URI_MANAGER_OUTPUTS)
public class ManagerOfOutputController {

    private Logger logger = LogManager.getLogger(ManagerOfOutputController.class);

    @Autowired
    private ManageOutputService manageOutputService;

    @Autowired
    private HikariDataSource ds;

    @PostMapping("/get_list_outputs")
    public DefaultPaginationDTO getListOutpust(@RequestBody @Valid DefaultRequestPagingVM defaultRequestPagingVM) throws SQLException, BusinessException {
        return manageOutputService.getListOutpust(defaultRequestPagingVM);
    }

    @GetMapping("/get_list_stations")
    public List<ComboBoxStr>  get_list_group_users(@RequestParam("username") String userId,@RequestParam("stationsType_search") String stationsType_search) throws SQLException, BusinessException {
        return manageOutputService.getListStations(userId,stationsType_search);
    }

    @GetMapping("/get_list_stations_type")
    public List<ComboBoxStr>  get_list_stations_type() throws SQLException, BusinessException {
        return manageOutputService.getListStationsType();
    }

    @GetMapping("/getList_parameter_byStationId")
    public List<ComboBox>  getListParameterByStations(@RequestParam("stationId") String stationId) throws SQLException, BusinessException {
        return manageOutputService.getListParameterByStations(stationId);
    }


    @GetMapping("/get_sqlStatement")
    public String  getSqlStatement(@RequestParam("stationId") String stationId,@RequestParam("parameterTypeId") String parameterTypeId,@RequestParam("fromDate") String fromDate,@RequestParam("toDate") String toDate) throws SQLException, BusinessException {
        return manageOutputService.getSqlStatement(stationId,parameterTypeId,fromDate,toDate);
    }

    @PostMapping("/editValueProd")
    public String editValueProd(@RequestBody @Valid ManageOutPutVM manageOutPutVM) throws SQLException, BusinessException {
        return manageOutputService.editValueProd(manageOutPutVM);
    }

    @GetMapping("/get_select2_time_searchHistory")
    public List<ComboBoxStr>  get_select2_time_searchHistory(@RequestParam("prodId") String prodId,@RequestParam("prodTableName") String prodTableName) throws SQLException, BusinessException {
        return manageOutputService.getListtimeHistory(prodId,prodTableName);
    }

    @GetMapping("/get_history_by_time")
    public List<HistoryOutPutsDTO> getHistoryByTime(@RequestParam(name = "time") String time, @RequestParam(name = "prodId") String prodId, @RequestParam(name = "tablePrName") String tablePrName) throws SQLException, BusinessException{
        return manageOutputService.getHistoryByTimes(time,prodId,tablePrName);
    }
}
