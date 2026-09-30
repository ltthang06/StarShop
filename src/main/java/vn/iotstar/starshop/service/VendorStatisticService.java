package vn.iotstar.starshop.service;

import java.time.LocalDate;

import vn.iotstar.starshop.dto.VendorStatisticData;

public interface VendorStatisticService {

    VendorStatisticData getStatistics(
            Long shopId,
            Long ownerId
    );

    VendorStatisticData.RangeStatistic getRangeStatistics(
            Long shopId,
            Long ownerId,
            LocalDate fromDate,
            LocalDate toDate
    );
}