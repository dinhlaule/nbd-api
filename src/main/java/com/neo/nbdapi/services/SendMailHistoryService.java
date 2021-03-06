package com.neo.nbdapi.services;

import com.neo.nbdapi.dto.DefaultPaginationDTO;
import com.neo.nbdapi.dto.DefaultResponseDTO;
import com.neo.nbdapi.entity.ComboBox;
import com.neo.nbdapi.entity.ComboBoxStr;
import com.neo.nbdapi.entity.NotificationHistory;
import com.neo.nbdapi.exception.BusinessException;
import com.neo.nbdapi.rest.vm.DefaultRequestPagingVM;
import com.neo.nbdapi.services.objsearch.SearchSendMailHistory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.mail.MessagingException;
import java.sql.SQLException;
import java.util.List;

public interface SendMailHistoryService {

    DefaultPaginationDTO getListOutpust(DefaultRequestPagingVM defaultRequestPagingVM) throws SQLException, BusinessException;

    List<ComboBoxStr> getListStations(String userId) throws SQLException, BusinessException;

    List<ComboBox> getLstWarningManagerByStationId(String stationId) throws SQLException, BusinessException;

    List<NotificationHistory> getListOutpust2(SearchSendMailHistory searchSendMailHistory) throws SQLException;

    SXSSFWorkbook export(SearchSendMailHistory searchSendMailHistory) throws SQLException;

    DefaultResponseDTO sendEmail(List<Long> groupEmailid, Long warningStationId) throws MessagingException, SQLException;

}
